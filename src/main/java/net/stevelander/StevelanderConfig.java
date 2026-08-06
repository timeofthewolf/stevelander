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
    public boolean fullBright = true;
    public boolean spoofClientBrand = true;
    public XRay xray = new XRay();

    public static final class XRay {
        public java.util.List<String> blocks = new java.util.ArrayList<>(java.util.List.of(
            "minecraft:coal_ore",
            "minecraft:copper_ore",
            "minecraft:diamond_ore",
            "minecraft:emerald_ore",
            "minecraft:gold_ore",
            "minecraft:iron_ore",
            "minecraft:lapis_ore",
            "minecraft:redstone_ore",
            "minecraft:deepslate_coal_ore",
            "minecraft:deepslate_copper_ore",
            "minecraft:deepslate_diamond_ore",
            "minecraft:deepslate_emerald_ore",
            "minecraft:deepslate_gold_ore",
            "minecraft:deepslate_iron_ore",
            "minecraft:deepslate_lapis_ore",
            "minecraft:deepslate_redstone_ore",
            "minecraft:coal_block",
            "minecraft:diamond_block",
            "minecraft:emerald_block",
            "minecraft:gold_block",
            "minecraft:iron_block",
            "minecraft:lapis_block",
            "minecraft:redstone_block",
            "minecraft:raw_copper_block",
            "minecraft:raw_gold_block",
            "minecraft:raw_iron_block",
            "minecraft:ancient_debris",
            "minecraft:nether_gold_ore",
            "minecraft:nether_quartz_ore",
            "minecraft:netherite_block",
            "minecraft:chest",
            "minecraft:trapped_chest",
            "minecraft:ender_chest",
            "minecraft:barrel",
            "minecraft:shulker_box",
            "minecraft:hopper",
            "minecraft:dispenser",
            "minecraft:dropper",
            "minecraft:spawner",
            "minecraft:beacon",
            "minecraft:enchanting_table",
            "minecraft:dragon_egg",
            "minecraft:end_portal",
            "minecraft:end_portal_frame",
            "minecraft:nether_portal",
            "minecraft:tnt",
            "minecraft:lava",
            "minecraft:water"
        ));
    }
    public Flight flight = new Flight();
    public AntiHunger antiHunger = new AntiHunger();
    public KeepSprint keepSprint = new KeepSprint();
    public MaceKill maceKill = new MaceKill();
    public SpearKill spearKill = new SpearKill();
    public FireTrail fireTrail = new FireTrail();
    public Criticals criticals = new Criticals();
    public AntiExploit antiExploit = new AntiExploit();
    public LiquidPlace liquidPlace = new LiquidPlace();
    public AirPlace airPlace = new AirPlace();

    public static final class KeepSprint {
        public boolean enabled = true;
        public float motion = 100.0F;
        public float motionWhenHurt = 100.0F;
        public int hurtTimeMin = 1;
        public int hurtTimeMax = 10;
        public float chance = 100.0F;
    }

    public static final class MaceKill {
        public boolean enabled = false;
        public int fallHeight = 22;
    }

    public static final class SpearKill {
        public boolean enabled = false;
        public String mode = "TELEPORT";
        public float maxSpeed = 7.0F;
        public boolean returnAfterHit = true;
        public float maxTargetDistance = 50.0F;

        public float lockAngle = 25.0F;

        public float overkill = 4.0F;

        public int holdTicks = 2;

        public int maxPackets = 5;
    }

    public static final class FireTrail {
        public boolean enabled = false;

        public int durabilityReserve = 1;

        public float gazeRange = 64.0F;
        public boolean extendedRange = false;
        public boolean carrierBlocks = false;
        public int maxPackets = 5;

        public boolean avoidDamage = true;

        public float safeDistance = 1.5F;

        public int lookaheadTicks = 10;

        public boolean reroute = true;

        public int rerouteRadius = 3;
    }

    public static final class Criticals {
        public boolean enabled = false;
        public String mode = "PACKET";
        public String packetMode = "NO_CHEAT_PLUS";
        public Jump jump = new Jump();

        public static final class Jump {
            public float height = 0.42F;
            public float range = 4.0F;
            public boolean canBeSeen = true;
        }
    }

    public static final class AntiExploit {
        public boolean enabled = true;
        public boolean limitExplosionStrength = true;
        public boolean limitParticlesAmount = true;
        public boolean limitParticlesSpeed = true;
        public float maxExplosionStrength = 20.0F;
        public int maxParticlesAmount = 2000;
        public float maxParticlesSpeed = 10.0F;
        public boolean cancelDemo = true;
        public boolean ignoreProtocolKick = true;
        public boolean notify = true;
    }

    public static final class LiquidPlace {
        public boolean enabled = false;
    }

    public static final class AirPlace {
        public boolean enabled = false;
        public boolean customRange = false;
        public float range = 3.0F;
        public boolean placeInLiquid = false;
    }

    public static final class Keybinds {
        public String flight = "RIGHT_CONTROL";
        public String xray = "RIGHT_SHIFT";
        public String fireTrail = "G";
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

        public boolean noSprint = false;

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

    public void save() {
        save(FabricLoader.getInstance().getConfigDir().resolve(Stevelander.MOD_ID + ".json"));
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
