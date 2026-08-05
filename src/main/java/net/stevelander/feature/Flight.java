package net.stevelander.feature;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class Flight {
    private static final double BYPASS_FALL_SPEED = -0.04;

    private static final int BYPASS_INTERVAL = 40;

    private static boolean enabled;

    private static boolean wasFlyingAllowed;

    private static int bypassStage;

    private Flight() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        enabled = !enabled;

        if (enabled) {
            wasFlyingAllowed = player.getAbilities().mayfly;
            player.getAbilities().mayfly = false;
            bypassStage = 0;
        } else {
            player.getAbilities().mayfly = wasFlyingAllowed;
        }

        NoFall.reset();
    }

    public static void tick(Minecraft minecraft) {
        if (!enabled) {
            return;
        }

        final LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        if (VehicleControl.tick(minecraft, player)) {
            return;
        }

        final StevelanderConfig.Flight settings = Stevelander.config().flight;

        if (NoFall.isRecovering()) {
            bypassStage = 0;
        }

        if (bypassStage == 1) {
            final Vec3 current = player.getDeltaMovement();
            player.setDeltaMovement(new Vec3(current.x, BYPASS_FALL_SPEED, current.z));
            bypassStage = 2;
            return;
        }

        if (bypassStage == 2) {
            bypassStage = 0;
            return;
        }

        final boolean useSprintSpeed = minecraft.options.keySprint.isDown() && settings.sprintSpeed.enabled;
        final float horizontal = useSprintSpeed
            ? settings.sprintSpeed.horizontal
            : settings.baseSpeed.horizontal;
        final float vertical = useSprintSpeed
            ? settings.sprintSpeed.vertical
            : settings.baseSpeed.vertical;

        final boolean jump = minecraft.options.keyJump.isDown();
        final boolean sneak = minecraft.options.keyShift.isDown();

        final double y;
        if (NoFall.isRecovering()) {
            y = NoFall.RECOVERY_ASCENT;
        } else if (jump && !sneak) {
            y = vertical;
        } else if (sneak && !jump) {
            y = -vertical;
        } else {
            y = settings.glide;
        }

        final Vec3 strafed = withStrafe(player, horizontal);
        player.setDeltaMovement(new Vec3(strafed.x, y, strafed.z));

        if (settings.bypassVanillaCheck && player.tickCount % BYPASS_INTERVAL == 0) {
            bypassStage = 1;
        }
    }

    private static Vec3 withStrafe(LocalPlayer player, double speed) {
        final Vec3 current = player.getDeltaMovement();
        final Input input = player.input.keyPresses;

        final boolean forwards = input.forward();
        final boolean backwards = input.backward();
        final boolean left = input.left();
        final boolean right = input.right();

        if (!forwards && !backwards && !left && !right) {
            return new Vec3(0.0, current.y, 0.0);
        }

        final double angle = Math.toRadians(movementDirection(player.getYRot(), forwards, backwards, left, right));
        return new Vec3(-Math.sin(angle) * speed, current.y, Math.cos(angle) * speed);
    }

    static float movementDirection(
        float facingYaw,
        boolean forwards,
        boolean backwards,
        boolean left,
        boolean right
    ) {
        float actualYaw = facingYaw;
        final float forwardMultiplier;

        if (backwards && !forwards) {
            actualYaw += 180.0F;
            forwardMultiplier = -0.5F;
        } else if (forwards && !backwards) {
            forwardMultiplier = 0.5F;
        } else {
            forwardMultiplier = 1.0F;
        }

        if (left && !right) {
            actualYaw -= 90.0F * forwardMultiplier;
        }
        if (right && !left) {
            actualYaw += 90.0F * forwardMultiplier;
        }

        return actualYaw;
    }

    public static void onPlayerAbilities(ClientboundPlayerAbilitiesPacket packet) {
        wasFlyingAllowed = packet.canFly();
    }

    public static void onSetback() {
        if (NoFall.consumeExpectedSetback()) {
            return;
        }

        if (!enabled || !Stevelander.config().flight.disableOnSetback) {
            return;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        enabled = false;
        player.getAbilities().mayfly = wasFlyingAllowed;

        Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(
            Component.literal("Setback detected, flight disabled.").withStyle(ChatFormatting.RED)
        );
    }
}
