package com.lyttledev.lyttlegravestone.listeners;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.utils.GravestoneManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class GuiClose implements Listener {

    public GuiClose(LyttleGravestone plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent event) {
        String title = event.getView().title().toString();
        if (!title.contains("'s gravestone")) { return; }

        ItemStack[] gravestoneInventory = event.getInventory().getContents();

        // TODO Cut feature for release, more time needed
//        try {
//            String[] values = GravestoneDatabase.getGravestone(location);
//            String DatabaseInventory = values[1];
//            ItemStack[] originalGravestoneInventory = ItemSerializer.deserializeInventory(DatabaseInventory, 0);
//
//            // Check for new items
//            for (int i = 0; i < gravestoneInventory.length; i++) {
//                if (
//                        gravestoneInventory[i] != null && ( // Slot has item.
//                                originalGravestoneInventory[i] == null || // Slot did not have item before.
//                                        !originalGravestoneInventory[i].isSimilar(gravestoneInventory[i]) || // Item is not the same
//                                        originalGravestoneInventory[i].getAmount() != gravestoneInventory[i].getAmount()) // Item amount is not the same
//                ) {
//                    // Drop the new item(s)
//                    ItemStack newItem = gravestoneInventory[i].clone();
//                    gravestoneInventory[i] = null;
//                    location.getWorld().dropItem(location.add(0.5, 1.0, 0.5), newItem);
//                }
//            }
//        } catch (SQLException exception) {
//            Bukkit.getLogger().warning("Whelp, an error happened: " + exception.getMessage());
//        }


        Player player = (Player) event.getPlayer();
        Location location = GravestoneManager.getGravestoneLocation(player);
        GravestoneManager.closeGravestone(player, location);

        if (location == null) { return; }

        boolean isEmpty = true;
        for (ItemStack item : gravestoneInventory) {
            if (item != null) {
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) {
            // KIL KIT
            try {
                GravestoneDatabase.deleteGravestone(location);
                location.getBlock().setType(Material.AIR);
                GravestoneManager.deleteGravestone(location);
            } catch (SQLException exception) {
                exception.printStackTrace();
                System.out.println("Failed to delete the database entry! " + exception.getMessage());
            }
        } else {
            // UPDATE THE ENTRY
            try {
                GravestoneDatabase.updateGravestone(gravestoneInventory, location);
            } catch (SQLException exception) {
                exception.printStackTrace();
                System.out.println("Failed to update the database entry! " + exception.getMessage());
            }
        }

    }

}
