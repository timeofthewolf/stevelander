package net.stevelander;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StevelanderConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(Stevelander.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Keybinds keybinds = new Keybinds();
    public Flight flight = new Flight();
    public AntiHunger antiHunger = new AntiHunger();

    public static final class Keybinds {
        public String flight = "RIGHT_CONTROL";
        public String xray = "RIGHT_SHIFT";
    }

    public static final class Flight {
        public float glide = 0.0F;

        public boolean bypassVanillaCheck = true;

        public boolean disableOnSetback = false;

        public Speed baseSpeed = new Speed(0.44F, 0.44F);
        public SprintSpeed sprintSpeed = new SprintSpeed();
        public NoFall noFall = new NoFall();
        public Vehicle vehicle = new Vehicle();
    }

    public static final class Vehicle {
        public boolean enabled = true;

        public Speed baseSpeed = new Speed(0.5F, 0.35F);
        public SprintSpeed sprintSpeed = new SprintSpeed(5.0F, 2.0F);

        public float glide = -0.15F;

        public boolean mouseControl = false;

        public boolean noGlideOnSprint = false;

        public boolean sneakDescends = true;
    }

    public static final class AntiHunger {
        public boolean enabled = true;

        public boolean keepFloating = true;

        public boolean noSprint = true;

        public boolean noSprintWhileSwimming = false;
    }

    public static final class NoFall {
        public boolean enabled = true;

        public String mode = "SPOOF_LANDING";

        public Offset landingOffset = new Offset(0.0, 0.0, 0.0);

        public boolean resetFallDistance = true;
    }

    public static final class Offset {
        public double x;
        public double y;
        public double z;

        public Offset() {
            this(0.0, 0.0, 0.0);
        }

        public Offset(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Speed {
        public float horizontal;
        public float vertical;

        public Speed() {
            this(0.44F, 0.44F);
        }

        public Speed(float horizontal, float vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
    }

    public static final class SprintSpeed extends Speed {
        public boolean enabled = true;

        public SprintSpeed() {
            super(1.0F, 1.0F);
        }

        public SprintSpeed(float horizontal, float vertical) {
            super(horizontal, vertical);
        }
    }

    public static StevelanderConfig load() {
        final Path path = FabricLoader.getInstance().getConfigDir().resolve(Stevelander.MOD_ID + ".json");

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                final StevelanderConfig config = GSON.fromJson(reader, StevelanderConfig.class);
                if (config != null) {
                    return config;
                }
                LOGGER.warn("{} is empty, falling back to defaults", path);
            } catch (IOException | JsonParseException e) {
                LOGGER.error("Could not read {}, falling back to defaults", path, e);
            }
            return new StevelanderConfig();
        }

        final StevelanderConfig config = new StevelanderConfig();
        config.save(path);
        return config;
    }

    private void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Could not write {}", path, e);
        }
    }

    public static int resolveKey(String name, int fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }

        try {
            return GLFW.class.getField("GLFW_KEY_" + name.trim().toUpperCase(java.util.Locale.ROOT)).getInt(null);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Unknown key \"{}\", falling back to the default bind", name);
            return fallback;
        }
    }
}
