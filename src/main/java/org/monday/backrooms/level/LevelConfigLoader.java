package org.monday.backrooms.level;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.monday.backrooms.Backrooms;

public final class LevelConfigLoader {

    private final Backrooms plugin;

    public LevelConfigLoader(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void loadInto(LevelRegistry registry) {
        Map<File, FileConfiguration> levelFiles = plugin.configFiles().levelFiles();
        if (levelFiles.isEmpty()) {
            plugin.getLogger().warning("No level config files are loaded; no Backrooms levels available.");
            return;
        }

        for (Entry<File, FileConfiguration> entry : levelFiles.entrySet()) {
            File file = entry.getKey();
            FileConfiguration section = entry.getValue();
            String id = section.getString("id", fileId(file));
            if (id == null || id.isBlank()) {
                plugin.getLogger().warning("Skipping level config file with blank id: " + file.getName());
                continue;
            }

            if (registry.get(id).isPresent()) {
                plugin.getLogger().warning("Duplicate level id '" + id + "' after normalization; previous level will be overwritten.");
            }

            BackroomsLevel level = new BackroomsLevel(
                    id,
                    section.getBoolean("enabled", true),
                    section.getString("display-name", id),
                    section.getString("world", id),
                    loadSpawn(section),
                    section.getBoolean("pvp", false),
                    loadRules(section),
                    section.getString("title", id),
                    section.getString("subtitle", ""),
                    section.getString("description", "")
            );

            registry.register(level);

            if (level.enabled() && Bukkit.getWorld(level.world()) == null) {
                plugin.getLogger().warning("Level '" + level.id() + "' references world '" + level.world() + "', but the world is not loaded.");
            }
            if (level.spawn() != null && level.spawn().pointCount() > 1) {
                plugin.getLogger().info("Level '" + level.id() + "' has " + level.spawn().pointCount() + " random spawn points.");
            }
        }

        plugin.getLogger().info("Loaded levels: total=" + registry.size()
                + ", enabled=" + registry.enabledCount()
                + ", disabled=" + registry.disabledCount() + ".");
    }

    private String fileId(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return dotIndex > 0 ? name.substring(0, dotIndex) : name;
    }

    private LevelSpawn loadSpawn(ConfigurationSection levelSection) {
        ConfigurationSection spawnSection = levelSection.getConfigurationSection("spawn");
        if (spawnSection == null) {
            return null;
        }

        List<LevelSpawnPoint> points = loadSpawnPoints(levelSection.getString("id", "unknown"), spawnSection);
        if (!points.isEmpty()) {
            return new LevelSpawn(points);
        }

        return new LevelSpawn(
                spawnSection.getDouble("x", 0.5D),
                spawnSection.getDouble("y", 64.0D),
                spawnSection.getDouble("z", 0.5D),
                (float) spawnSection.getDouble("yaw", 0.0D),
                (float) spawnSection.getDouble("pitch", 0.0D)
        );
    }

    private List<LevelSpawnPoint> loadSpawnPoints(String levelId, ConfigurationSection spawnSection) {
        List<Map<?, ?>> configuredPoints = spawnSection.getMapList("points");
        if (configuredPoints.isEmpty()) {
            return List.of();
        }

        List<LevelSpawnPoint> points = new ArrayList<>();
        for (int index = 0; index < configuredPoints.size(); index++) {
            Map<?, ?> point = configuredPoints.get(index);
            Object x = point.get("x");
            Object y = point.get("y");
            Object z = point.get("z");
            if (!(x instanceof Number xNumber) || !(y instanceof Number yNumber) || !(z instanceof Number zNumber)) {
                plugin.getLogger().warning("Skipping invalid spawn point " + index + " for level '" + levelId + "': x, y and z must be numbers.");
                continue;
            }

            points.add(new LevelSpawnPoint(
                    xNumber.doubleValue(),
                    yNumber.doubleValue(),
                    zNumber.doubleValue(),
                    number(point.get("yaw"), 0.0F),
                    number(point.get("pitch"), 0.0F)
            ));
        }

        if (points.isEmpty()) {
            plugin.getLogger().warning("Level '" + levelId + "' has spawn.points configured, but no valid points were loaded; falling back to legacy spawn fields.");
        }
        return points;
    }

    private float number(Object value, float fallback) {
        return value instanceof Number number ? number.floatValue() : fallback;
    }

    private LevelRules loadRules(ConfigurationSection levelSection) {
        ConfigurationSection rulesSection = levelSection.getConfigurationSection("rules");
        if (rulesSection == null) {
            return new LevelRules(false, false, true);
        }

        return new LevelRules(
                rulesSection.getBoolean("allow-block-break", false),
                rulesSection.getBoolean("allow-block-place", false),
                rulesSection.getBoolean("resource-interaction", true)
        );
    }
}
