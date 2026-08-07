package net.stevelander.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class VehicleControl {
    private VehicleControl() {
    }

    public static boolean shouldWithholdSneak() {
        if (!Flight.isEnabled()) {
            return false;
        }

        final StevelanderConfig.Vehicle settings = Stevelander.config().flight.vehicle;
        if (!settings.enabled || !settings.sneakDescends) {
            return false;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        final Entity vehicle = player.getControlledVehicle();
        if (vehicle == null) {
            return false;
        }

        return !vehicle.onGround() && !vehicle.isInWater();
    }

    public static boolean tick(Minecraft minecraft, LocalPlayer player) {
        final StevelanderConfig.Vehicle settings = Stevelander.config().flight.vehicle;
        if (!settings.enabled) {
            return false;
        }

        final Entity vehicle = player.getControlledVehicle();
        if (vehicle == null) {
            return false;
        }

        final boolean useSprintSpeed = minecraft.options.keySprint.isDown() && settings.sprintSpeed.enabled;
        final float horizontal = useSprintSpeed
            ? settings.sprintSpeed.horizontal
            : settings.baseSpeed.horizontal;
        final float vertical = useSprintSpeed
            ? settings.sprintSpeed.vertical
            : settings.baseSpeed.vertical;

        if (settings.mouseControl) {
            vehicle.setYRot(player.getYRot());
            vehicle.yRotO = player.getYRot();
        }

        final double y;
        if (minecraft.options.keyJump.isDown()) {
            y = vertical;
        } else if (minecraft.options.keyShift.isDown()) {
            y = -vertical;
        } else if (!vehicle.isInWater() && !(useSprintSpeed && settings.noGlideOnSprint)) {
            y = settings.glide;
        } else {
            y = 0.0;
        }

        final Input input = player.input.keyPresses;
        final boolean forwards = input.forward();
        final boolean backwards = input.backward();
        final boolean left = input.left();
        final boolean right = input.right();
        final boolean moving = forwards || backwards || left || right;

        if (!moving) {
            vehicle.setDeltaMovement(Flight.capDiagonal(new Vec3(0.0, y, 0.0)));
            return true;
        }

        final float yaw = Flight.movementDirection(vehicle.getYRot(), forwards, backwards, left, right);
        final double angle = Math.toRadians(yaw);

        vehicle.setDeltaMovement(Flight.capDiagonal(new Vec3(
            -Math.sin(angle) * horizontal,
            y,
            Math.cos(angle) * horizontal
        )));

        return true;
    }
}
