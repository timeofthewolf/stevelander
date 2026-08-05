package net.stevelander.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class MaceKill {

    private MaceKill() {
    }

    public static void onAttack() {
        final StevelanderConfig.MaceKill settings = Stevelander.config().maceKill;
        if (!settings.enabled) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        if (!player.getMainHandItem().is(Items.MACE)) {
            return;
        }

        final int height = determineHeight(minecraft, player, settings.fallHeight);
        if (height <= 0) {
            return;
        }

        if (height > 10) {
            final int repeats = (int) Math.ceil(Math.abs(height / 10.0));
            for (int i = 0; i < repeats; i++) {
                warp(player, null, false);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                warp(player, null, player.onGround());
            }
        }

        warp(player, player.position().add(0.0, height, 0.0), false);
        warp(player, player.position(), false);
    }

    private static int determineHeight(Minecraft minecraft, LocalPlayer player, int maxHeight) {
        final AABB box = player.getBoundingBox();

        for (int i = maxHeight; i >= 1; i--) {
            final AABB raised = box.move(0.0, i, 0.0);
            if (!minecraft.level.getBlockCollisions(player, raised).iterator().hasNext()) {
                return i;
            }
        }

        return 0;
    }

    private static void warp(LocalPlayer player, Vec3 pos, boolean onGround) {
        if (player.connection == null) {
            return;
        }

        if (pos == null) {
            player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(
                onGround, player.horizontalCollision));
        } else {
            player.connection.send(new ServerboundMovePlayerPacket.Pos(
                pos.x, pos.y, pos.z, onGround, player.horizontalCollision));
        }
    }
}
