package net.stevelander;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {

    public static final int DEFAULT_FLIGHT = GLFW.GLFW_KEY_RIGHT_CONTROL;
    public static final int DEFAULT_XRAY = GLFW.GLFW_KEY_RIGHT_SHIFT;
    public static final int DEFAULT_FIRE_TRAIL = GLFW.GLFW_KEY_G;

    private static final String PREFIX = "GLFW_KEY_";

    private static volatile int flight = DEFAULT_FLIGHT;
    private static volatile int xray = DEFAULT_XRAY;
    private static volatile int fireTrail = DEFAULT_FIRE_TRAIL;

    private static Map<Integer, String> namesByCode;

    private Keybinds() {
    }

    public static void reload() {
        final StevelanderConfig config = Stevelander.config();
        flight = StevelanderConfig.resolveKey(config.keybinds.flight, DEFAULT_FLIGHT);
        xray = StevelanderConfig.resolveKey(config.keybinds.xray, DEFAULT_XRAY);
        fireTrail = StevelanderConfig.resolveKey(config.keybinds.fireTrail, DEFAULT_FIRE_TRAIL);
    }

    public static int flight() {
        return flight;
    }

    public static int xray() {
        return xray;
    }

    public static int fireTrail() {
        return fireTrail;
    }

    public static void setFlight(int key) {
        flight = key;
        Stevelander.config().keybinds.flight = nameOf(key);
    }

    public static void setXray(int key) {
        xray = key;
        Stevelander.config().keybinds.xray = nameOf(key);
    }

    public static void setFireTrail(int key) {
        fireTrail = key;
        Stevelander.config().keybinds.fireTrail = nameOf(key);
    }

    public static Component displayName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) {
            return Component.translatable("stevelander.keybind.unbound");
        }

        return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName();
    }

    public static String nameOf(int key) {
        final String name = names().get(key);
        return name != null ? name : String.valueOf(key);
    }

    private static synchronized Map<Integer, String> names() {
        if (namesByCode != null) {
            return namesByCode;
        }

        final Map<Integer, String> map = new HashMap<>();
        for (Field field : GLFW.class.getFields()) {
            if (!field.getName().startsWith(PREFIX) || field.getType() != int.class) {
                continue;
            }

            try {
                map.putIfAbsent(field.getInt(null), field.getName().substring(PREFIX.length()));
            } catch (IllegalAccessException ignored) {
            }
        }

        namesByCode = map;
        return map;
    }
}
