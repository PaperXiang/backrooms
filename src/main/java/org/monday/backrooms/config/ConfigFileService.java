package org.monday.backrooms.config;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.monday.backrooms.Backrooms;

public final class ConfigFileService {

    private final Backrooms plugin;
    private FileConfiguration messages;
    private FileConfiguration settings;
    private FileConfiguration resources;
    private FileConfiguration transitions;
    private FileConfiguration rooms;
    private final Map<File, FileConfiguration> levelFiles = new LinkedHashMap<>();

    public ConfigFileService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void ensureDefaultFiles() {
        saveDefault("messages.yml");
        saveDefault("resources.yml");
        saveDefault("transitions.yml");
        saveDefault("rooms.yml");
        saveDefault("settings/config.yml");
        saveDefault("levels/level_0.yml");
        saveDefault("levels/level_1.yml");
    }

    public void reload() {
        this.messages = loadFile("messages.yml");
        this.settings = loadFile("settings/config.yml");
        this.resources = loadFile("resources.yml");
        this.transitions = loadFile("transitions.yml");
        this.rooms = loadFile("rooms.yml");
        reloadLevelFiles();

        warnIgnoredLegacySection("messages", "messages.yml");
        warnIgnoredLegacySection("level-title", "settings/config.yml");
        warnIgnoredLegacySection("resource-blocks", "resources.yml");
        warnIgnoredLegacySection("transitions", "transitions.yml");
        warnIgnoredLegacySection("rooms", "rooms.yml");
        warnIgnoredLegacySection("levels", "levels/*.yml");
    }

    public FileConfiguration messages() {
        return messages;
    }

    public FileConfiguration settings() {
        return settings;
    }

    public FileConfiguration resources() {
        return resources;
    }

    public FileConfiguration transitions() {
        return transitions;
    }

    public FileConfiguration rooms() {
        return rooms;
    }

    public Map<File, FileConfiguration> levelFiles() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(levelFiles));
    }

    private void saveDefault(String resourcePath) {
        File target = new File(plugin.getDataFolder(), resourcePath);
        if (!target.exists()) {
            plugin.saveResource(resourcePath, false);
            plugin.getLogger().info("Created default config file: " + resourcePath);
        }
    }

    private FileConfiguration loadFile(String relativePath) {
        File file = new File(plugin.getDataFolder(), relativePath);
        if (!file.exists()) {
            plugin.getLogger().warning("Config file missing: " + relativePath);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void reloadLevelFiles() {
        levelFiles.clear();
        File levelsDir = new File(plugin.getDataFolder(), "levels");
        File[] files = levelsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("No level config files found in levels/.");
            return;
        }

        for (File file : files) {
            levelFiles.put(file, YamlConfiguration.loadConfiguration(file));
        }
        plugin.getLogger().info("Loaded level config files: " + levelFiles.size());
    }

    private void warnIgnoredLegacySection(String legacySection, String newLocation) {
        if (plugin.getConfig().isConfigurationSection(legacySection)) {
            plugin.getLogger().warning("Legacy config.yml section '" + legacySection + "' is ignored because " + newLocation + " is used.");
        }
    }
}
