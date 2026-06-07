package org.monday.backrooms.items;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.monday.backrooms.Backrooms;

public final class BackroomsItemService {

    private final Backrooms plugin;
    private final NamespacedKey itemIdKey;
    private final Map<String, BackroomsItemDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, String> displayNameFallbacks = new HashMap<>();
    private final Map<String, Long> useCooldowns = new HashMap<>();

    public BackroomsItemService(Backrooms plugin) {
        this.plugin = plugin;
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
    }

    public void reload() {
        definitions.clear();
        displayNameFallbacks.clear();
        useCooldowns.clear();

        if (!plugin.configFiles().items().getBoolean("items.enabled", true)) {
            plugin.getLogger().info("Backrooms items disabled by config.");
            return;
        }

        ConfigurationSection section = plugin.configFiles().items().getConfigurationSection("items.definitions");
        if (section == null) {
            plugin.getLogger().warning("Items are enabled, but 'items.definitions' is missing.");
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection definitionSection = section.getConfigurationSection(id);
            if (definitionSection == null) {
                plugin.getLogger().warning("Skipping invalid item definition section: " + id);
                skipped++;
                continue;
            }

            Optional<BackroomsItemDefinition> definition = loadDefinition(id, definitionSection);
            if (definition.isEmpty()) {
                skipped++;
                continue;
            }

            String normalizedId = normalize(definition.get().id());
            if (definitions.containsKey(normalizedId)) {
                plugin.getLogger().warning("Skipping duplicate item id: " + definition.get().id());
                skipped++;
                continue;
            }

            definitions.put(normalizedId, definition.get());
            registerDisplayFallback(definition.get());
        }

        long activeEffects = definitions.values().stream().filter(definition -> definition.sanity().hasEffect()).count();
        plugin.getLogger().info("Loaded Backrooms items: enabled=true, definitions=" + definitions.size()
                + ", skipped=" + skipped + ", sanityEffects=" + activeEffects + ".");
    }

    public void clear() {
        definitions.clear();
        displayNameFallbacks.clear();
        useCooldowns.clear();
    }

    public int definitionCount() {
        return definitions.size();
    }

    public Collection<BackroomsItemDefinition> all() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public Optional<BackroomsItemDefinition> get(String id) {
        return Optional.ofNullable(definitions.get(normalize(id)));
    }

    public Optional<ItemStack> create(String id, int amount) {
        return get(id).filter(BackroomsItemDefinition::enabled).map(definition -> create(definition, amount));
    }

