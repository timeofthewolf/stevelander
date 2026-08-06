package net.stevelander.ui;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.stevelander.Keybinds;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;
import net.stevelander.feature.Criticals;
import net.stevelander.feature.NoFall;
import net.stevelander.feature.SpearKill;
import org.lwjgl.glfw.GLFW;

public class StevelanderOptionsScreen extends OptionsSubScreen {

    private static final List<String> NOFALL_MODES = List.of(
        NoFall.MODE_SPOOF_LANDING, NoFall.MODE_NO_GROUND);
    private static final List<String> CRITICALS_MODES = List.of(
        Criticals.MODE_NONE, Criticals.MODE_PACKET, Criticals.MODE_NO_GROUND, Criticals.MODE_JUMP);
    private static final List<String> PACKET_MODES = List.of(
        "NO_CHEAT_PLUS", "VANILLA", "FALLING", "LOW", "DOWN");
    private static final List<String> SPEARKILL_MODES = List.of(
        SpearKill.MODE_TELEPORT, SpearKill.MODE_FLIGHT);

    private enum Bind { FLIGHT, XRAY, FIRE_TRAIL }

    private Button flightBind;
    private Button xrayBind;
    private Button fireTrailBind;
    private Bind rebinding;

    public StevelanderOptionsScreen(Screen lastScreen) {
        super(lastScreen, net.minecraft.client.Minecraft.getInstance().options,
            Component.translatable("stevelander.options.title"));
    }

    @Override
    protected void addOptions() {
        final StevelanderConfig config = Stevelander.config();

        this.list.addHeader(Component.translatable("stevelander.group.keybinds"));
        this.flightBind = Button.builder(Component.empty(), b -> beginRebind(Bind.FLIGHT)).build();
        this.xrayBind = Button.builder(Component.empty(), b -> beginRebind(Bind.XRAY)).build();
        this.fireTrailBind = Button.builder(Component.empty(), b -> beginRebind(Bind.FIRE_TRAIL)).build();
        this.list.addSmall(this.flightBind, this.xrayBind);
        this.list.addBig(this.fireTrailBind);
        refreshBindLabels();

        this.list.addSmall(
            Options.bool("stevelander.fullBright", () -> config.fullBright, v -> config.fullBright = v),
            Options.bool("stevelander.antiExploit.enabled",
                () -> config.antiExploit.enabled, v -> config.antiExploit.enabled = v)
        );

        this.list.addBig(Button.builder(
            Component.translatable("stevelander.xray.blocks"),
            b -> this.minecraft.setScreenAndShow(new XRayBlockListScreen(this))
        ).build());

        addFlight(config);
        addNoFall(config);
        addVehicle(config);
        addAntiHunger(config);
        addCombat(config);
        addFireTrail(config);
        addWorld(config);
        addAntiExploitDetail(config);
    }

    private void addFlight(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.flight"));
        this.list.addSmall(
            Options.floatSlider("stevelander.flight.baseHorizontal", 0.1F, 10.0F, 0.01F,
                () -> config.flight.baseSpeed.horizontal, v -> config.flight.baseSpeed.horizontal = v),
            Options.floatSlider("stevelander.flight.baseVertical", 0.1F, 10.0F, 0.01F,
                () -> config.flight.baseSpeed.vertical, v -> config.flight.baseSpeed.vertical = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.flight.sprintEnabled",
                () -> config.flight.sprintSpeed.enabled, v -> config.flight.sprintSpeed.enabled = v),
            Options.floatSlider("stevelander.flight.glide", -1.0F, 1.0F, 0.01F,
                () -> config.flight.glide, v -> config.flight.glide = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.flight.sprintHorizontal", 0.1F, 10.0F, 0.01F,
                () -> config.flight.sprintSpeed.horizontal, v -> config.flight.sprintSpeed.horizontal = v),
            Options.floatSlider("stevelander.flight.sprintVertical", 0.1F, 10.0F, 0.01F,
                () -> config.flight.sprintSpeed.vertical, v -> config.flight.sprintSpeed.vertical = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.flight.bypassVanillaCheck",
                () -> config.flight.bypassVanillaCheck, v -> config.flight.bypassVanillaCheck = v),
            Options.bool("stevelander.flight.disableOnSetback",
                () -> config.flight.disableOnSetback, v -> config.flight.disableOnSetback = v)
        );
    }

