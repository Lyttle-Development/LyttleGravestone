package com.lyttledev.lyttlegravestone.listeners;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.utils.ItemSerializer;
import com.lyttledev.lyttlegravestone.utils.Memory;
import com.lyttledev.lyttlegravestone.utils.Message;
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

import static com.lyttledev.lyttlegravestone.utils.DisplayName.getDisplayName;

public class RightClick implements Listener {

    public RightClick(LyttleGravestone plugin) {
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

        if (!Memory.getGravestone(location)) { return; }

        try {
            String[] values = GravestoneDatabase.getGravestone(location);

            // UUID and player related logic
            String graveOwnerString = values[0];
            Player graveOwnerPlayer = Bukkit.getPlayer(UUID.fromString(graveOwnerString));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(graveOwnerString));
            String graveOwnerName = graveOwnerPlayer != null ? getDisplayName(graveOwnerPlayer) : offlinePlayer.getName();

            // Permission logic
            if (player != graveOwnerPlayer && !player.hasPermission("lyttlegravestone.Staff")) {
                String[][] replacements = {{"<PLAYER>", graveOwnerName}};
                Message.sendMessage(player, "wrong_player", replacements);
                return;
            }

            // Inventory logic
            String DatabaseInventory = values[1];
            inventory = ItemSerializer.deserializeInventory(DatabaseInventory, 0);

            if (Memory.checkForGravestone(location)) { return; }

            Memory.openGravestone(player, location);
            openGui(player, inventory, graveOwnerName);

        } catch (SQLException exception) {
            exception.printStackTrace();
            System.out.println("Failed to get the database entry! " + exception.getMessage());
        }

    }

    private void openGui(Player player, ItemStack[] items, String graveOwnerName) {
        Inventory gravestoneInventory = Bukkit.createInventory(player, 54, Component.text(graveOwnerName + "'s gravestone"));
        gravestoneInventory.setContents(items);
        player.openInventory(gravestoneInventory);
    }

}