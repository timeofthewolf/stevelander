package net.stevelander.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;
import net.stevelander.interfaces.MovePacketAccess;

public final class AntiHunger {
    private AntiHunger() {
    }

    public static void onMovePacket(ServerboundMovePlayerPacket packet, MovePacketAccess access) {
        final StevelanderConfig.AntiHunger settings = Stevelander.config().antiHunger;
        if (!settings.enabled || !settings.keepFloating) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        if (player.isPassenger() || (minecraft.gameMode != null && minecraft.gameMode.isDestroying())) {
            return;
        }

        if (player.isInWater() || player.isSwimming() || player.isUnderWater()) {
            return;
        }

        if (packet.isOnGround() && player.fallDistance <= 0.0) {
            access.stevelander$setOnGround(false);
        }
    }

    public static void tick(Minecraft minecraft) {
        final StevelanderConfig.AntiHunger settings = Stevelander.config().antiHunger;
        if (!settings.enabled || !settings.noSprint) {
            return;
        }

        final LocalPlayer player = minecraft.player;
        if (player == null || !player.isSprinting()) {
            return;
        }

        if (player.isSwimming() && !settings.noSprintWhileSwimming) {
            return;
        }

        player.setSprinting(false);
    }
}
