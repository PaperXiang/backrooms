package org.monday.backrooms.corpse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;

public final class CorpseService {

    private final Backrooms plugin;
    private final Map<UUID, List<ItemStack>> pendingInsurance = new HashMap<>();
    private boolean enabled;
    private Set<String> levels = Set.of();
    private Material containerMaterial = Material.CHEST;
    private String inventoryTitle = "Corpse Cache";
    private int insuranceSlots = 1;
    private int placementSearchRadius = 2;
    private int placementSearchHeight = 2;
    private boolean dropLeftovers = true;
    private boolean dropIfNoContainerSpace = true;

    public CorpseService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        ConfigurationSection section = plugin.configFiles().corpses().getConfigurationSection("corpses");
        if (section == null) {
            enabled = false;
            plugin.getLogger().warning("Corpses are disabled because 'corpses' config section is missing.");
            return;
        }

        enabled = section.getBoolean("enabled", true);
        levels = loadLevels(section.getStringList("levels"));
        containerMaterial = loadContainerMaterial(section.getString("container-material", "CHEST"));
        inventoryTitle = section.getString("inventory-title", "Corpse Cache");
        insuranceSlots = Math.max(0, section.getInt("insurance-slots", 1));
        placementSearchRadius = Math.max(0, section.getInt("placement-search-radius", 2));
        placementSearchHeight = Math.max(0, section.getInt("placement-search-height", 2));
        dropLeftovers = section.getBoolean("drop-leftovers", true);
        dropIfNoContainerSpace = section.getBoolean("drop-if-no-container-space", true);

        plugin.getLogger().info("Loaded corpse config: enabled=" + enabled
                + ", levels=" + (levels.isEmpty() ? "any" : levels.size())
                + ", container=" + containerMaterial.name()
                + ", insuranceSlots=" + insuranceSlots
                + ", pendingInsurance=" + pendingInsurance.size() + ".");
    }

    public void copyRuntimeStateTo(CorpseService target) {
        for (Map.Entry<UUID, List<ItemStack>> entry : pendingInsurance.entrySet()) {
            target.pendingInsurance.put(entry.getKey(), cloneItems(entry.getValue()));
        }
    }

    public int pendingInsuranceCount() {
        return pendingInsurance.size();
    }

    public boolean handleDeath(PlayerDeathEvent event) {
        if (!enabled || event.getDrops().isEmpty()) {
            return false;
        }

        Player player = event.getPlayer();
        Optional<BackroomsLevel> level = plugin.levels().getByWorld(player.getWorld().getName()).filter(BackroomsLevel::enabled);
        if (level.isEmpty() || (!levels.isEmpty() && !levels.contains(level.get().id()))) {
            return false;
        }

        Optional<Location> containerLocation = findContainerLocation(player.getLocation());
        if (containerLocation.isEmpty()) {
            if (!dropIfNoContainerSpace) {
                event.getDrops().clear();
            }
            plugin.getLogger().warning("Could not place corpse cache for " + player.getName()
                    + " at " + describe(player.getLocation()) + ".");
            return false;
        }

        List<ItemStack> drops = cloneItems(event.getDrops());
        List<ItemStack> insured = takeInsurance(drops);
        if (!createContainer(containerLocation.get(), player, drops)) {
            if (!dropIfNoContainerSpace) {
                event.getDrops().clear();
            }
            return false;
        }

        event.getDrops().clear();
        if (!insured.isEmpty()) {
            pendingInsurance.put(player.getUniqueId(), insured);
        }
        plugin.getLogger().info("Created corpse cache for " + player.getName()
                + " at " + describe(containerLocation.get())
                + " items=" + drops.size()
                + ", insured=" + insured.size() + ".");
        return true;
    }

    public void handleRespawn(PlayerRespawnEvent event) {
        List<ItemStack> insured = pendingInsurance.remove(event.getPlayer().getUniqueId());
        if (insured == null || insured.isEmpty()) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player player = event.getPlayer();
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(insured.toArray(ItemStack[]::new));
            for (ItemStack item : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            plugin.messages().send(player, "corpse-insurance-restored",
                    plugin.messages().text("items", String.valueOf(insured.size()))
            );
        });
    }

    private Set<String> loadLevels(List<String> configuredLevels) {
        Set<String> loaded = new HashSet<>();
        for (String levelId : configuredLevels) {
            String normalized = normalize(levelId);
            if (plugin.levels().get(normalized).isEmpty()) {
                plugin.getLogger().warning("Corpse config references unknown level '" + levelId + "'.");
            }
            loaded.add(normalized);
        }
        return Set.copyOf(loaded);
    }

    private Material loadContainerMaterial(String configuredMaterial) {
        Material material = Material.matchMaterial(configuredMaterial);
        if (material == null || !material.isBlock()) {
            plugin.getLogger().warning("Invalid corpse container material '" + configuredMaterial + "', using CHEST.");
            return Material.CHEST;
        }
        return material;
    }

    private Optional<Location> findContainerLocation(Location origin) {
        World world = origin.getWorld();
        if (world == null) {
            return Optional.empty();
        }

        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();
        for (int dy = 0; dy <= placementSearchHeight; dy++) {
            int y = baseY + dy;
            if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
                continue;
            }
            for (int radius = 0; radius <= placementSearchRadius; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }
                        Block block = world.getBlockAt(baseX + dx, y, baseZ + dz);
                        if (block.getType().isAir()) {
                            return Optional.of(block.getLocation());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private List<ItemStack> takeInsurance(List<ItemStack> drops) {
        List<ItemStack> insured = new ArrayList<>();
        int index = 0;
        while (insured.size() < insuranceSlots && index < drops.size()) {
            ItemStack item = drops.get(index);
            if (item == null || item.getType().isAir()) {
                index++;
                continue;
            }
            insured.add(item.clone());
            drops.remove(index);
        }
        return insured;
    }

    private boolean createContainer(Location location, Player owner, List<ItemStack> drops) {
        Block block = location.getBlock();
        block.setType(containerMaterial, false);
        if (!(block.getState() instanceof Container container)) {
            plugin.getLogger().warning("Corpse container material '" + containerMaterial.name() + "' did not create a container.");
            block.setType(Material.AIR, false);
            return false;
        }

        container.setCustomName(inventoryTitle);
        container.getPersistentDataContainer().set(key("corpse_owner"), PersistentDataType.STRING, owner.getUniqueId().toString());
        container.getPersistentDataContainer().set(key("corpse_owner_name"), PersistentDataType.STRING, owner.getName());
        container.getPersistentDataContainer().set(key("corpse_created_at"), PersistentDataType.LONG, System.currentTimeMillis());
        container.update(true, false);

        Map<Integer, ItemStack> leftovers = container.getInventory().addItem(drops.toArray(ItemStack[]::new));
        if (dropLeftovers) {
            Location dropLocation = location.clone().add(0.5D, 1.0D, 0.5D);
            for (ItemStack item : leftovers.values()) {
                location.getWorld().dropItemNaturally(dropLocation, item);
            }
        }
        return true;
    }

    private List<ItemStack> cloneItems(List<ItemStack> items) {
        List<ItemStack> cloned = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                cloned.add(item.clone());
            }
        }
        return cloned;
    }

    private NamespacedKey key(String value) {
        return new NamespacedKey(plugin, value);
    }

    private String describe(Location location) {
        return location.getWorld().getName() + " "
                + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
