package com.lyttledev.lyttlegravestone.types;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GravestoneListInventory implements InventoryHolder {
    private Inventory inventory;

    public GravestoneListInventory(LyttleGravestone plugin, String playerName, List<String> locations) {
        this.inventory = plugin.getServer().createInventory(this, 54, playerName + "'s list of gravestones");
        setInventory(locations);
    }

    private void setInventory(List<String> locations) {
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
            this.inventory.addItem(item);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

}
