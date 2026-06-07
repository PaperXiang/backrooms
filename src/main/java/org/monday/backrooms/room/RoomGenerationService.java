package org.monday.backrooms.room;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;

public final class RoomGenerationService {

    private final Backrooms plugin;
    private final Map<String, RoomDefinition> definitions = new LinkedHashMap<>();
    private boolean enabled;
    private int maxBlocksPerGenerate;
    private boolean replaceAirOnly;

    public RoomGenerationService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        definitions.clear();

        this.enabled = plugin.configFiles().rooms().getBoolean("rooms.enabled", true);
        this.maxBlocksPerGenerate = plugin.configFiles().rooms().getInt("rooms.defaults.max-blocks-per-generate", 5000);
        this.replaceAirOnly = plugin.configFiles().rooms().getBoolean("rooms.defaults.replace-air-only", true);
        if (!enabled) {
            plugin.getLogger().info("Room generation disabled by config.");
            return;
        }

        ConfigurationSection section = plugin.configFiles().rooms().getConfigurationSection("rooms.definitions");
        if (section == null) {
            plugin.getLogger().warning("Room generation is enabled, but 'rooms.definitions' is missing.");
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection definitionSection = section.getConfigurationSection(id);
            if (definitionSection == null) {
                plugin.getLogger().warning("Skipping invalid room definition section: " + id);
                skipped++;
                continue;
            }

            Optional<RoomDefinition> definition = loadDefinition(id, definitionSection);
            if (definition.isEmpty()) {
                skipped++;
                continue;
            }

            String normalizedId = normalize(definition.get().id());
            if (definitions.containsKey(normalizedId)) {
                plugin.getLogger().warning("Skipping duplicate room id: " + definition.get().id());
                skipped++;
                continue;
            }
            definitions.put(normalizedId, definition.get());
        }

