package com.lyttledev.lyttlegravestone;

import com.lyttledev.lyttlegravestone.commands.RetrieveGraveStoneCommand;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.listeners.*;
import com.lyttledev.lyttlegravestone.types.Configs;
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
        RetrieveGraveStoneCommand.register(this, commands);
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
//            case "0":
//                // Migrate config entries.
//                config.messages.set("prefix", config.defaultMessages.get("prefix"));
//
//                // Update config version.
//                config.general.set("config_version", 1);
//
//                // Recheck if the config is fully migrated.
//                migrateConfig();
//                break;
            default:
                break;
        }
    }
}
