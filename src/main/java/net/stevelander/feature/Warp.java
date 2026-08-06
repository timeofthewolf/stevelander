package net.stevelander.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Warp {

    private static final double ALLOWANCE = 100.0;
    private static final double MARGIN = 0.95;
    private static final double STEP = Math.sqrt(ALLOWANCE) * MARGIN;
    private static final double GROUND_PROBE = 0.0625;
    private static final double PATH_PROBE = 0.5;
    private static final double EPSILON = 1.0E-4;

    private static boolean setback;

    private Warp() {
    }

    public static void clearSetback() {
        setback = false;
    }

    public static void onSetback() {
        setback = true;
    }

    public static boolean wasSetBack() {
        return setback;
    }

    public static boolean unlimited(Minecraft minecraft) {
        return minecraft.hasSingleplayerServer();
    }

    public static double budget(Minecraft minecraft, LocalPlayer player, int maxPackets) {
        return unlimited(minecraft) ? Double.MAX_VALUE : STEP * maxPackets;
    }

    public static Vec3 moveTo(
        Minecraft minecraft,
        LocalPlayer player,
        Vec3 from,
        Vec3 to,
        float yRot,
        float xRot,
        int maxPackets
    ) {
        if (player.connection == null) {
            return from;
        }

        if (unlimited(minecraft)) {
            send(player, to, yRot, xRot);
            return to;
        }

        Vec3 at = from;

        for (int packet = 0; packet < maxPackets; packet++) {
            final Vec3 remaining = to.subtract(at);
            final double distance = remaining.length();

            if (distance <= EPSILON) {
                return at;
            }

            final double step = Math.min(distance, STEP);
            final Vec3 next = at.add(remaining.scale(step / distance));

            if (!pathClear(player, at, next)) {
                return at;
            }

            at = next;
            send(player, at, yRot, xRot);
        }

        return at;
    }

    private static boolean pathClear(LocalPlayer player, Vec3 from, Vec3 to) {
        final Vec3 delta = to.subtract(from);
        final int probes = (int) Math.ceil(delta.length() / PATH_PROBE);

        for (int probe = 1; probe <= probes; probe++) {
            if (!clear(player, from.add(delta.scale((double) probe / probes)))) {
                return false;
            }
        }

        return true;
    }

    private static boolean clear(LocalPlayer player, Vec3 pos) {
        return player.level().noCollision(player, boxAt(player, pos));
    }

    public static boolean grounded(LocalPlayer player, Vec3 pos) {
        return clear(player, pos) && supported(player, pos);
    }

    private static boolean supported(LocalPlayer player, Vec3 pos) {
        return !player.level().noCollision(player, boxAt(player, pos).move(0.0, -GROUND_PROBE, 0.0));
    }

    private static AABB boxAt(LocalPlayer player, Vec3 pos) {
        return player.getBoundingBox().move(pos.subtract(player.position()));
    }

    private static void send(LocalPlayer player, Vec3 pos, float yRot, float xRot) {
        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
            pos, yRot, xRot, supported(player, pos), player.horizontalCollision));
    }
}