    private void addNoFall(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.noFall"));
        this.list.addSmall(
            Options.bool("stevelander.noFall.enabled",
                () -> config.flight.noFall.enabled, v -> config.flight.noFall.enabled = v),
            Options.bool("stevelander.noFall.resetFallDistance",
                () -> config.flight.noFall.resetFallDistance, v -> config.flight.noFall.resetFallDistance = v)
        );
        this.list.addBig(Options.choice("stevelander.noFall.mode", NOFALL_MODES,
            () -> config.flight.noFall.mode, v -> config.flight.noFall.mode = v).createButton(this.options));
    }

    private void addVehicle(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.vehicle"));
        this.list.addSmall(
            Options.bool("stevelander.vehicle.enabled",
                () -> config.flight.vehicle.enabled, v -> config.flight.vehicle.enabled = v),
            Options.bool("stevelander.vehicle.sneakDescends",
                () -> config.flight.vehicle.sneakDescends, v -> config.flight.vehicle.sneakDescends = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.vehicle.baseHorizontal", 0.1F, 10.0F, 0.01F,
                () -> config.flight.vehicle.baseSpeed.horizontal,
                v -> config.flight.vehicle.baseSpeed.horizontal = v),
            Options.floatSlider("stevelander.vehicle.baseVertical", 0.1F, 10.0F, 0.01F,
                () -> config.flight.vehicle.baseSpeed.vertical,
                v -> config.flight.vehicle.baseSpeed.vertical = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.vehicle.sprintHorizontal", 0.1F, 10.0F, 0.01F,
                () -> config.flight.vehicle.sprintSpeed.horizontal,
                v -> config.flight.vehicle.sprintSpeed.horizontal = v),
            Options.floatSlider("stevelander.vehicle.sprintVertical", 0.1F, 10.0F, 0.01F,
                () -> config.flight.vehicle.sprintSpeed.vertical,
                v -> config.flight.vehicle.sprintSpeed.vertical = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.vehicle.glide", -0.3F, 0.3F, 0.01F,
                () -> config.flight.vehicle.glide, v -> config.flight.vehicle.glide = v),
            Options.bool("stevelander.vehicle.mouseControl",
                () -> config.flight.vehicle.mouseControl, v -> config.flight.vehicle.mouseControl = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.vehicle.noGlideOnSprint",
                () -> config.flight.vehicle.noGlideOnSprint, v -> config.flight.vehicle.noGlideOnSprint = v),
            Options.bool("stevelander.vehicle.sprintEnabled",
                () -> config.flight.vehicle.sprintSpeed.enabled,
                v -> config.flight.vehicle.sprintSpeed.enabled = v)
        );
    }

    private void addAntiHunger(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.antiHunger"));
        this.list.addSmall(
            Options.bool("stevelander.antiHunger.enabled",
                () -> config.antiHunger.enabled, v -> config.antiHunger.enabled = v),
            Options.bool("stevelander.antiHunger.keepFloating",
                () -> config.antiHunger.keepFloating, v -> config.antiHunger.keepFloating = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.antiHunger.noSprint",
                () -> config.antiHunger.noSprint, v -> config.antiHunger.noSprint = v),
            Options.bool("stevelander.antiHunger.noSprintWhileSwimming",
                () -> config.antiHunger.noSprintWhileSwimming,
                v -> config.antiHunger.noSprintWhileSwimming = v)
        );
    }

