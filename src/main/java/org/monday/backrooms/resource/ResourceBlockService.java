package org.monday.backrooms.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;

public final class ResourceBlockService {

    private final Backrooms plugin;
    private final List<ResourceBlockDefinition> definitions = new ArrayList<>();
    private final Map<String, Long> cooldowns = new HashMap<>();

    public ResourceBlockService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        definitions.clear();
        cooldowns.clear();

        if (!plugin.configFiles().resources().getBoolean("resource-blocks.enabled", true)) {
            plugin.getLogger().info("Resource blocks disabled by config.");
            return;
        }

        ConfigurationSection section = plugin.configFiles().resources().getConfigurationSection("resource-blocks.definitions");
        if (section == null) {
            plugin.getLogger().warning("Resource blocks are enabled, but 'resource-blocks.definitions' is missing.");
            return;
        }

        if (section.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("Resource blocks are enabled, but no definitions were configured.");
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection definitionSection = section.getConfigurationSection(id);
            if (definitionSection == null) {
                plugin.getLogger().warning("Skipping invalid resource block definition section: " + id);
                skipped++;
                continue;
            }

            Optional<ResourceBlockDefinition> definition = loadDefinition(id, definitionSection);
            if (definition.isPresent()) {
                definitions.add(definition.get());
            } else {
                skipped++;
            }
        }

