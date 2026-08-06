package net.stevelander.feature;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public final class Inventories {

    public static final int NOT_FOUND = -1;

    private Inventories() {
    }

    public static int find(LocalPlayer player, Predicate<ItemStack> match) {
        final Inventory inventory = player.getInventory();

        for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
            if (match.test(inventory.getItem(slot))) {
                return slot;
            }
        }

        for (int slot = Inventory.SELECTION_SIZE; slot < InventoryMenu.INV_SLOT_END; slot++) {
            if (match.test(inventory.getItem(slot))) {
                return slot;
            }
        }

        return NOT_FOUND;
    }

    public static boolean inHotbar(int slot) {
        return slot >= 0 && slot < Inventory.SELECTION_SIZE;
    }

    public static void select(LocalPlayer player, int hotbarSlot) {
        player.getInventory().setSelectedSlot(hotbarSlot);

        if (player.connection != null) {
            player.connection.send(new ServerboundSetCarriedItemPacket(hotbarSlot));
        }
    }

    public static int spareHotbarSlot(LocalPlayer player) {
        final Inventory inventory = player.getInventory();

        for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                return slot;
            }
        }

        return inventory.getSelectedSlot();
    }

    public static boolean swap(Minecraft minecraft, LocalPlayer player, int storageSlot, int hotbarSlot) {
        if (player.containerMenu != player.inventoryMenu || inHotbar(storageSlot)) {
            return false;
        }

        minecraft.gameMode.handleContainerInput(
            player.inventoryMenu.containerId, storageSlot, hotbarSlot, ContainerInput.SWAP, player);

        return true;
    }
}
