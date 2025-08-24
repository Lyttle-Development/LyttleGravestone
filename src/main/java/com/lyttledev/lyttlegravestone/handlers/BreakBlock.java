package com.lyttledev.lyttlegravestone.handlers;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.utils.GravestoneManager;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.lyttledev.lyttleutils.utils.convertion.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.lyttledev.lyttleutils.utils.entity.Player.getDisplayName;


public class BreakBlock implements Listener {
    private final LyttleGravestone plugin;

    public BreakBlock(LyttleGravestone plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();

        if (block.getType() == Material.MOSSY_STONE_BRICK_STAIRS && GravestoneManager.getGravestone(location)) {
            try {
                String[] values = GravestoneDatabase.getGravestone(location);
                UUID graveOwnerUUID = UUID.fromString(values[0]);
                Player graveOwnerPlayer = Bukkit.getPlayer(graveOwnerUUID);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(graveOwnerUUID);
                String graveOwnerName = graveOwnerPlayer != null ? getDisplayName(graveOwnerPlayer) : offlinePlayer.getName();

                if (!graveOwnerUUID.equals(player.getUniqueId()) && !player.hasPermission("lyttlegravestone.Staff")) {
                    Replacements replacements = new Replacements.Builder()
                        .add("<PLAYER>", graveOwnerName)
                        .build();

                    plugin.message.sendMessage(player, "wrong_player", replacements);
                    event.setCancelled(true);
                    return;
                }

                destroyGravestone(block, values[1]);

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to handle gravestone destruction: " + e.getMessage());
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        preventGravestoneDestruction(event.blockList());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        preventGravestoneDestruction(event.blockList());
    }

    private void preventGravestoneDestruction(List<Block> blocks) {
        Iterator<Block> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (block.getType() == Material.MOSSY_STONE_BRICK_STAIRS && GravestoneManager.getGravestone(block.getLocation())) {
                iterator.remove(); // Prevent explosion from breaking it
            }
        }
    }

    private void destroyGravestone(Block block, String serializedInventory) {
        Location location = block.getLocation();

        ItemStack[] inventory = ItemSerializer.deserializeInventory(serializedInventory, 0);
        if (inventory != null) {
            for (ItemStack item : inventory) {
                if (item != null) {
                    block.getWorld().dropItemNaturally(location, item);
                }
            }
        }

        block.setType(Material.AIR);

        try {
            GravestoneDatabase.deleteGravestone(location);
            GravestoneManager.deleteGravestone(location);
        } catch (SQLException exception) {
            exception.printStackTrace();
            plugin.getLogger().severe("Failed to delete gravestone from database: " + exception.getMessage());
        }
    }
}