    public ItemStack create(BackroomsItemDefinition definition, int amount) {
        int stackAmount = Math.max(1, amount);
        ItemStack stack = new ItemStack(definition.material(), stackAmount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        meta.displayName(plugin.messages().parse(definition.displayName()));
        if (!definition.lore().isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : definition.lore()) {
                lore.add(plugin.messages().parse(line));
            }
            meta.lore(lore);
        }
        if (definition.hasCustomModelData()) {
            meta.setCustomModelData(definition.customModelData());
        }
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, definition.id());
        stack.setItemMeta(meta);
        return stack;
    }

    public Optional<BackroomsItemDefinition> fromStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return Optional.empty();
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }

        String storedId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        if (storedId != null) {
            Optional<BackroomsItemDefinition> definition = get(storedId);
            if (definition.isPresent()) {
                return definition;
            }
        }

        for (String plainName : plainNames(meta)) {
            String fallbackKey = displayFallbackKey(stack.getType(), plainName);
            String fallbackId = displayNameFallbacks.get(fallbackKey);
            if (fallbackId != null) {
                return get(fallbackId);
            }
        }

        return Optional.empty();
    }

    public long remainingUseCooldown(Player player, BackroomsItemDefinition definition) {
        if (definition.useCooldownSeconds() <= 0) {
            return 0L;
        }
        return useCooldowns.getOrDefault(cooldownKey(player.getUniqueId(), definition.id()), 0L) - System.currentTimeMillis();
    }

    public void startUseCooldown(Player player, BackroomsItemDefinition definition) {
        if (definition.useCooldownSeconds() <= 0) {
            return;
        }
        useCooldowns.put(cooldownKey(player.getUniqueId(), definition.id()),
                System.currentTimeMillis() + definition.useCooldownSeconds() * 1000L);
    }

    private Optional<BackroomsItemDefinition> loadDefinition(String id, ConfigurationSection section) {
        String materialName = section.getString("material", "PAPER");
        Optional<Material> material = parseMaterial(materialName, "item " + id);
        if (material.isEmpty() || material.get().isAir()) {
            plugin.getLogger().warning("Skipping item '" + id + "' because it has invalid material '" + materialName + "'.");
            return Optional.empty();
        }

        Material consumeReplacement = parseMaterial(section.getString("consume.replacement", "AIR"), "consume replacement for item " + id)
                .orElse(Material.AIR);
        SanityItemEffect sanity = new SanityItemEffect(
                section.getDouble("sanity.restore", 0.0D),
                section.getDouble("sanity.stabilize-seconds", 0.0D)
        );

        return Optional.of(new BackroomsItemDefinition(
                normalize(id),
                section.getBoolean("enabled", true),
                material.get(),
                section.getString("display-name", id),
                List.copyOf(section.getStringList("lore")),
                section.getInt("custom-model-data", 0),
                section.getBoolean("consume.right-click", false),
                consumeReplacement,
                Math.max(0, section.getInt("consume.cooldown-seconds", 0)),
                sanity,
                section.getString("consume.message", "")
        ));
    }

    private void registerDisplayFallback(BackroomsItemDefinition definition) {
        try {
            String plainName = PlainTextComponentSerializer.plainText().serialize(plugin.messages().parse(definition.displayName()));
            displayNameFallbacks.put(displayFallbackKey(definition.material(), plainName), definition.id());
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Could not build display-name fallback for item '" + definition.id() + "': " + exception.getMessage());
        }
    }

    private List<String> plainNames(ItemMeta meta) {
        List<String> names = new ArrayList<>();
        if (meta.hasDisplayName() && meta.displayName() != null) {
            names.add(PlainTextComponentSerializer.plainText().serialize(meta.displayName()));
        }

        // CraftEngine item_name uses the modern item-name component on Paper.
        reflectComponentName(meta, "itemName").ifPresent(names::add);
        reflectStringName(meta, "getItemName").ifPresent(names::add);
        return names;
    }

    private Optional<String> reflectComponentName(ItemMeta meta, String methodName) {
        try {
            Method method = meta.getClass().getMethod(methodName);
            Object value = method.invoke(meta);
            if (value instanceof Component component) {
                return Optional.of(PlainTextComponentSerializer.plainText().serialize(component));
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Older APIs may not expose item-name components; display-name fallback still works.
        }
        return Optional.empty();
    }

    private Optional<String> reflectStringName(ItemMeta meta, String methodName) {
        try {
            Method method = meta.getClass().getMethod(methodName);
            Object value = method.invoke(meta);
            if (value instanceof String string && !string.isBlank()) {
                return Optional.of(string);
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Older APIs may not expose item-name methods; display-name fallback still works.
        }
        return Optional.empty();
    }

    private Optional<Material> parseMaterial(String name, String context) {
        Material material = Material.matchMaterial(name == null ? "" : name);
        if (material == null) {
            plugin.getLogger().warning("Unknown material '" + name + "' in " + context + ".");
            return Optional.empty();
        }
        return Optional.of(material);
    }

    private String displayFallbackKey(Material material, String plainName) {
        return material.name() + ":" + plainName.toLowerCase(Locale.ROOT).trim();
    }

    private String cooldownKey(UUID playerId, String itemId) {
        return playerId + ":" + normalize(itemId);
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