    private void addCombat(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.combat"));
        this.list.addSmall(
            Options.bool("stevelander.keepSprint.enabled",
                () -> config.keepSprint.enabled, v -> config.keepSprint.enabled = v),
            Options.floatSlider("stevelander.keepSprint.chance", 0.0F, 100.0F, 1.0F,
                () -> config.keepSprint.chance, v -> config.keepSprint.chance = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.keepSprint.motion", 0.0F, 100.0F, 1.0F,
                () -> config.keepSprint.motion, v -> config.keepSprint.motion = v),
            Options.floatSlider("stevelander.keepSprint.motionWhenHurt", 0.0F, 100.0F, 1.0F,
                () -> config.keepSprint.motionWhenHurt, v -> config.keepSprint.motionWhenHurt = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.maceKill.enabled",
                () -> config.maceKill.enabled, v -> config.maceKill.enabled = v),
            Options.intSlider("stevelander.maceKill.fallHeight", 1, 170,
                () -> config.maceKill.fallHeight, v -> config.maceKill.fallHeight = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.spearKill.enabled",
                () -> config.spearKill.enabled, v -> config.spearKill.enabled = v),
            Options.floatSlider("stevelander.spearKill.lockAngle", 1.0F, 90.0F, 1.0F,
                () -> config.spearKill.lockAngle, v -> config.spearKill.lockAngle = v)
        );
        this.list.addBig(Options.choice("stevelander.spearKill.mode", SPEARKILL_MODES,
            () -> config.spearKill.mode, v -> config.spearKill.mode = v).createButton(this.options));
        this.list.addSmall(
            Options.floatSlider("stevelander.spearKill.maxSpeed", 2.0F, 10.0F, 0.1F,
                () -> config.spearKill.maxSpeed, v -> config.spearKill.maxSpeed = v),
            Options.bool("stevelander.spearKill.returnAfterHit",
                () -> config.spearKill.returnAfterHit, v -> config.spearKill.returnAfterHit = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.spearKill.maxTargetDistance", 3.0F, 200.0F, 1.0F,
                () -> config.spearKill.maxTargetDistance, v -> config.spearKill.maxTargetDistance = v),
            Options.floatSlider("stevelander.spearKill.overkill", 1.0F, 10.0F, 0.5F,
                () -> config.spearKill.overkill, v -> config.spearKill.overkill = v)
        );
        this.list.addSmall(
            Options.intSlider("stevelander.spearKill.maxPackets", 1, 40,
                () -> config.spearKill.maxPackets, v -> config.spearKill.maxPackets = v),
            Options.intSlider("stevelander.spearKill.holdTicks", 1, 6,
                () -> config.spearKill.holdTicks, v -> config.spearKill.holdTicks = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.criticals.enabled",
                () -> config.criticals.enabled, v -> config.criticals.enabled = v),
            Options.bool("stevelander.criticals.canBeSeen",
                () -> config.criticals.jump.canBeSeen, v -> config.criticals.jump.canBeSeen = v)
        );
        this.list.addBig(Options.choice("stevelander.criticals.mode", CRITICALS_MODES,
            () -> config.criticals.mode, v -> config.criticals.mode = v).createButton(this.options));
        this.list.addBig(Options.choice("stevelander.criticals.packetMode", PACKET_MODES,
            () -> config.criticals.packetMode, v -> config.criticals.packetMode = v).createButton(this.options));
        this.list.addSmall(
            Options.floatSlider("stevelander.criticals.jumpHeight", 0.1F, 0.42F, 0.01F,
                () -> config.criticals.jump.height, v -> config.criticals.jump.height = v),
            Options.floatSlider("stevelander.criticals.jumpRange", 1.0F, 6.0F, 0.1F,
                () -> config.criticals.jump.range, v -> config.criticals.jump.range = v)
        );
    }

    private void addFireTrail(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.fireTrail"));
        this.list.addSmall(
            Options.bool("stevelander.fireTrail.enabled",
                () -> config.fireTrail.enabled, v -> config.fireTrail.enabled = v),
            Options.bool("stevelander.fireTrail.extendedRange",
                () -> config.fireTrail.extendedRange, v -> config.fireTrail.extendedRange = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.fireTrail.avoidDamage",
                () -> config.fireTrail.avoidDamage, v -> config.fireTrail.avoidDamage = v),
            Options.floatSlider("stevelander.fireTrail.safeDistance", 0.0F, 6.0F, 0.5F,
                () -> config.fireTrail.safeDistance, v -> config.fireTrail.safeDistance = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.fireTrail.reroute",
                () -> config.fireTrail.reroute, v -> config.fireTrail.reroute = v),
            Options.intSlider("stevelander.fireTrail.rerouteRadius", 1, 8,
                () -> config.fireTrail.rerouteRadius, v -> config.fireTrail.rerouteRadius = v)
        );
        this.list.addSmall(
            Options.intSlider("stevelander.fireTrail.lookaheadTicks", 0, 40,
                () -> config.fireTrail.lookaheadTicks, v -> config.fireTrail.lookaheadTicks = v)
        );
        this.list.addSmall(
            Options.intSlider("stevelander.fireTrail.durabilityReserve", 0, 32,
                () -> config.fireTrail.durabilityReserve, v -> config.fireTrail.durabilityReserve = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.fireTrail.gazeRange", 4.0F, 200.0F, 1.0F,
                () -> config.fireTrail.gazeRange, v -> config.fireTrail.gazeRange = v),
            Options.intSlider("stevelander.fireTrail.maxPackets", 1, 40,
                () -> config.fireTrail.maxPackets, v -> config.fireTrail.maxPackets = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.fireTrail.carrierBlocks",
                () -> config.fireTrail.carrierBlocks, v -> config.fireTrail.carrierBlocks = v)
        );
    }

