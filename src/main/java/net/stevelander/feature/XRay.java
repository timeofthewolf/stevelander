package net.stevelander.feature;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class XRay {
    private static final Set<Block> VISIBLE_BLOCKS = Set.of(
        Blocks.COAL_ORE,
        Blocks.COPPER_ORE,
        Blocks.DIAMOND_ORE,
        Blocks.EMERALD_ORE,
        Blocks.GOLD_ORE,
        Blocks.IRON_ORE,
        Blocks.LAPIS_ORE,
        Blocks.REDSTONE_ORE,

        Blocks.DEEPSLATE_COAL_ORE,
        Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.DEEPSLATE_IRON_ORE,
        Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DEEPSLATE_REDSTONE_ORE,

        Blocks.COAL_BLOCK,
        Blocks.DIAMOND_BLOCK,
        Blocks.EMERALD_BLOCK,
        Blocks.GOLD_BLOCK,
        Blocks.IRON_BLOCK,
        Blocks.LAPIS_BLOCK,
        Blocks.REDSTONE_BLOCK,
        Blocks.RAW_COPPER_BLOCK,
        Blocks.RAW_GOLD_BLOCK,
        Blocks.RAW_IRON_BLOCK,

        Blocks.ANCIENT_DEBRIS,
        Blocks.NETHER_GOLD_ORE,
        Blocks.NETHER_QUARTZ_ORE,
        Blocks.NETHERITE_BLOCK,

        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.ENDER_CHEST,
        Blocks.BARREL,
        Blocks.SHULKER_BOX,
        Blocks.HOPPER,
        Blocks.DISPENSER,
        Blocks.DROPPER,

        Blocks.SPAWNER,
        Blocks.BEACON,
        Blocks.ENCHANTING_TABLE,
        Blocks.DRAGON_EGG,
        Blocks.END_PORTAL,
        Blocks.END_PORTAL_FRAME,
        Blocks.NETHER_PORTAL,
        Blocks.TNT,

        Blocks.LAVA,
        Blocks.WATER
    );

    private static boolean enabled;

    private XRay() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            minecraft.levelExtractor.allChanged();
        }
    }

    public static boolean shouldRenderFace(boolean original, BlockState state, BlockState otherState, Direction side) {
        if (!enabled) {
            return original;
        }

        return VISIBLE_BLOCKS.contains(state.getBlock());
    }
}
