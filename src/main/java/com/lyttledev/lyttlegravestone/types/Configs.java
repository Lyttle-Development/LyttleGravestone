package com.lyttledev.lyttlegravestone.types;

import com.lyttledev.lyttlegravestone.LyttleGravestone;
import com.lyttledev.lyttleutils.types.YamlConfig;

public class Configs {
    private final LyttleGravestone plugin;

    // Configs
    public YamlConfig general;
    public YamlConfig messages;

    // Default configs
    public YamlConfig defaultMessages;
    public YamlConfig defaultGeneral;


    public Configs(LyttleGravestone plugin) {
        this.plugin = plugin;

        // Configs
        general = new YamlConfig(plugin, "config.yml");
        messages = new YamlConfig(plugin, "messages.yml");

        // Default configs
        defaultMessages = new YamlConfig(plugin, "#defaults/messages.yml");
        defaultGeneral = new YamlConfig(plugin, "#defaults/config.yml");
    }

    public void reload() {
        general.reload();
        messages.reload();

        plugin.reloadConfig();
    }

    private String getConfigPath(String path) {
        return plugin.getConfig().getString("configs." + path);
    }
}
