package org.monday.backrooms.loot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.monday.backrooms.Backrooms;

public final class LootTableService {

    private final Backrooms plugin;
    private final Map<String, LootTableDefinition> definitions = new LinkedHashMap<>();

    public LootTableService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        definitions.clear();

        if (!plugin.configFiles().loot().getBoolean("loot-tables.enabled", true)) {
            plugin.getLogger().info("Loot tables disabled by config.");
            return;
        }

        ConfigurationSection section = plugin.configFiles().loot().getConfigurationSection("loot-tables.definitions");
        if (section == null) {
            plugin.getLogger().warning("Loot tables are enabled, but 'loot-tables.definitions' is missing.");
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection definitionSection = section.getConfigurationSection(id);
            if (definitionSection == null) {
                plugin.getLogger().warning("Skipping invalid loot table section: " + id);
                skipped++;
                continue;
            }

            Optional<LootTableDefinition> definition = loadDefinition(id, definitionSection);
            if (definition.isEmpty()) {
                skipped++;
                continue;
            }

            String normalizedId = normalize(definition.get().id());
            if (definitions.containsKey(normalizedId)) {
                plugin.getLogger().warning("Skipping duplicate loot table id: " + definition.get().id());
                skipped++;
                continue;
            }
            definitions.put(normalizedId, definition.get());
        }

        int entries = definitions.values().stream().mapToInt(table -> table.entries().size()).sum();
        plugin.getLogger().info("Loaded loot tables: enabled=true, definitions=" + definitions.size()
                + ", skipped=" + skipped + ", entries=" + entries + ".");
    }

    public int definitionCount() {
        return definitions.size();
    }

    public Collection<LootTableDefinition> all() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public Optional<LootTableDefinition> get(String id) {
        return Optional.ofNullable(definitions.get(normalize(id)));
    }

    public List<ItemStack> roll(LootTableDefinition table) {
        if (!table.enabled()) {
            return List.of();
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int minRolls = Math.max(1, table.rollsMin());
        int maxRolls = Math.max(minRolls, table.rollsMax());
        int rolls = random.nextInt(minRolls, maxRolls + 1);
        List<ItemStack> items = new ArrayList<>();

        for (int roll = 0; roll < rolls; roll++) {
            for (LootEntry entry : table.entries()) {
                if (random.nextDouble() > entry.chance()) {
                    continue;
                }

                int min = Math.max(1, entry.min());
                int max = Math.max(min, entry.max());
                int amount = random.nextInt(min, max + 1);
                createStack(entry, amount).ifPresent(items::add);
            }
        }
        return items;
    }

    private Optional<ItemStack> createStack(LootEntry entry, int amount) {
        if (entry.customItem()) {
            Optional<ItemStack> stack = plugin.items().create(entry.itemId(), amount);
            if (stack.isEmpty()) {
                plugin.getLogger().warning("Loot entry references disabled or unknown item '" + entry.itemId() + "'.");
            }
            return stack;
        }
        return Optional.of(new ItemStack(entry.material(), amount));
    }

    private Optional<LootTableDefinition> loadDefinition(String id, ConfigurationSection section) {
        List<LootEntry> entries = loadEntries(section.getMapList("entries"), id);
        if (entries.isEmpty()) {
            plugin.getLogger().warning("Skipping loot table '" + id + "' because it has no valid entries.");
            return Optional.empty();
        }

        int rollsMin = Math.max(1, section.getInt("rolls.min", 1));
        int rollsMax = Math.max(rollsMin, section.getInt("rolls.max", rollsMin));
        return Optional.of(new LootTableDefinition(
                id,
                section.getBoolean("enabled", true),
                section.getString("display-name", id),
                rollsMin,
                rollsMax,
                List.copyOf(entries)
        ));
    }

    private List<LootEntry> loadEntries(List<Map<?, ?>> entryMaps, String tableId) {
        List<LootEntry> entries = new ArrayList<>();
        for (Map<?, ?> entryMap : entryMaps) {
            String itemId = stringValue(entryMap, "item", "");
            if (itemId.isBlank()) {
                itemId = stringValue(entryMap, "custom-item", "");
            }
            if (!itemId.isBlank()) {
                String normalizedItemId = normalize(itemId);
                if (plugin.items().get(normalizedItemId).isEmpty()) {
                    plugin.getLogger().warning("Skipping unknown item '" + itemId + "' in loot table '" + tableId + "'.");
                    continue;
                }
                double chance = clamp(getDouble(entryMap, "chance", 1.0D), 0.0D, 1.0D);
                int min = Math.max(1, getInt(entryMap, "min", 1));
                int max = Math.max(min, getInt(entryMap, "max", min));
                entries.add(new LootEntry(Material.AIR, normalizedItemId, chance, min, max));
                continue;
            }

            Object materialValue = entryMap.get("material");
            String materialName = materialValue == null ? "AIR" : String.valueOf(materialValue);
            Optional<Material> material = parseMaterial(materialName, "entry for loot table " + tableId);
            if (material.isEmpty() || material.get().isAir()) {
                plugin.getLogger().warning("Skipping invalid loot material '" + materialName + "' in loot table '" + tableId + "'.");
                continue;
            }

            double chance = clamp(getDouble(entryMap, "chance", 1.0D), 0.0D, 1.0D);
            int min = Math.max(1, getInt(entryMap, "min", 1));
            int max = Math.max(min, getInt(entryMap, "max", min));
            entries.add(new LootEntry(material.get(), "", chance, min, max));
        }
        return entries;
    }

    private String stringValue(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private Optional<Material> parseMaterial(String name, String context) {
        Material material = Material.matchMaterial(name);
        if (material == null) {
            plugin.getLogger().warning("Unknown material '" + name + "' in " + context + ".");
            return Optional.empty();
        }
        return Optional.of(material);
    }

    private int getInt(Map<?, ?> map, String key, int fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }

    private double getDouble(Map<?, ?> map, String key, double fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
