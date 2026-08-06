package net.stevelander.feature;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class FireTrail {

    private static final double PLACE_RANGE = 5.5;

    private static final double[] VANTAGE_DISTANCES = {3.0, 2.0, 4.0};

    private static final Direction[] SUPPORTS = {
        Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
    };

    private static final double[] VANTAGE_YAWS = {0.0, 30.0, -30.0, 60.0, -60.0, 90.0, -90.0};

    private static final double[] VANTAGE_PITCHES = {0.0, 30.0, 60.0};

    private static final double REROUTE_MARGIN = 1.0;

    private static final double VANTAGE_DROP = 4.0;

    private static BlockPos lastAnchor;

    private FireTrail() {
    }

    public static void tick(Minecraft minecraft, boolean gazeDown) {
        final StevelanderConfig.FireTrail settings = Stevelander.config().fireTrail;
        final LocalPlayer player = minecraft.player;

        if (player == null || player.connection == null || !settings.enabled || !gazeDown) {
            lastAnchor = null;
            return;
        }

        if (SpearKill.isBusy() || Warp.wasSetBack()) {
            return;
        }

        final Vec3 eye = player.getEyePosition();
        final Vec3 end = eye.add(player.getLookAngle().scale(settings.gazeRange));

        final BlockHitResult hit = player.level().clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        final BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        if (target.equals(lastAnchor)) {
            return;
        }

        if (ignite(minecraft, player, target, settings)) {
            lastAnchor = target;
        }
    }

    private static boolean ignite(
        Minecraft minecraft,
        LocalPlayer player,
        BlockPos aimed,
        StevelanderConfig.FireTrail settings
    ) {
        final BlockPos target = choose(player, aimed, settings);
        if (target == null) {
            return false;
        }

        final Vec3 home = player.position();
        Vec3 at = home;

        if (!inRange(player, at, target)) {
            if (!settings.extendedRange) {
                return false;
            }

            final Vec3 vantage = vantage(player, target, settings);
            if (vantage == null) {
                return false;
            }

            at = Warp.moveTo(minecraft, player, at, vantage,
                player.getYRot(), player.getXRot(), settings.maxPackets);
        }

        boolean fired = false;

        if (inRange(player, at, target) && prepare(minecraft, player, target, at, settings)) {
            final Support support = findSupport(player.level(), target);
            final Held igniter = support == null ? null : acquireIgniter(minecraft, player, settings);

            if (igniter != null) {
                place(minecraft, player, support, igniter.hand());
                release(minecraft, player, igniter);
                fired = true;
            }
        }

        if (!at.equals(home)) {
            Warp.moveTo(minecraft, player, at, home, player.getYRot(), player.getXRot(), settings.maxPackets);
        }

        return fired;
    }

    private static BlockPos choose(LocalPlayer player, BlockPos aimed, StevelanderConfig.FireTrail settings) {
        final Level level = player.level();

        if (!level.getBlockState(aimed).isAir()) {
            return null;
        }

        if (!settings.avoidDamage || !burnsUs(player, aimed, settings)) {
            return aimed;
        }

        if (!settings.reroute) {
            return null;
        }

        final int radius = Math.max(1, settings.rerouteRadius);
        final Vec3 eye = player.getEyePosition();

        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;

        for (BlockPos candidate : BlockPos.betweenClosed(
            aimed.offset(-radius, -radius, -radius), aimed.offset(radius, radius, radius))) {
            if (!level.getBlockState(candidate).isAir()
                || burnsUs(player, candidate, settings, REROUTE_MARGIN)
                || !BaseFireBlock.canBePlacedAt(level, candidate, player.getDirection())) {
                continue;
            }

            final double score = candidate.distSqr(aimed)
                - 0.01 * Vec3.atCenterOf(candidate).distanceToSqr(eye);

            if (score < bestScore) {
                bestScore = score;
                best = candidate.immutable();
            }
        }

        return best;
    }

    private static boolean burnsUs(LocalPlayer player, BlockPos target, StevelanderConfig.FireTrail settings) {
        return burnsUs(player, target, settings, 0.0);
    }

    private static boolean burnsUs(
        LocalPlayer player,
        BlockPos target,
        StevelanderConfig.FireTrail settings,
        double margin
    ) {
        return clearance(player, settings, margin).intersects(new AABB(target));
    }

    private static AABB clearance(LocalPlayer player, StevelanderConfig.FireTrail settings, double margin) {
        return player.getBoundingBox()
            .expandTowards(player.getDeltaMovement().scale(settings.lookaheadTicks))
            .inflate(settings.safeDistance + margin);
    }

    private static boolean prepare(
        Minecraft minecraft,
        LocalPlayer player,
        BlockPos target,
        Vec3 from,
        StevelanderConfig.FireTrail settings
    ) {
        final Level level = player.level();

        if (BaseFireBlock.canBePlacedAt(level, target, player.getDirection())) {
            return true;
        }

        if (!settings.carrierBlocks) {
            return false;
        }

        final InteractionHand hand = carrier(player);
        final BlockPos below = target.below();

        if (hand == null || !level.getBlockState(below).isAir() || !inRange(player, from, below)) {
            return false;
        }

        minecraft.gameMode.useItemOn(player, hand, new BlockHitResult(
            Vec3.atCenterOf(below), Direction.UP, below, false));

        return !level.getBlockState(below).isAir();
    }

    private record Support(BlockPos pos, Direction face) {
    }

    private static Support findSupport(Level level, BlockPos fire) {
        for (Direction direction : SUPPORTS) {
            final BlockPos support = fire.relative(direction);

            if (!level.getBlockState(support).isAir()) {
                return new Support(support, direction.getOpposite());
            }
        }

        return null;
    }

    private static void place(Minecraft minecraft, LocalPlayer player, Support support, InteractionHand hand) {
        if (hand == null) {
            return;
        }

        final Vec3 point = Vec3.atCenterOf(support.pos())
            .add(Vec3.atLowerCornerOf(support.face().getUnitVec3i()).scale(0.5));

        minecraft.gameMode.useItemOn(player, hand, new BlockHitResult(point, support.face(), support.pos(), false));
    }

    private static boolean inRange(LocalPlayer player, Vec3 feet, BlockPos pos) {
        final Vec3 eye = feet.add(0.0, player.getEyeHeight(), 0.0);
        return new AABB(pos).distanceToSqr(eye) < PLACE_RANGE * PLACE_RANGE;
    }

    private static Vec3 vantage(LocalPlayer player, BlockPos pos, StevelanderConfig.FireTrail settings) {
        final Vec3 centre = Vec3.atCenterOf(pos);
        final Vec3 towards = player.position().subtract(centre);
        final Vec3 approach = towards.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : towards.normalize();

        final Vec3 grounded = search(player, pos, centre, approach, settings, true);
        return grounded != null ? grounded : search(player, pos, centre, approach, settings, false);
    }

    private static Vec3 search(
        LocalPlayer player,
        BlockPos pos,
        Vec3 centre,
        Vec3 approach,
        StevelanderConfig.FireTrail settings,
        boolean requireGround
    ) {
        for (double distance : VANTAGE_DISTANCES) {
            for (double pitch : VANTAGE_PITCHES) {
                for (double yaw : VANTAGE_YAWS) {
                    final Vec3 base = centre
                        .add(tilt(approach, yaw, pitch).scale(distance))
                        .subtract(0.0, player.getEyeHeight(), 0.0);

                    final Vec3 candidate = requireGround
                        ? settle(player, base, settings)
                        : (standable(player, base, settings) ? base : null);

                    if (candidate != null && inRange(player, candidate, pos)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private static Vec3 settle(LocalPlayer player, Vec3 feet, StevelanderConfig.FireTrail settings) {
        for (double drop = 0.0; drop <= VANTAGE_DROP; drop += 0.5) {
            final Vec3 candidate = feet.subtract(0.0, drop, 0.0);

            if (standable(player, candidate, settings) && Warp.grounded(player, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private static Vec3 tilt(Vec3 direction, double yaw, double pitch) {
        final double yawRadians = Math.toRadians(yaw);
        final double cos = Math.cos(yawRadians);
        final double sin = Math.sin(yawRadians);

        final Vec3 turned = new Vec3(
            direction.x * cos - direction.z * sin,
            direction.y,
            direction.x * sin + direction.z * cos
        );

        return turned.add(0.0, Math.tan(Math.toRadians(pitch)), 0.0).normalize();
    }

    private static boolean standable(LocalPlayer player, Vec3 feet, StevelanderConfig.FireTrail settings) {
        final AABB box = player.getBoundingBox().move(feet.subtract(player.position()));

        if (!player.level().noCollision(player, box)) {
            return false;
        }

        return !settings.avoidDamage || !hazardous(player.level(), box);
    }

    private static boolean hazardous(Level level, AABB box) {
        for (BlockPos pos : BlockPos.betweenClosed(box)) {
            if (burns(level, pos)) {
                return true;
            }
        }

        return false;
    }

    private static boolean burns(Level level, BlockPos pos) {
        final var block = level.getBlockState(pos).getBlock();

        if (block instanceof BaseFireBlock || block instanceof MagmaBlock || block instanceof CampfireBlock) {
            return true;
        }

        final var fluid = level.getFluidState(pos).getType();
        return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
    }

    private static Held acquireIgniter(Minecraft minecraft, LocalPlayer player, StevelanderConfig.FireTrail settings) {
        final Held charge = acquire(minecraft, player, stack -> stack.is(Items.FIRE_CHARGE));
        if (charge != null) {
            return charge;
        }

        return acquire(minecraft, player, stack -> hasSteelLeft(stack, settings));
    }

    private static boolean hasSteelLeft(ItemStack stack, StevelanderConfig.FireTrail settings) {
        if (!stack.is(Items.FLINT_AND_STEEL)) {
            return false;
        }

        return stack.getMaxDamage() - stack.getDamageValue() > settings.durabilityReserve;
    }

    private static InteractionHand carrier(LocalPlayer player) {
        if (player.getOffhandItem().getItem() instanceof BlockItem) {
            return InteractionHand.OFF_HAND;
        }

        return player.getMainHandItem().getItem() instanceof BlockItem ? InteractionHand.MAIN_HAND : null;
    }

    private record Held(InteractionHand hand, int restoreSlot, int storageSlot, int borrowedSlot) {
    }

    private static Held acquire(Minecraft minecraft, LocalPlayer player, Predicate<ItemStack> match) {
        if (match.test(player.getOffhandItem())) {
            return new Held(InteractionHand.OFF_HAND, Inventories.NOT_FOUND,
                Inventories.NOT_FOUND, Inventories.NOT_FOUND);
        }

        if (match.test(player.getMainHandItem())) {
            return new Held(InteractionHand.MAIN_HAND, Inventories.NOT_FOUND,
                Inventories.NOT_FOUND, Inventories.NOT_FOUND);
        }

        final int found = Inventories.find(player, match);
        if (found == Inventories.NOT_FOUND) {
            return null;
        }

        final int selected = player.getInventory().getSelectedSlot();

        if (Inventories.inHotbar(found)) {
            Inventories.select(player, found);
            return new Held(InteractionHand.MAIN_HAND, selected, Inventories.NOT_FOUND, Inventories.NOT_FOUND);
        }

        final int borrowed = Inventories.spareHotbarSlot(player);
        if (!Inventories.swap(minecraft, player, found, borrowed)) {
            return null;
        }

        Inventories.select(player, borrowed);
        return new Held(InteractionHand.MAIN_HAND, selected, found, borrowed);
    }

    private static void release(Minecraft minecraft, LocalPlayer player, Held held) {
        if (held.storageSlot() != Inventories.NOT_FOUND) {
            Inventories.swap(minecraft, player, held.storageSlot(), held.borrowedSlot());
        }

        if (held.restoreSlot() != Inventories.NOT_FOUND) {
            Inventories.select(player, held.restoreSlot());
        }
    }
}
