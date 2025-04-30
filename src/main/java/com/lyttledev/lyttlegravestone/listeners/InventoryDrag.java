package com.lyttledev.lyttlegravestone.listeners;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.utils.Memory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryDrag implements Listener {

    public InventoryDrag(LyttleGravestone plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().title().toString();
        if (!title.contains("'s gravestone")) { return; }

        int inventorySize = event.getInventory().getSize();
        if (inventorySize != 54) { return; }

        Player player = (Player) event.getWhoClicked();
        Location location = Memory.getGravestoneLocation(player);
        if (location == null) { return; }

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