package net.stevelander.feature;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Aim {

    private Aim() {
    }

    public static float yRot(Vec3 direction) {
        return (float) Math.toDegrees(Mth.atan2(direction.z, direction.x)) - 90.0F;
    }

    public static float xRot(Vec3 direction) {
        return (float) -Math.toDegrees(Mth.atan2(direction.y, direction.horizontalDistance()));
    }
}
