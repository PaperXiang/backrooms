package org.monday.backrooms.level;

import org.bukkit.configuration.ConfigurationSection;
import org.monday.backrooms.Backrooms;

public final class LevelConfigLoader {

    private final Backrooms plugin;

    public LevelConfigLoader(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void loadInto(LevelRegistry registry) {
        ConfigurationSection levelsSection = plugin.getConfig().getConfigurationSection("levels");
        if (levelsSection == null) {
            plugin.getLogger().warning("No 'levels' section found in config.yml.");
            return;
        }

        for (String id : levelsSection.getKeys(false)) {
            ConfigurationSection section = levelsSection.getConfigurationSection(id);
            if (section == null) {
                plugin.getLogger().warning("Skipping invalid level config section: " + id);
                continue;
            }

            BackroomsLevel level = new BackroomsLevel(
                    id,
                    section.getBoolean("enabled", true),
                    section.getString("display-name", id),
                    section.getString("world", id),
                    loadSpawn(section),
                    section.getBoolean("pvp", false),
                    section.getString("title", id),
                    section.getString("subtitle", ""),
                    section.getString("description", "")
            );

            registry.register(level);
        }
    }

    private LevelSpawn loadSpawn(ConfigurationSection levelSection) {
        ConfigurationSection spawnSection = levelSection.getConfigurationSection("spawn");
        if (spawnSection == null) {
            return null;
        }

        return new LevelSpawn(
                spawnSection.getDouble("x", 0.5D),
                spawnSection.getDouble("y", 64.0D),
                spawnSection.getDouble("z", 0.5D),
                (float) spawnSection.getDouble("yaw", 0.0D),
                (float) spawnSection.getDouble("pitch", 0.0D)
        );
    }
}
