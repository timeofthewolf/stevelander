package net.stevelander.feature;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class SpearKill {

    private static final double MIN_RANGE_CLEARANCE = 0.5;

    private static final double CLEARANCE_STEP = 0.25;

    private static final double TICKS_PER_SECOND = 20.0;
    private static final int ABORT_COOLDOWN = 10;

    public static final String MODE_TELEPORT = "TELEPORT";
    public static final String MODE_FLIGHT = "FLIGHT";

    private static final Deque<Vec3> plan = new ArrayDeque<>();

    private static Vec3 origin;
    private static Vec3 strike;
    private static int heldTicks;
    private static int cooldown;

    private SpearKill() {
    }

    public static boolean tick(Minecraft minecraft) {
        final StevelanderConfig.SpearKill settings = Stevelander.config().spearKill;
        final LocalPlayer player = minecraft.player;

        if (player == null || player.connection == null) {
            reset(player);
            return false;
        }

        if (!plan.isEmpty()) {
            player.setDeltaMovement(plan.removeFirst());
            return true;
        }

        if (origin != null) {
            hold(minecraft, player, settings);
            return false;
        }

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        if (!settings.enabled || !minecraft.options.keyAttack.isDown()) {
            return false;
        }

        final ItemStack using = player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY;
        final KineticWeapon weapon = using.get(DataComponents.KINETIC_WEAPON);

        if (weapon == null || player.getTicksUsingItem() < weapon.delayTicks()) {
            return false;
        }

        final Target target = findTarget(player, settings);
        if (target == null) {
            return false;
        }

        if (MODE_FLIGHT.equals(settings.mode)) {
            buildPlan(player, target, weapon, settings);
            return !plan.isEmpty();
        }

        if (!Warp.wasSetBack()) {
            launch(minecraft, player, using, weapon, target, settings);
        }

        return false;
    }

    private static void buildPlan(
        LocalPlayer player,
        Target target,
        KineticWeapon weapon,
        StevelanderConfig.SpearKill settings
    ) {
        final double speed = Math.max(0.1, settings.maxSpeed);
        final int approach = Math.max(1, (int) Math.ceil(target.distance() / speed - 0.5));
        final double travel = 2.0 * target.distance() * approach / (2.0 * approach + 1);

        final int ticks = Math.max(1, (int) Math.ceil(travel / speed));

        Vec3 direction = target.aim().subtract(player.getEyePosition());
        if (direction.lengthSqr() <= 0.0) {
            direction = player.getLookAngle();
        }

        final Vec3 movement = direction.normalize().scale(travel / ticks);

        for (int i = 0; i < ticks; i++) {
            plan.addLast(movement);
        }

        if (settings.returnAfterHit) {
            final Vec3 back = movement.scale(-1.0);
            for (int i = 0; i < ticks; i++) {
                plan.addLast(back);
            }
        }

        plan.addLast(Vec3.ZERO);
        cooldown = weapon.contactCooldownTicks();
    }

    private static void reset(LocalPlayer player) {
        origin = null;
        strike = null;

        if (!plan.isEmpty()) {
            plan.clear();

            if (player != null) {
                player.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    private static void hold(Minecraft minecraft, LocalPlayer player, StevelanderConfig.SpearKill settings) {
        if (++heldTicks < Math.max(1, settings.holdTicks)) {
            return;
        }

        Warp.moveTo(minecraft, player, strike, player.position(),
            player.getYRot(), player.getXRot(), settings.maxPackets);

        origin = null;
        strike = null;
    }

    public static boolean isBusy() {
        return origin != null || !plan.isEmpty();
    }

    public static void abort() {
        origin = null;
        strike = null;
        plan.clear();
        heldTicks = 0;
        cooldown = ABORT_COOLDOWN;
    }

    private record Target(LivingEntity entity, Vec3 aim, double distance) {
    }

    private static Target findTarget(LocalPlayer player, StevelanderConfig.SpearKill settings) {
        final Vec3 eye = player.getEyePosition();
        final Vec3 look = player.getLookAngle();
        final double maxDistance = settings.maxTargetDistance;
        final double minAlignment = Math.cos(Math.toRadians(settings.lockAngle));

        final List<LivingEntity> candidates = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(maxDistance),
            entity -> entity != player
                && entity.isAlive()
                && entity.isAttackable()
                && !entity.isSpectator()
        );

        Target best = null;
        double bestAlignment = minAlignment;

        for (LivingEntity entity : candidates) {
            final Vec3 aim = entity.getBoundingBox().getCenter();
            final Vec3 offset = aim.subtract(eye);
            final double distance = offset.length();

            if (distance < 1.0E-4 || distance > maxDistance) {
                continue;
            }

            final double alignment = offset.scale(1.0 / distance).dot(look);
            if (alignment < bestAlignment || !canSee(player, eye, aim)) {
                continue;
            }

            bestAlignment = alignment;
            best = new Target(entity, aim, distance);
        }

        return best;
    }

    private static boolean canSee(LocalPlayer player, Vec3 eye, Vec3 aim) {
        final HitResult hit = player.level().clip(new ClipContext(
            eye, aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        return hit.getType() == HitResult.Type.MISS;
    }

    private static void launch(
        Minecraft minecraft,
        LocalPlayer player,
        ItemStack using,
        KineticWeapon weapon,
        Target target,
        StevelanderConfig.SpearKill settings
    ) {
        final Vec3 eye = player.getEyePosition();
        final Vec3 direction = target.aim().subtract(eye).normalize();

        final AttackRange range = player.getAttackRangeWith(using);
        final double maxReach = range.effectiveMaxRange(player);
        final double minReach = range.effectiveMinRange(player);

        final double reachNeed = (target.distance() - maxReach) / 2.0 + range.hitboxMargin();
        final double damageNeed = damageWarp(player, weapon, target, settings);

        final double wanted = Math.max(Math.max(reachNeed, damageNeed), 0.0);
        if (wanted <= 0.0) {
            return;
        }

        final double ceiling = Math.min(
            Warp.budget(minecraft, player, settings.maxPackets),
            target.distance() - minReach - MIN_RANGE_CLEARANCE
        );

        final double distance = clearDistance(player, direction, Math.min(wanted, ceiling));
        if (distance <= 0.0) {
            return;
        }

        origin = player.position();
        heldTicks = 0;
        cooldown = weapon.contactCooldownTicks();

        strike = Warp.moveTo(
            minecraft,
            player,
            origin,
            origin.add(direction.scale(distance)),
            Aim.yRot(direction),
            Aim.xRot(direction),
            settings.maxPackets
        );
    }

    private static double damageWarp(
        LocalPlayer player,
        KineticWeapon weapon,
        Target target,
        StevelanderConfig.SpearKill settings
    ) {
        final double multiplier = weapon.damageMultiplier();
        if (multiplier <= 0.0) {
            return 0.0;
        }

        final double base = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        final double health = target.entity().getHealth() + target.entity().getAbsorptionAmount();

        return (health * settings.overkill - base) / (TICKS_PER_SECOND * multiplier);
    }

    private static double clearDistance(LocalPlayer player, Vec3 direction, double max) {
        if (max <= 0.0) {
            return 0.0;
        }

        final AABB box = player.getBoundingBox();
        double reached = 0.0;

        for (double travelled = CLEARANCE_STEP; travelled <= max; travelled += CLEARANCE_STEP) {
            if (!player.level().noCollision(player, box.move(direction.scale(travelled)))) {
                return reached;
            }
            reached = travelled;
        }

        return player.level().noCollision(player, box.move(direction.scale(max))) ? max : reached;
    }
}