        int drops = definitions.stream().mapToInt(definition -> definition.drops().size()).sum();
        plugin.getLogger().info("Loaded resource blocks: enabled=true, definitions=" + definitions.size()
                + ", skipped=" + skipped + ", drops=" + drops + ".");
    }

    public int definitionCount() {
        return definitions.size();
    }

    public boolean handleBreak(BlockBreakEvent event, BackroomsLevel level) {
        Optional<ResourceBlockDefinition> definition = match(level, event.getBlock(), ResourceTrigger.BREAK);
        if (definition.isEmpty()) {
            return false;
        }

        event.setCancelled(true);
        event.setExpToDrop(0);
        harvest(event.getPlayer(), event.getBlock(), definition.get());
        return true;
    }

    public boolean handleInteract(PlayerInteractEvent event, BackroomsLevel level) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return false;
        }

        Optional<ResourceBlockDefinition> definition = match(level, block, ResourceTrigger.RIGHT_CLICK);
        if (definition.isEmpty()) {
            return false;
        }

        if (definition.get().cancelOriginalEvent()) {
            event.setCancelled(true);
        }

        harvest(event.getPlayer(), block, definition.get());
        return true;
    }

    private Optional<ResourceBlockDefinition> match(BackroomsLevel level, Block block, ResourceTrigger trigger) {
        if (!level.rules().resourceInteraction()) {
            return Optional.empty();
        }

        return definitions.stream()
                .filter(definition -> definition.appliesToLevel(level.id()))
                .filter(definition -> definition.matches(block.getType(), trigger))
                .findFirst();
    }

    private void harvest(Player player, Block block, ResourceBlockDefinition definition) {
        long remainingMillis = remainingCooldown(block, definition);
        if (remainingMillis > 0L) {
            plugin.messages().send(player, "resource-cooldown",
                    plugin.messages().text("seconds", String.valueOf(Math.max(1L, (remainingMillis + 999L) / 1000L)))
            );
            return;
        }

        Location dropLocation = block.getLocation().add(0.5D, 0.5D, 0.5D);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (ResourceDrop drop : definition.drops()) {
            if (random.nextDouble() > drop.chance()) {
                continue;
            }

            int min = Math.max(1, drop.min());
            int max = Math.max(min, drop.max());
            int amount = random.nextInt(min, max + 1);
            block.getWorld().dropItemNaturally(dropLocation, new ItemStack(drop.material(), amount));
        }

        if (definition.removeBlock()) {
            block.setType(definition.replacement(), false);
        }

        startCooldown(block, definition);
    }

    private Optional<ResourceBlockDefinition> loadDefinition(String id, ConfigurationSection section) {
        Set<String> levels = new HashSet<>();
        for (String levelId : section.getStringList("levels")) {
            levels.add(levelId.toLowerCase(Locale.ROOT));
        }

        Set<Material> materials = loadMaterials(section.getStringList("materials"), "resource block " + id);
        if (materials.isEmpty()) {
            plugin.getLogger().warning("Skipping resource block '" + id + "' because it has no valid materials.");
            return Optional.empty();
        }

        Set<ResourceTrigger> triggers = new HashSet<>();
        for (String trigger : section.getStringList("triggers")) {
            ResourceTrigger.fromConfig(trigger).ifPresentOrElse(triggers::add,
                    () -> plugin.getLogger().warning("Unknown resource trigger '" + trigger + "' in resource block '" + id + "'."));
        }
        if (triggers.isEmpty()) {
            plugin.getLogger().warning("Resource block '" + id + "' has no valid triggers; defaulting to BREAK.");
            triggers.add(ResourceTrigger.BREAK);
        }

        for (String levelId : levels) {
            if (plugin.levels().get(levelId).isEmpty()) {
                plugin.getLogger().warning("Resource block '" + id + "' references unknown level '" + levelId + "'.");
            }
        }

        Material replacement = parseMaterial(section.getString("replacement", "AIR"), "replacement for resource block " + id)
                .orElse(Material.AIR);

        List<ResourceDrop> drops = loadDrops(section.getMapList("drops"), id);
        boolean removeBlock = section.getBoolean("remove-block", false);
        if (drops.isEmpty() && !removeBlock) {
            plugin.getLogger().warning("Resource block '" + id + "' has no drops and does not remove the block; interaction will have no reward.");
        }

        return Optional.of(new ResourceBlockDefinition(
                id,
                levels,
                materials,
                triggers,
                section.getBoolean("cancel-original-event", true),
                removeBlock,
                replacement,
                section.getLong("cooldown-seconds", 0L),
                drops
        ));
    }

    private Set<Material> loadMaterials(List<String> names, String context) {
        Set<Material> materials = new HashSet<>();
        for (String name : names) {
            parseMaterial(name, context).ifPresent(materials::add);
        }
        return materials;
    }

    private List<ResourceDrop> loadDrops(List<Map<?, ?>> dropMaps, String definitionId) {
        List<ResourceDrop> drops = new ArrayList<>();
        for (Map<?, ?> dropMap : dropMaps) {
            Object materialValue = dropMap.get("material");
            String materialName = materialValue == null ? "AIR" : String.valueOf(materialValue);
            Optional<Material> material = parseMaterial(materialName, "drop for resource block " + definitionId);
            if (material.isEmpty() || material.get().isAir()) {
                plugin.getLogger().warning("Skipping invalid drop material '" + materialName + "' in resource block '" + definitionId + "'.");
                continue;
            }

            double chance = getDouble(dropMap, "chance", 1.0D);
            int min = getInt(dropMap, "min", 1);
            int max = getInt(dropMap, "max", min);
            drops.add(new ResourceDrop(material.get(), Math.max(0.0D, Math.min(1.0D, chance)), min, max));
        }
        return drops;
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

    private long remainingCooldown(Block block, ResourceBlockDefinition definition) {
        if (definition.cooldownSeconds() <= 0L) {
            return 0L;
        }

        return cooldowns.getOrDefault(cooldownKey(block, definition), 0L) - System.currentTimeMillis();
    }

    private void startCooldown(Block block, ResourceBlockDefinition definition) {
        if (definition.cooldownSeconds() <= 0L) {
            return;
        }

        cooldowns.put(cooldownKey(block, definition), System.currentTimeMillis() + definition.cooldownSeconds() * 1000L);
    }

    private String cooldownKey(Block block, ResourceBlockDefinition definition) {
        Location location = block.getLocation();
        return definition.id() + "@" + location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }
}
