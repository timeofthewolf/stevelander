package net.stevelander.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stevelander.Stevelander;
import net.stevelander.StevelanderConfig;

public final class AirPlace {

    private AirPlace() {
    }

    public static HitResult adjust(Minecraft minecraft, HitResult original) {
        final StevelanderConfig.AirPlace settings = Stevelander.config().airPlace;
        if (!settings.enabled) {
            return original;
        }

        if (!(original instanceof BlockHitResult hit) || original.getType() != HitResult.Type.MISS) {
            return original;
        }

        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || player.isSpectator()) {
            return original;
        }

        BlockHitResult target = hit;

        if (settings.customRange) {
            final Vec3 eye = player.getEyePosition();
            final Vec3 direction = hit.getLocation().subtract(eye).normalize();
            final Vec3 point = eye.add(direction.scale(settings.range));
            target = new BlockHitResult(point, hit.getDirection(), BlockPos.containing(point), hit.isInside());
        }

        if (!isAirOrFluid(minecraft, settings, target.getBlockPos())) {
            return original;
        }

        if (!canPlaceAt(minecraft, player, target)) {
            return original;
        }

        return new BlockHitResult(
            target.getLocation(),
            target.getDirection(),
            target.getBlockPos(),
            target.isInside()
        );
    }

    private static boolean isAirOrFluid(Minecraft minecraft, StevelanderConfig.AirPlace settings, BlockPos pos) {
        if (minecraft.level.getBlockState(pos).isAir()) {
            return true;
        }

        return settings.placeInLiquid && !minecraft.level.getFluidState(pos).isEmpty();
    }

    private static boolean canPlaceAt(Minecraft minecraft, LocalPlayer player, BlockHitResult hit) {
        return isPlaceable(minecraft, player.getMainHandItem(), hit)
            || isPlaceable(minecraft, player.getOffhandItem(), hit);
    }

    private static boolean isPlaceable(Minecraft minecraft, ItemStack stack, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState().canSurvive(minecraft.level, hit.getBlockPos());
        }

        return stack.getItem() instanceof SpawnEggItem || stack.getItem() instanceof ArmorStandItem;
    }
}
