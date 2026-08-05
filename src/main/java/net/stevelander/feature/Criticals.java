package net.stevelander.feature;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class Criticals {

    public static final String MODE_NONE = "NONE";
    public static final String MODE_PACKET = "PACKET";
    public static final String MODE_NO_GROUND = "NO_GROUND";
    public static final String MODE_JUMP = "JUMP";

    private static volatile boolean adjustNextJump;

    private Criticals() {
    }

    public static boolean isEnabled() {
        return Stevelander.config().criticals.enabled;
    }

    private static String mode() {
        return Stevelander.config().criticals.mode;
    }

    public static boolean isNoGroundActive() {
        return isEnabled() && MODE_NO_GROUND.equalsIgnoreCase(mode());
    }

    public static void onAttack(Entity target) {
        if (!isEnabled() || !MODE_PACKET.equalsIgnoreCase(mode())) {
            return;
        }

        if (!(target instanceof LivingEntity)) {
            return;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !canCrit(player)) {
            return;
        }

        switch (Stevelander.config().criticals.packetMode.toUpperCase(Locale.ROOT)) {
            case "VANILLA" -> {
                offset(player, 0.2);
                offset(player, 0.01);
            }
            case "FALLING" -> {
                offset(player, 0.0625);
                offset(player, 0.0625013579);
                offset(player, 0.0000013579);
            }
            case "LOW" -> {
                offset(player, 1e-9);
                offset(player, 0.0);
            }
            case "DOWN" -> offset(player, -1e-9);
            default -> {
                offset(player, 0.11);
                offset(player, 0.1100013579);
                offset(player, 0.0000013579);
            }
        }
    }

    public static void tick(Minecraft minecraft) {
        if (!isEnabled() || !MODE_JUMP.equalsIgnoreCase(mode())) {
            adjustNextJump = false;
            return;
        }

        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        if (!player.onGround() || !canCrit(player)) {
            return;
        }

        if (findTarget(minecraft, player) == null) {
            return;
        }

        adjustNextJump = true;
        player.jumpFromGround();
    }

    public static boolean shouldAdjustJump() {
        final boolean adjust = adjustNextJump;
        adjustNextJump = false;
        return adjust;
    }

    public static float jumpHeight() {
        return Stevelander.config().criticals.jump.height;
    }

    private static Entity findTarget(Minecraft minecraft, LocalPlayer player) {
        final StevelanderConfig.Criticals.Jump settings = Stevelander.config().criticals.jump;
        final double range = settings.range;
        final AABB search = player.getBoundingBox().inflate(range);

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : minecraft.level.getEntities(player, search)) {
            if (!(entity instanceof LivingEntity) || !entity.isAlive()) {
                continue;
            }

            final double distance = player.distanceTo(entity);
            if (distance > range || distance >= closestDistance) {
                continue;
            }

            if (settings.canBeSeen && !player.hasLineOfSight(entity)) {
                continue;
            }

            closest = entity;
            closestDistance = distance;
        }

        return closest;
    }

    private static boolean canCrit(LocalPlayer player) {
        return !player.onClimbable()
            && !player.isInWater()
            && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
            && !player.isPassenger();
    }

    private static void offset(LocalPlayer player, double dy) {
        if (player.connection == null) {
            return;
        }

        player.connection.send(new ServerboundMovePlayerPacket.Pos(
            player.getX(),
            player.getY() + dy,
            player.getZ(),
            false,
            player.horizontalCollision
        ));
    }

    public static String normaliseMode(String mode) {
        if (mode == null) {
            return null;
        }

        final String upper = mode.trim().toUpperCase(Locale.ROOT);
        return switch (upper) {
            case MODE_NONE, MODE_PACKET, MODE_NO_GROUND, MODE_JUMP -> upper;
            default -> null;
        };
    }
}
