package org.monday.backrooms.loot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;

public final class LootSourceService {

    private final Backrooms plugin;
    private final Map<String, LootSourceDefinition> definitions = new LinkedHashMap<>();
    private final Set<String> generatedRuntimeFallbacks = new HashSet<>();
    private boolean enabled;

    public LootSourceService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        definitions.clear();
        generatedRuntimeFallbacks.clear();

        enabled = plugin.configFiles().loot().getBoolean("loot-sources.enabled", false);
        if (!enabled) {
            plugin.getLogger().info("Loot sources disabled by config.");
            return;
        }

        ConfigurationSection section = plugin.configFiles().loot().getConfigurationSection("loot-sources.definitions");
        if (section == null) {
            plugin.getLogger().warning("Loot sources are enabled, but 'loot-sources.definitions' is missing.");
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection definitionSection = section.getConfigurationSection(id);
            if (definitionSection == null) {
                plugin.getLogger().warning("Skipping invalid loot source section: " + id);
                skipped++;
                continue;
            }

            Optional<LootSourceDefinition> definition = loadDefinition(id, definitionSection);
            if (definition.isEmpty()) {
                skipped++;
                continue;
            }

            String normalizedId = normalize(definition.get().id());
            if (definitions.containsKey(normalizedId)) {
                plugin.getLogger().warning("Skipping duplicate loot source id: " + definition.get().id());
                skipped++;
                continue;
            }
            definitions.put(normalizedId, definition.get());
        }

