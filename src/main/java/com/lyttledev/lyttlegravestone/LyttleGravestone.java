package com.lyttledev.lyttlegravestone;

import com.lyttledev.lyttlegravestone.commands.GravestoneListCommand;
import com.lyttledev.lyttlegravestone.commands.LyttleGravestoneCommand;
import com.lyttledev.lyttlegravestone.commands.RetrieveGraveStoneCommand;
import com.lyttledev.lyttlegravestone.database.GravestoneDatabase;
import com.lyttledev.lyttlegravestone.handlers.*;
import com.lyttledev.lyttlegravestone.types.Configs;
import com.lyttledev.lyttleutils.utils.communication.Console;
import com.lyttledev.lyttleutils.utils.communication.Message;
import com.lyttledev.lyttleutils.utils.storage.GlobalConfig;
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
    public ListHandler listHandler;
    public Configs config;
    public Console console;
    public Message message;
    public GlobalConfig global;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Setup config after creating the configs
        this.config = new Configs(this);
        this.global = new GlobalConfig(this);
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
        this.console = new Console(this);
        this.message = new Message(this, config.messages, global);

        // Register the handlers
        new Death(this);
        new RightClick(this);
        new BreakBlock(this);
        new GuiClose(this);
        new InventoryClick(this);
        new InventoryDrag(this);
        this.listHandler = new ListHandler(this);

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
            // Retrieve command
            RetrieveGraveStoneCommand.register(this, commands);
        }
        // Lyttle gravestone command
        LyttleGravestoneCommand.register(this, commands);
        GravestoneListCommand.register(this, commands);
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
            case "2":
                // Migrate config entries.
                config.general.set("retrieve_command_blocks", config.defaultGeneral.get("retrieve_command_blocks"));
                config.general.set("retrieve_command_price", config.defaultGeneral.get("retrieve_command_price"));
                config.general.set("retrieve_command_price__other_world", config.defaultGeneral.get("retrieve_command_price__other_world"));

                // Update config version.
                config.general.set("config_version", 3);

                // Recheck if the config is fully migrated.
                migrateConfig();
            case "3":
                // Migrate config entries.
                config.messages.set("retrieve_confirmed", config.defaultMessages.get("retrieve_confirmed"));

                // Update config version.
                config.general.set("config_version", 4);

                // Recheck if the config is fully migrated.
                migrateConfig();
            default:
                break;
        }
    }
}
