package com.lyttledev.lyttlegravestone.listeners;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.utils.ItemSerializer;
import com.lyttledev.lyttlegravestone.utils.Memory;
import com.lyttledev.lyttlegravestone.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.UUID;

import static com.lyttledev.lyttlegravestone.utils.DisplayName.getDisplayName;

public class BreakBlock implements Listener {

    public BreakBlock(LyttleGravestone plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) throws SQLException {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();

        if (block.getType().equals(Material.MOSSY_STONE_BRICK_STAIRS)) {
            if (!Memory.getGravestone(location)) { return; }

            String[] values = GravestoneDatabase.getGravestone(location);

            // UUID and player related logic
            String graveOwnerString = values[0];
            Player graveOwnerPlayer = Bukkit.getPlayer(UUID.fromString(graveOwnerString));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(graveOwnerString));
            String graveOwnerName = graveOwnerPlayer != null ? graveOwnerPlayer.getName() : offlinePlayer.getName();

            // Permission logic
            if (player != graveOwnerPlayer && !player.hasPermission("lyttlegravestone.Staff")) {
                String[][] replacements = {{"<PLAYER>", getDisplayName(player)}};
                Message.sendMessage(player, "wrong_player", replacements);
                event.setCancelled(true);
                return;
            }

            // Inventory logic
            String DatabaseInventory = values[1];
            ItemStack[] inventory = ItemSerializer.deserializeInventory(DatabaseInventory, 0);

            // Drop the inventory
            if (inventory != null) {
                for (ItemStack item : inventory) {
                    if (item != null) {
                        block.getWorld().dropItemNaturally(location, item);
                    }
                }
            }

            // Make sure the stairs are gone at this point
            block.setType(Material.AIR);

            try {
                GravestoneDatabase.deleteGravestone(location);
                Memory.deleteGravestone(location);
            } catch (SQLException exception) {
                exception.printStackTrace();
                System.out.println("Failed to delete the database entry! " + exception.getMessage());
            }
        }
    }
}
