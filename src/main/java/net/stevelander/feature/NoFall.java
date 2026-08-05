package net.stevelander.feature;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;
import net.stevelander.interfaces.MovePacketAccess;

public final class NoFall {
    public static final String MODE_SPOOF_LANDING = "SPOOF_LANDING";

    public static final String MODE_NO_GROUND = "NO_GROUND";

    private static final int RECOVERY_TICKS = 5;

    public static final double RECOVERY_ASCENT = 0.42;

    private static double serverFallDistance;

    private static double lastReportedY;
    private static boolean haveLastReportedY;

    private static int recoveryTicks;

    private static boolean expectingSetback;

    private NoFall() {
    }

    public static void onMovePacket(ServerboundMovePlayerPacket packet) {
        if (!Flight.isEnabled()) {
            return;
        }

        final StevelanderConfig.NoFall settings = Stevelander.config().flight.noFall;
        if (!settings.enabled) {
            return;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        final MovePacketAccess access = (MovePacketAccess) packet;

        final boolean clientOnGround = player.onGround();

        trackServerFallDistance(packet);

        if (MODE_NO_GROUND.equalsIgnoreCase(settings.mode)) {
            access.stevelander$setOnGround(false);
            if (settings.resetFallDistance) {
                player.resetFallDistance();
            }
            return;
        }

        spoofLanding(access, player, settings, clientOnGround);
    }

    private static void trackServerFallDistance(ServerboundMovePlayerPacket packet) {
        if (!packet.hasPosition()) {
            return;
        }

        final double y = packet.getY(lastReportedY);

        if (haveLastReportedY) {
            final double dy = y - lastReportedY;
            if (dy > 0.0) {
                serverFallDistance = 0.0;
            } else {
                serverFallDistance -= dy;
            }
        }

        lastReportedY = y;
        haveLastReportedY = true;
    }

    private static void spoofLanding(
        MovePacketAccess access,
        LocalPlayer player,
        StevelanderConfig.NoFall settings,
        boolean clientOnGround
    ) {
        if (serverFallDistance < player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE)) {
            if (clientOnGround) {
                serverFallDistance = 0.0;
            }
            return;
        }

        access.stevelander$setOnGround(false);

        if (clientOnGround) {
            recoveryTicks = RECOVERY_TICKS;

            final StevelanderConfig.Offset offset = settings.landingOffset;
            if (offset.x != 0.0 || offset.y != 0.0 || offset.z != 0.0) {
                expectingSetback = true;
                access.stevelander$offsetPosition(offset.x, offset.y, offset.z);
            }
        }

        if (settings.resetFallDistance) {
            player.resetFallDistance();
        }
    }

    public static boolean isRecovering() {
        return recoveryTicks > 0;
    }

    public static void tick() {
        if (recoveryTicks > 0) {
            recoveryTicks--;
        }
    }

    public static boolean consumeExpectedSetback() {
        final boolean expected = expectingSetback;
        expectingSetback = false;
        return expected;
    }

    public static void reset() {
        serverFallDistance = 0.0;
        lastReportedY = 0.0;
        haveLastReportedY = false;
        recoveryTicks = 0;
        expectingSetback = false;
    }

    public static String normaliseMode(String mode) {
        if (mode == null) {
            return null;
        }

        final String upper = mode.trim().toUpperCase(Locale.ROOT);
        if (MODE_NO_GROUND.equals(upper) || MODE_SPOOF_LANDING.equals(upper)) {
            return upper;
        }

        return null;
    }
}
