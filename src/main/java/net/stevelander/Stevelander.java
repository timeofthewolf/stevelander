package net.stevelander;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.stevelander.feature.AntiHunger;
import net.stevelander.feature.Criticals;
import net.stevelander.feature.Flight;
import net.stevelander.feature.NoFall;
import net.stevelander.feature.SpearKill;
import net.stevelander.feature.XRay;
import net.stevelander.ui.OptionsButton;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class Stevelander implements ClientModInitializer {
    public static final String MOD_ID = "stevelander";

    private static StevelanderConfig config = new StevelanderConfig();

    private boolean flightKeyWasDown;
    private boolean xrayKeyWasDown;

    public static StevelanderConfig config() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        config = StevelanderConfig.load();
        Keybinds.reload();

        final String mode = NoFall.normaliseMode(config.flight.noFall.mode);
        if (mode == null) {
            LoggerFactory.getLogger(MOD_ID).error(
                "Unknown noFall mode \"{}\", falling back to {}",
                config.flight.noFall.mode, NoFall.MODE_SPOOF_LANDING
            );
            config.flight.noFall.mode = NoFall.MODE_SPOOF_LANDING;
        } else {
            config.flight.noFall.mode = mode;
        }

        final String critMode = Criticals.normaliseMode(config.criticals.mode);
        if (critMode == null) {
            LoggerFactory.getLogger(MOD_ID).error(
                "Unknown criticals mode \"{}\", falling back to {}",
                config.criticals.mode, Criticals.MODE_PACKET
            );
            config.criticals.mode = Criticals.MODE_PACKET;
        } else {
            config.criticals.mode = critMode;
        }

        OptionsButton.register();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft minecraft) {
        Safety.run("tick", () -> tick(minecraft));
    }

    private void tick(Minecraft minecraft) {
        final boolean acceptsInput = minecraft.gui.screen() == null;
        final Window window = minecraft.getWindow();

        final boolean flightKeyDown = acceptsInput && InputConstants.isKeyDown(window, Keybinds.flight());
        if (flightKeyDown && !this.flightKeyWasDown) {
            Flight.toggle();
        }
        this.flightKeyWasDown = flightKeyDown;

        final boolean xrayKeyDown = acceptsInput && InputConstants.isKeyDown(window, Keybinds.xray());
        if (xrayKeyDown && !this.xrayKeyWasDown) {
            XRay.toggle();
        }
        this.xrayKeyWasDown = xrayKeyDown;

        AntiHunger.tick(minecraft);
        Criticals.tick(minecraft);

        if (!SpearKill.tick(minecraft)) {
            Flight.tick(minecraft);
        }

        NoFall.tick();
    }
}
