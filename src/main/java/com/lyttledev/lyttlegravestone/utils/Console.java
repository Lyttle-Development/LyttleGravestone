package com.lyttledev.lyttlegravestone.utils;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class Console {
    public static LyttleGravestone plugin;

    public static void init(LyttleGravestone plugin) {
        Console.plugin = plugin;
    }

    public static void run(String command) {
        if (command == null || command.isEmpty()) return;
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Bukkit.getScheduler().callSyncMethod( plugin, () -> Bukkit.dispatchCommand( console, command ) );
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }
}
