package com.lyttledev.lyttlegravestone.utils;

import com.lyttledev.lyttleutils.utils.location.BlockLocation;
import com.lyttledev.lyttleutils.utils.storage.Memory;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Centralized memory management for gravestones and deliveries.
 */
public class GravestoneManager {

    // One gravestone "open" per player
    private static final Memory<Player, Location> openGravestones = new Memory<>();

    public static void openGravestone(Player player, Location location) {
        openGravestones.addValue(player, location);
    }

    public static boolean checkForGravestone(Location location) {
        for (Location loc : openGravestones.getAllValues()) {
            if (BlockLocation.isSameBlockLocation(loc, location)) {
                return true;
            }
        }
        return false;
    }

    public static void closeGravestone(Player player, Location location) {
        Location current = (Location) openGravestones.getValue(player);
        if (current != null && BlockLocation.isSameBlockLocation(current, location)) {
            openGravestones.removeValue(player);
        }
    }

    public static Location getGravestoneLocation(Player player) {
        return (Location) openGravestones.getValue(player);
    }

    // All gravestones in the world
    private static final List<Location> gravestones = new ArrayList<>();

    public static void addGravestone(Location location) {
        gravestones.add(location);
    }

    public static boolean getGravestone(Location location) {
        return findGravestone(location) >= 0;
    }

    public static void deleteGravestone(Location location) {
        int index = findGravestone(location);
        if (index >= 0) {
            gravestones.remove(index);
        }
    }

    private static int findGravestone(Location location) {
        for (int i = 0; i < gravestones.size(); i++) {
            if (BlockLocation.isSameBlockLocation(gravestones.get(i), location)) {
                return i;
            }
        }
        return -1;
    }

    // Deliveries in progress
    private static final Memory<UUID, Boolean> deliveriesInProgress = new Memory<>();

    public static void addDelivery(UUID uuid) {
        deliveriesInProgress.addValue(uuid, true);
    }

    public static void removeDelivery(UUID uuid) {
        deliveriesInProgress.removeValue(uuid);
    }

    public static boolean checkDelivery(UUID player) {
        return deliveriesInProgress.hasValue(player);
    }
}
