package com.lyttledev.lyttlegravestone.handlers;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.inventories.GravestoneInventory;
import com.lyttledev.lyttlegravestone.utils.GravestoneManager;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.lyttledev.lyttleutils.utils.convertion.ItemSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.UUID;

import static com.lyttledev.lyttleutils.utils.entity.Player.getDisplayName;


public class RightClick implements Listener {
    private final LyttleGravestone plugin;

    public RightClick(LyttleGravestone plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) { return; }

        Block block = event.getClickedBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();
        ItemStack[] inventory;

        if (!block.getType().equals(Material.MOSSY_STONE_BRICK_STAIRS)) { return; }

        if (!GravestoneManager.getGravestone(location)) { return; }

        try {
            String[] values = GravestoneDatabase.getGravestone(location);

            // UUID and player related logic
            String graveOwnerString = values[0];
            Player graveOwnerPlayer = Bukkit.getPlayer(UUID.fromString(graveOwnerString));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(graveOwnerString));
            String graveOwnerName = graveOwnerPlayer != null ? getDisplayName(graveOwnerPlayer) : offlinePlayer.getName();

            // Permission logic
            if (player != graveOwnerPlayer && !player.hasPermission("lyttlegravestone.Staff")) {
                Replacements replacements = new Replacements.Builder()
                        .add("<PLAYER>", graveOwnerName)
                        .build();

                plugin.message.sendMessage(player, "wrong_player", replacements);
                return;
            }

            // Inventory logic
            String DatabaseInventory = values[1];
            inventory = ItemSerializer.deserializeInventory(DatabaseInventory, 0);

            if (GravestoneManager.checkForGravestone(location)) { return; }

            GravestoneManager.openGravestone(player, location);
            GravestoneInventory gravestoneInventoryClass = new GravestoneInventory(plugin, player.getName());
            gravestoneInventoryClass.setGravestone(inventory);

            Inventory gravestoneInventory = gravestoneInventoryClass.getInventory();
            player.openInventory(gravestoneInventory);

        } catch (SQLException exception) {
            exception.printStackTrace();
            plugin.getLogger().severe("Failed to get the database entry! " + exception.getMessage());
        }
    }
}