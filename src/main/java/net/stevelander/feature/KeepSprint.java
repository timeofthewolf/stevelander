package net.stevelander.feature;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class KeepSprint {

    private static final double VANILLA_MOTION = 0.6;

    private KeepSprint() {
    }

    public static boolean isEnabled() {
        return Stevelander.config().keepSprint.enabled;
    }

    public static double getMotion() {
        final StevelanderConfig.KeepSprint settings = Stevelander.config().keepSprint;

        if (settings.chance < 100.0F
            && ThreadLocalRandom.current().nextFloat() * 100.0F >= settings.chance) {
            return VANILLA_MOTION;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        final boolean hurt = player != null
            && player.hurtTime >= settings.hurtTimeMin
            && player.hurtTime <= settings.hurtTimeMax;

        return (hurt ? settings.motionWhenHurt : settings.motion) / 100.0;
    }
}
