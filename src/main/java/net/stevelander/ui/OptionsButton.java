package net.stevelander.ui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class OptionsButton {

    private static final int WIDTH = 150;
    private static final int HEIGHT = 20;
    private static final int GAP = 4;

    private OptionsButton() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof OptionsScreen)) {
                return;
            }

            final AbstractWidget done = findDone(screen);
            final int x;
            final int y;

            if (done == null) {
                x = width / 2 + WIDTH / 2 + GAP;
                y = height - HEIGHT - GAP;
            } else {
                done.setX(width / 2 - WIDTH - GAP / 2);
                done.setWidth(WIDTH);
                x = width / 2 + GAP / 2;
                y = done.getY();
            }

            Screens.getWidgets(screen).add(Button.builder(
                Component.translatable("stevelander.options.title"),
                b -> client.setScreenAndShow(new StevelanderOptionsScreen(screen))
            ).bounds(x, y, WIDTH, HEIGHT).build());
        });
    }

    private static AbstractWidget findDone(net.minecraft.client.gui.screens.Screen screen) {
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (CommonComponents.GUI_DONE.equals(widget.getMessage())) {
                return widget;
            }
        }
        return null;
    }
}
