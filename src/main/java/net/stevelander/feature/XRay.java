package net.stevelander.feature;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.stevelander.Stevelander;

public final class XRay {

    private static volatile boolean enabled;
    private static Set<Block> visibleBlocks;

    private XRay() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        reload();
    }

    public static void invalidate() {
        visibleBlocks = null;
        reload();
    }

    private static void reload() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            minecraft.levelExtractor.allChanged();
        }
    }

    public static Set<Block> visibleBlocks() {
        Set<Block> blocks = visibleBlocks;
        if (blocks != null) {
            return blocks;
        }

        blocks = new HashSet<>();
        for (String id : Stevelander.config().xray.blocks) {
            final Identifier identifier = Identifier.tryParse(id);
            if (identifier == null) {
                continue;
            }

            final Block block = BuiltInRegistries.BLOCK.getValue(identifier);
            if (block != Blocks.AIR) {
                blocks.add(block);
            }
        }

        visibleBlocks = blocks;
        return blocks;
    }

    public static boolean shouldRenderFace(boolean original, BlockState state, BlockState otherState, Direction side) {
        if (!enabled) {
            return original;
        }

        return visibleBlocks().contains(state.getBlock());
    }
}
