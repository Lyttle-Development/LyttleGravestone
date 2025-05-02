package com.lyttledev.lyttlegravestone;

import com.lyttledev.lyttlegravestone.commands.RetrieveGraveStoneCommand;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.listeners.*;
import com.lyttledev.lyttlegravestone.types.Configs;
import com.lyttledev.lyttlegravestone.utils.Console;
import com.lyttledev.lyttlegravestone.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.sql.SQLException;

public final class LyttleGravestone extends JavaPlugin {
    private GravestoneDatabase gravestoneDatabase;
    private Economy economy;
    public Configs config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Setup config after creating the configs
        config = new Configs(this);
        // Migrate config
        migrateConfig();

        // Vault logic
        Boolean command = (Boolean) config.general.get("retrieve_command_active");
        Boolean vault = (Boolean) config.general.get("use_vault");
        if (command && vault) {
            if (!setupEconomy()) {
                getLogger().severe("Vault or an economy plugin is not installed!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        // Plugin startup logic
        Message.init(this);
        Console.init(this);

        // Register the listeners
        new Death(this);
        new RightClick(this);
        new BreakBlock(this);
        new GuiClose(this);
        new InventoryClick(this);
        new InventoryDrag(this);

        // Register the commands
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            this.registerCommands(commands);
        });

        // Create the database connection
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            gravestoneDatabase = new GravestoneDatabase(this);
            GravestoneDatabase.initGravestonesCache();
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.out.println("Failed to connect to the database! " + exception.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        try {
            if (gravestoneDatabase != null) {
                gravestoneDatabase.closeConnection();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void registerCommands(Commands commands) {
        Boolean command = (Boolean) config.general.get("retrieve_command_active");
        if (command) {
            RetrieveGraveStoneCommand.register(this, commands);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    @Override
    public void saveDefaultConfig() {
        String configPath = "config.yml";
        if (!new File(getDataFolder(), configPath).exists())
            saveResource(configPath, false);

        String messagesPath = "messages.yml";
        if (!new File(getDataFolder(), messagesPath).exists())
            saveResource(messagesPath, false);

        // Defaults:
        String defaultPath = "#defaults/";
        String defaultGeneralPath =  defaultPath + configPath;
        saveResource(defaultGeneralPath, true);

        String defaultMessagesPath =  defaultPath + messagesPath;
        saveResource(defaultMessagesPath, true);
    }

    private void migrateConfig() {
        if (!config.general.contains("config_version")) {
            config.general.set("config_version", 0);
        }

        // TODO Do this thing down here
        switch (config.general.get("config_version").toString()) {
            case "0":
                // Migrate config entries.
                config.messages.set("prefix", config.defaultMessages.get("prefix"));
                config.messages.set("no_permission", config.defaultMessages.get("no_permission"));
                config.messages.set("player_not_found", config.defaultMessages.get("player_not_found"));
                config.messages.set("must_be_player", config.defaultMessages.get("must_be_player"));
                config.messages.set("message_not_found", config.defaultMessages.get("message_not_found"));
                config.messages.set("wrong_player", config.defaultMessages.get("wrong_player"));
                config.messages.set("retrieve_price_changed", config.defaultMessages.get("retrieve_price_changed"));
                config.messages.set("no_gravestone_found", config.defaultMessages.get("no_gravestone_found"));
                config.messages.set("not_enough_money", config.defaultMessages.get("not_enough_money"));
                config.messages.set("death_message", config.defaultMessages.get("death_message"));
                config.messages.set("retrieve_confirm", config.defaultMessages.get("retrieve_confirm"));
                config.general.set("retrieve_command_active", config.defaultGeneral.get("retrieve_command_active"));
                config.general.set("use_vault", config.defaultGeneral.get("use_vault"));

                // Update config version.
                config.general.set("config_version", 1);

                // Recheck if the config is fully migrated.
                migrateConfig();
                break;
            case "1":
                // Migrate config entries.
                config.messages.set("death_message_no_delivery",  config.defaultMessages.get("death_message_no_delivery"));

                // Update config version.
                config.general.set("config_version", 2);

                // Recheck if the config is fully migrated.
                migrateConfig();
            default:
                break;
        }
    }
}
