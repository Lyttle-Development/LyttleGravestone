package com.lyttledev.lyttlegravestone.types;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GravestoneInventory implements InventoryHolder {
    private Inventory inventory;

    public GravestoneInventory(LyttleGravestone plugin, String playerName) {
        this.inventory = plugin.getServer().createInventory(this, 54, playerName + "'s gravestone");
    }

    public void setGravestone(ItemStack[] inventory) {
        this.inventory.setContents(inventory);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
