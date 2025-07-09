package com.lyttledev.lyttlegravestone.handlers;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.utils.GravestoneManager;
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
    private static LyttleGravestone plugin;

    public InventoryClick(LyttleGravestone plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().title().toString();
        Player player = (Player) event.getWhoClicked();

        if (!title.contains("'s gravestone") && !title.contains("'s list of gravestones")) { return; }

        Inventory clickedInventory = event.getClickedInventory();
        Inventory playerInventory = event.getWhoClicked().getInventory();

        int inventorySize = event.getInventory().getSize();
        if (inventorySize != 54) { return; }

        Location location = GravestoneManager.getGravestoneLocation(player);
        if (location == null && !title.contains("'s list of gravestones")) { return; }

        if (title.contains("'s list of gravestones")) {
            event.setCancelled(true);
        }

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
