package net.stevelander.feature;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class SpearKill {
    private static final double MIN_TARGET_DISTANCE = 3.0;
    private static final double SEARCH_PADDING = 1.0;

    private static final Deque<Vec3> plan = new ArrayDeque<>();

    private SpearKill() {
    }

    public static boolean tick(Minecraft minecraft) {
        final StevelanderConfig.SpearKill settings = Stevelander.config().spearKill;
        final LocalPlayer player = minecraft.player;

        if (player == null) {
            plan.clear();
            return false;
        }

        if (!settings.enabled) {
            reset(player);
            return false;
        }

        final ItemStack using = player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY;
        final KineticWeapon weapon = using.get(DataComponents.KINETIC_WEAPON);

        if (weapon == null) {
            reset(player);
            return false;
        }

        if (player.getTicksUsingItem() <= weapon.delayTicks()) {
            plan.clear();
            return false;
        }

        if (!plan.isEmpty()) {
            player.setDeltaMovement(plan.removeFirst());
            return true;
        }

        final int chargeDuration = weapon.computeDamageUseDuration() - weapon.delayTicks();
        if (!minecraft.options.keyAttack.isDown() || player.getTicksUsingItem() >= chargeDuration) {
            return false;
        }

        final Target target = findTarget(player, settings);
        if (target != null) {
            buildPlan(player, target, settings);
        }

        return false;
    }

    public static void reset() {
        reset(Minecraft.getInstance().player);
    }

    private static void reset(LocalPlayer player) {
        if (plan.isEmpty()) {
            return;
        }

        plan.clear();

        if (player != null) {
            player.setDeltaMovement(Vec3.ZERO);
        }
    }

    private record Target(LivingEntity entity, double travel) {
    }

    private static Target findTarget(LocalPlayer player, StevelanderConfig.SpearKill settings) {
        final Level level = player.level();
        final Vec3 eye = player.getEyePosition();
        final Vec3 lookEnd = eye.add(player.getLookAngle().scale(settings.maxTargetDistance));

        final AABB search = player.getBoundingBox()
            .expandTowards(lookEnd.subtract(eye))
            .inflate(SEARCH_PADDING);

        final List<LivingEntity> candidates = level.getEntitiesOfClass(
            LivingEntity.class,
            search,
            entity -> entity != player
                && entity.isAlive()
                && entity.getBoundingBox().clip(eye, lookEnd).isPresent()
        );

        Target best = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (LivingEntity entity : candidates) {
            final double distanceSq = player.distanceToSqr(entity);
            if (distanceSq >= bestDistanceSq) {
                continue;
            }

            final double distance = Math.sqrt(distanceSq);
            if (distance < MIN_TARGET_DISTANCE || distance > settings.maxTargetDistance) {
                continue;
            }

            final int ticks = Math.max(1, (int) Math.ceil(distance / settings.maxSpeed - 0.5));
            final double travel = 2.0 * distance * ticks / (2.0 * ticks + 1);

            if (!hasClearPath(player, eye, travel)) {
                continue;
            }

            best = new Target(entity, travel);
            bestDistanceSq = distanceSq;
        }

        return best;
    }

    private static boolean hasClearPath(LocalPlayer player, Vec3 eye, double travel) {
        final Vec3 end = eye.add(player.getLookAngle().scale(travel));
        final BlockHitResult hit = player.level().clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceTo(eye) >= travel;
    }

    private static void buildPlan(LocalPlayer player, Target target, StevelanderConfig.SpearKill settings) {
        final int ticks = Math.max(1, (int) Math.ceil(target.travel() / settings.maxSpeed));
        final double velocity = target.travel() / ticks;

        Vec3 direction = predictPosition(target.entity(), ticks)
            .subtract(player.getEyePosition())
            .normalize();

        if (direction.lengthSqr() <= 0.0) {
            direction = player.getLookAngle().normalize();
        }

        final Vec3 movement = direction.scale(velocity);

        for (int i = 0; i < ticks; i++) {
            plan.addLast(movement);
        }

        if (settings.returnAfterHit) {
            final Vec3 reverse = movement.scale(-1.0);
            for (int i = 0; i < ticks; i++) {
                plan.addLast(reverse);
            }
        }

        plan.addLast(Vec3.ZERO);
    }

    private static Vec3 predictPosition(LivingEntity entity, int ticks) {
        final Vec3 position = entity.position();
        final Vec3 velocity = position.subtract(new Vec3(entity.xOld, entity.yOld, entity.zOld));

        return position
            .add(velocity.scale(ticks))
            .add(0.0, entity.getBbHeight() * 0.5, 0.0);
    }
}