        plugin.getLogger().info("Loaded room definitions: enabled=true, definitions=" + definitions.size()
                + ", skipped=" + skipped + ", maxBlocksPerGenerate=" + maxBlocksPerGenerate + ".");
    }

    public int definitionCount() {
        return definitions.size();
    }

    public Collection<RoomDefinition> all() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public Optional<RoomDefinition> get(String id) {
        return Optional.ofNullable(definitions.get(normalize(id)));
    }

    public RoomGenerationResult generate(RoomDefinition room, BackroomsLevel level, Location origin) {
        if (!enabled) {
            return failure("room-generate-disabled", room, level, origin);
        }
        if (!room.enabled()) {
            return failure("room-disabled", room, level, origin);
        }
        if (!room.appliesToLevel(level.id())) {
            return failure("room-generate-level-not-allowed", room, level, origin);
        }

        int estimatedBlocks = room.width() * room.length() * room.height();
        if (estimatedBlocks > maxBlocksPerGenerate) {
            return failure("room-generate-too-large", room, level, origin);
        }
        if (!withinWorldHeight(room, origin)) {
            return failure("room-generate-out-of-bounds", room, level, origin);
        }

        int changed = switch (room.shape()) {
            case ROOM -> generateRoom(room, origin);
            case CORRIDOR -> generateCorridor(room, origin);
        };
        if (changed == 0) {
            return failure("room-generate-no-changes", room, level, origin);
        }

        return new RoomGenerationResult(true, "room-generate-success", changed, room.id(), level.id(), origin.getWorld().getName(),
                origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    }

    private Optional<RoomDefinition> loadDefinition(String id, ConfigurationSection section) {
        Optional<RoomShape> shape = RoomShape.fromConfig(section.getString("shape", "room"));
        if (shape.isEmpty()) {
            plugin.getLogger().warning("Skipping room '" + id + "' because shape is invalid.");
            return Optional.empty();
        }

        Material floor = parseMaterial(section.getString("palette.floor", "STONE"), id, "floor").orElse(null);
        Material wall = parseMaterial(section.getString("palette.wall", "STONE"), id, "wall").orElse(null);
        Material ceiling = parseMaterial(section.getString("palette.ceiling", "STONE"), id, "ceiling").orElse(null);
        Material light = parseMaterial(section.getString("palette.light", "SEA_LANTERN"), id, "light").orElse(null);
        Material marker = parseMaterial(section.getString("palette.marker", "AIR"), id, "marker").orElse(Material.AIR);
        if (floor == null || wall == null || ceiling == null || light == null) {
            return Optional.empty();
        }

        Set<String> levels = new HashSet<>();
        for (String levelId : section.getStringList("levels")) {
            String normalized = normalize(levelId);
            levels.add(normalized);
            if (plugin.levels().get(normalized).isEmpty()) {
                plugin.getLogger().warning("Room '" + id + "' references unknown level '" + levelId + "'.");
            }
        }

        int width = clamp(section.getInt("size.width", 9), 3, 31);
        int length = clamp(section.getInt("size.length", 9), 3, 64);
        int height = clamp(section.getInt("size.height", 5), 3, 16);

        return Optional.of(new RoomDefinition(
                id,
                section.getBoolean("enabled", true),
                section.getString("display-name", id),
                Set.copyOf(levels),
                shape.get(),
                width,
                length,
                height,
                floor,
                wall,
                ceiling,
                light,
                marker
        ));
    }

    private int generateRoom(RoomDefinition room, Location origin) {
        int changed = 0;
        int minX = origin.getBlockX() - room.width() / 2;
        int maxX = minX + room.width() - 1;
        int minZ = origin.getBlockZ() - room.length() / 2;
        int maxZ = minZ + room.length() - 1;
        int floorY = origin.getBlockY() - 1;
        int ceilingY = floorY + room.height() - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = floorY; y <= ceilingY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean boundaryX = x == minX || x == maxX;
                    boolean boundaryZ = z == minZ || z == maxZ;
                    Material material = materialFor(room, y, floorY, ceilingY, boundaryX || boundaryZ);
                    changed += setBlock(origin.getWorld(), x, y, z, material);
                }
            }
        }

        changed += setBlock(origin.getWorld(), origin.getBlockX(), ceilingY, origin.getBlockZ(), room.light(), true);
        changed += placeMarker(room, origin.getWorld(), origin.getBlockX(), floorY + 1, origin.getBlockZ());
        return changed;
    }

    private int generateCorridor(RoomDefinition room, Location origin) {
        int changed = 0;
        int minX = origin.getBlockX() - room.width() / 2;
        int maxX = minX + room.width() - 1;
        int minZ = origin.getBlockZ();
        int maxZ = minZ + room.length() - 1;
        int floorY = origin.getBlockY() - 1;
        int ceilingY = floorY + room.height() - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = floorY; y <= ceilingY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean wall = x == minX || x == maxX;
                    Material material = materialFor(room, y, floorY, ceilingY, wall);
                    changed += setBlock(origin.getWorld(), x, y, z, material);
                }
            }
        }

        int centerX = origin.getBlockX();
        for (int z = minZ + 2; z <= maxZ; z += 5) {
            changed += setBlock(origin.getWorld(), centerX, ceilingY, z, room.light(), true);
        }
        changed += placeMarker(room, origin.getWorld(), centerX, floorY + 1, minZ + room.length() / 2);
        return changed;
    }

    private boolean withinWorldHeight(RoomDefinition room, Location origin) {
        World world = origin.getWorld();
        int floorY = origin.getBlockY() - 1;
        int ceilingY = floorY + room.height() - 1;
        return floorY >= world.getMinHeight() && ceilingY < world.getMaxHeight();
    }

    private Material materialFor(RoomDefinition room, int y, int floorY, int ceilingY, boolean wall) {
        if (y == floorY) {
            return room.floor();
        }
        if (y == ceilingY) {
            return room.ceiling();
        }
        return wall ? room.wall() : Material.AIR;
    }

    private int placeMarker(RoomDefinition room, World world, int x, int y, int z) {
        if (room.marker().isAir()) {
            return 0;
        }
        return setBlock(world, x, y, z, room.marker());
    }

    private int setBlock(World world, int x, int y, int z, Material material) {
        return setBlock(world, x, y, z, material, false);
    }

    private int setBlock(World world, int x, int y, int z, Material material, boolean ignoreReplaceAirOnly) {
        Block block = world.getBlockAt(x, y, z);
        if (!ignoreReplaceAirOnly && replaceAirOnly && !block.getType().isAir()) {
            return 0;
        }
        if (block.getType() == material) {
            return 0;
        }
        block.setType(material, false);
        return 1;
    }

    private RoomGenerationResult failure(String messageKey, RoomDefinition room, BackroomsLevel level, Location origin) {
        return new RoomGenerationResult(false, messageKey, 0, room.id(), level.id(), origin.getWorld().getName(),
                origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    }

    private Optional<Material> parseMaterial(String name, String roomId, String field) {
        Material material = Material.matchMaterial(name == null ? "" : name);
        if (material == null) {
            plugin.getLogger().warning("Skipping room '" + roomId + "' because palette." + field + " material '" + name + "' is invalid.");
            return Optional.empty();
        }
        return Optional.of(material);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String normalize(String input) {
        return input.toLowerCase(Locale.ROOT);
    }
}