        plugin.getLogger().info("Loaded loot sources: enabled=true, definitions=" + definitions.size()
                + ", skipped=" + skipped + ".");
    }

    public int definitionCount() {
        return definitions.size();
    }

    public Collection<LootSourceDefinition> all() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public Optional<LootSourceDefinition> get(String id) {
        return Optional.ofNullable(definitions.get(normalize(id)));
    }

    public boolean handleVanillaContainerOpen(Player player, Block block, Inventory inventory) {
        if (!enabled || block == null || inventory == null) {
            return false;
        }

        Optional<BackroomsLevel> level = plugin.levels().getByWorld(block.getWorld().getName());
        if (level.isEmpty() || !level.get().enabled()) {
            return false;
        }

        Optional<LootSourceDefinition> source = definitions.values().stream()
                .filter(LootSourceDefinition::enabled)
                .filter(definition -> definition.type() == LootSourceType.VANILLA_CONTAINER)
                .filter(definition -> definition.appliesToLevel(level.get().id()))
                .filter(definition -> definition.matches(block))
                .findFirst();
        if (source.isEmpty()) {
            return false;
        }

        LootSourceDefinition definition = source.get();
        if (definition.oneTime() && isGenerated(block, definition)) {
            return false;
        }
        if (definition.fillEmptyOnly() && !isEmpty(inventory)) {
            return false;
        }

        List<ItemStack> generated = roll(definition);
        if (generated.isEmpty()) {
            if (definition.oneTime()) {
                markGenerated(block, definition);
            }
            return false;
        }

        Map<Integer, ItemStack> leftovers = inventory.addItem(generated.toArray(ItemStack[]::new));
        Location dropLocation = block.getLocation().add(0.5D, 1.0D, 0.5D);
        for (ItemStack item : leftovers.values()) {
            block.getWorld().dropItemNaturally(dropLocation, item);
        }
        if (definition.oneTime()) {
            markGenerated(block, definition);
        }
        plugin.getLogger().info("Generated loot source '" + definition.id() + "' for " + player.getName()
                + " at " + block.getWorld().getName() + " "
                + block.getX() + "," + block.getY() + "," + block.getZ()
                + " items=" + generated.size() + ".");
        return true;
    }

    private List<ItemStack> roll(LootSourceDefinition definition) {
        List<ItemStack> generated = new ArrayList<>();
        for (String lootTableId : definition.lootTables()) {
            plugin.lootTables().get(lootTableId).ifPresent(table -> generated.addAll(plugin.lootTables().roll(table)));
        }
        return generated;
    }

    private boolean isEmpty(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir()) {
                return false;
            }
        }
        return true;
    }

    private boolean isGenerated(Block block, LootSourceDefinition definition) {
        if (block.getState() instanceof TileState tileState) {
            Byte value = tileState.getPersistentDataContainer().get(generatedKey(definition), PersistentDataType.BYTE);
            return value != null && value == (byte) 1;
        }
        return generatedRuntimeFallbacks.contains(runtimeKey(block, definition));
    }

    private void markGenerated(Block block, LootSourceDefinition definition) {
        if (block.getState() instanceof TileState tileState) {
            tileState.getPersistentDataContainer().set(generatedKey(definition), PersistentDataType.BYTE, (byte) 1);
            tileState.update(true, false);
            return;
        }
        generatedRuntimeFallbacks.add(runtimeKey(block, definition));
    }

    private NamespacedKey generatedKey(LootSourceDefinition definition) {
        return new NamespacedKey(plugin, "loot_source_" + normalize(definition.id()));
    }

    private String runtimeKey(Block block, LootSourceDefinition definition) {
        return definition.id() + ":" + block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    private Optional<LootSourceDefinition> loadDefinition(String id, ConfigurationSection section) {
        Optional<LootSourceType> type = LootSourceType.fromConfig(section.getString("type", "vanilla_container"));
        if (type.isEmpty()) {
            plugin.getLogger().warning("Skipping loot source '" + id + "' because type is invalid.");
            return Optional.empty();
        }

        Set<String> levels = new HashSet<>();
        for (String levelId : section.getStringList("levels")) {
            String normalized = normalize(levelId);
            levels.add(normalized);
            if (plugin.levels().get(normalized).isEmpty()) {
                plugin.getLogger().warning("Loot source '" + id + "' references unknown level '" + levelId + "'.");
            }
        }

        Set<Material> materials = loadMaterials(section.getStringList("materials"), "loot source " + id);
        if (materials.isEmpty()) {
            plugin.getLogger().warning("Skipping loot source '" + id + "' because it has no valid materials.");
            return Optional.empty();
        }

        List<String> lootTables = loadLootTables(section.getStringList("loot-tables"), id);
        if (lootTables.isEmpty()) {
            plugin.getLogger().warning("Skipping loot source '" + id + "' because it has no valid loot tables.");
            return Optional.empty();
        }

        return Optional.of(new LootSourceDefinition(
                normalize(id),
                section.getBoolean("enabled", true),
                type.get(),
                Set.copyOf(levels),
                Set.copyOf(materials),
                Set.copyOf(loadPositions(section.getMapList("locations"), id)),
                List.copyOf(lootTables),
                section.getBoolean("one-time", true),
                section.getBoolean("fill-empty-only", true)
        ));
    }

    private List<String> loadLootTables(List<String> tableIds, String definitionId) {
        List<String> lootTables = new ArrayList<>();
        for (String tableId : tableIds) {
            String normalized = normalize(tableId);
            if (plugin.lootTables().get(normalized).isEmpty()) {
                plugin.getLogger().warning("Loot source '" + definitionId + "' references unknown loot table '" + tableId + "'.");
                continue;
            }
            lootTables.add(normalized);
        }
        return lootTables;
    }

    private Set<LootSourcePosition> loadPositions(List<Map<?, ?>> maps, String definitionId) {
        Set<LootSourcePosition> positions = new HashSet<>();
        for (Map<?, ?> map : maps) {
            Object x = map.get("x");
            Object y = map.get("y");
            Object z = map.get("z");
            if (!(x instanceof Number xNumber) || !(y instanceof Number yNumber) || !(z instanceof Number zNumber)) {
                plugin.getLogger().warning("Skipping invalid location in loot source '" + definitionId + "'.");
                continue;
            }
            positions.add(new LootSourcePosition(xNumber.intValue(), yNumber.intValue(), zNumber.intValue()));
        }
        return positions;
    }

    private Set<Material> loadMaterials(List<String> names, String context) {
        Set<Material> materials = new HashSet<>();
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                plugin.getLogger().warning("Unknown material '" + name + "' in " + context + ".");
                continue;
            }
            materials.add(material);
        }
        return materials;
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
