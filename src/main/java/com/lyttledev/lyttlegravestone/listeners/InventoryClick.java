package com.lyttledev.lyttlegravestone.listeners;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.utils.Memory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClick implements Listener {

    public InventoryClick(LyttleGravestone plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().title().toString();
        if (!title.contains("'s gravestone")) { return; }

        Inventory clickedInventory = event.getClickedInventory();
        Inventory playerInventory = event.getWhoClicked().getInventory();

        int inventorySize = event.getInventory().getSize();
        if (inventorySize != 54) { return; }

        Player player = (Player) event.getWhoClicked();
        Location location = Memory.getGravestoneLocation(player);
        if (location == null) { return; }

        if (event.getClick().isShiftClick()) {
            if (playerInventory == clickedInventory) {
                event.setCancelled(true);
            }
        }

        if (playerInventory != clickedInventory) {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem.getType() != Material.AIR) {
                event.setCancelled(true);
            }
        }

    }
}
