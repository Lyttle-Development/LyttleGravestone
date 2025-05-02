package com.lyttledev.lyttlegravestone.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Memory {

    // Keeps track of the gravestones that are being looked at
    private static final HashMap<Player, Location> openGravestones = new HashMap<>();

    public static void openGravestone(Player player, Location location) { openGravestones.put(player, location); }

    public static boolean checkForGravestone(Location location) { return openGravestones.containsValue(location); }

    public static void closeGravestone(Player player, Location location) { openGravestones.remove(player,location); }

    public static Location getGravestoneLocation(Player player) { return openGravestones.get(player); }


    // Keeps track of all the gravestones in existence
    private static final List<Location> gravestones = new ArrayList<>();

    public static void addGravestone(Location location) { gravestones.add(location); }

    public static boolean getGravestone(Location location) { return findGravestone(location) >= 0; }

    public static void deleteGravestone(Location location) {
        int index = findGravestone(location);
        if (index < 0) { return; }
        gravestones.remove(index);
    }

    private static int findGravestone(Location location) {
        for (int index = 0; index < gravestones.size(); index++) {
            if (BlockLocation.isSameBlockLocation(gravestones.get(index), location)) { return index; }
        }
        return -1;
    }


    // Keeps track of deliveries in progress
    private static final List<Player> deliveriesInProgress = new ArrayList<>();

    public static void addDelivery(Player player) { deliveriesInProgress.add(player); }

    public static void removeDelivery(Player player) { deliveriesInProgress.remove(player); }

    public static boolean checkDelivery(Player player) {
        for (Player playerEntry : deliveriesInProgress) {
            if (playerEntry.equals(player)) { return true; }
        }
        return false;
    }

}