    private void addWorld(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.world"));
        this.list.addSmall(
            Options.bool("stevelander.liquidPlace.enabled",
                () -> config.liquidPlace.enabled, v -> config.liquidPlace.enabled = v),
            Options.bool("stevelander.airPlace.enabled",
                () -> config.airPlace.enabled, v -> config.airPlace.enabled = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.airPlace.customRange",
                () -> config.airPlace.customRange, v -> config.airPlace.customRange = v),
            Options.floatSlider("stevelander.airPlace.range", 1.0F, 4.5F, 0.1F,
                () -> config.airPlace.range, v -> config.airPlace.range = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.airPlace.placeInLiquid",
                () -> config.airPlace.placeInLiquid, v -> config.airPlace.placeInLiquid = v)
        );
    }

    private void addAntiExploitDetail(StevelanderConfig config) {
        this.list.addHeader(Component.translatable("stevelander.group.antiExploit"));
        this.list.addSmall(
            Options.bool("stevelander.antiExploit.limitExplosionStrength",
                () -> config.antiExploit.limitExplosionStrength,
                v -> config.antiExploit.limitExplosionStrength = v),
            Options.bool("stevelander.antiExploit.limitParticlesAmount",
                () -> config.antiExploit.limitParticlesAmount,
                v -> config.antiExploit.limitParticlesAmount = v)
        );
        this.list.addSmall(
            Options.bool("stevelander.antiExploit.limitParticlesSpeed",
                () -> config.antiExploit.limitParticlesSpeed,
                v -> config.antiExploit.limitParticlesSpeed = v),
            Options.bool("stevelander.antiExploit.notify",
                () -> config.antiExploit.notify, v -> config.antiExploit.notify = v)
        );
        this.list.addSmall(
            Options.floatSlider("stevelander.antiExploit.maxExplosionStrength", 1.0F, 100.0F, 1.0F,
                () -> config.antiExploit.maxExplosionStrength,
                v -> config.antiExploit.maxExplosionStrength = v),
            Options.intSlider("stevelander.antiExploit.maxParticlesAmount", 100, 20000,
                () -> config.antiExploit.maxParticlesAmount,
                v -> config.antiExploit.maxParticlesAmount = v)
        );
    }

    private void beginRebind(Bind bind) {
        this.rebinding = bind;
        refreshBindLabels();
    }

    private void refreshBindLabels() {
        if (this.flightBind == null || this.xrayBind == null || this.fireTrailBind == null) {
            return;
        }

        this.flightBind.setMessage(label("stevelander.keybind.flight", Keybinds.flight(), Bind.FLIGHT));
        this.xrayBind.setMessage(label("stevelander.keybind.xray", Keybinds.xray(), Bind.XRAY));
        this.fireTrailBind.setMessage(
            label("stevelander.keybind.fireTrail", Keybinds.fireTrail(), Bind.FIRE_TRAIL));
    }

    private Component label(String key, int bound, Bind bind) {
        final Component value = this.rebinding == bind
            ? Component.literal("> ")
                .append(Component.translatable("stevelander.keybind.press"))
                .append(" <")
                .withStyle(ChatFormatting.YELLOW)
            : Keybinds.displayName(bound);

        return Component.translatable(key).append(": ").append(value);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.rebinding == null) {
            return super.keyPressed(event);
        }

        final int key = event.key() == GLFW.GLFW_KEY_ESCAPE
            ? GLFW.GLFW_KEY_UNKNOWN
            : InputConstants.getKey(event).getValue();

        switch (this.rebinding) {
            case FLIGHT -> Keybinds.setFlight(key);
            case XRAY -> Keybinds.setXray(key);
            case FIRE_TRAIL -> Keybinds.setFireTrail(key);
        }

        this.rebinding = null;
        refreshBindLabels();
        Stevelander.config().save();
        return true;
    }

    @Override
    public void onClose() {
        Stevelander.config().save();
        super.onClose();
    }
}
