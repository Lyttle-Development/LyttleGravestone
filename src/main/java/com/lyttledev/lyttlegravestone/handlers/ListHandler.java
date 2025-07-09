package com.lyttledev.lyttlegravestone.handlers;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ListHandler implements Listener {
    private final LyttleGravestone plugin;

    public ListHandler(LyttleGravestone plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGui(Player player, List<String> locations) {
        Inventory gravestones = Bukkit.createInventory(player, 54, Component.text(player.getName() + "'s list of gravestones"));

        for (String location : locations) {
            String[] data = location.split(",");

            ItemStack item = new ItemStack(Material.STONE);
            ItemMeta itemMeta = item.getItemMeta();

            switch (data[0]) {
                case "world":
                    item.setType(Material.GRASS_BLOCK);
                    itemMeta.displayName(Component.text("Overworld"));
                    break;
                case "world_nether":
                    item.setType(Material.NETHERRACK);
                    itemMeta.displayName(Component.text("Nether"));
                    break;
                case "world_the_end":
                    item.setType(Material.END_STONE);
                    itemMeta.displayName(Component.text("The End"));
                    break;
                default:
                    itemMeta.displayName(Component.text(data[0]));
                    break;
            }

            item.setItemMeta(itemMeta);
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("X:" + data[1] + " Y:" + data[2] + " Z:"  + data[3]));
            item.lore(lore);
            gravestones.addItem(item);
        }

        player.openInventory(gravestones);
    }

}
