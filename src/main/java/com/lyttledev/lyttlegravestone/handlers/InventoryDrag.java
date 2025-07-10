package com.lyttledev.lyttlegravestone.handlers;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.inventories.GravestoneInventory;
import com.lyttledev.lyttlegravestone.inventories.GravestoneListInventory;
import com.lyttledev.lyttlegravestone.utils.GravestoneManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryDrag implements Listener {
    private static LyttleGravestone plugin;

    public InventoryDrag(LyttleGravestone plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof GravestoneInventory)
                && !(inventory.getHolder(false) instanceof GravestoneListInventory) ) { return; }

        int inventorySize = event.getInventory().getSize();
        if (inventorySize != 54) { return; }

        Location location = GravestoneManager.getGravestoneLocation(player);
        if (location == null && !(inventory.getHolder(false) instanceof GravestoneListInventory)) { return; }

        ItemStack draggedItem = event.getOldCursor();
        if (draggedItem.getType() == Material.AIR) { return; }

        for (int slot : event.getRawSlots()) {
            if (slot < inventorySize) {
                event.setCancelled(true);
                break;
            }
        }
    }
}