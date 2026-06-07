package org.monday.backrooms.worldgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;

public final class WorldGenerationService {

    private final Backrooms plugin;
    private final Map<String, SchematicTemplateDefinition> templates = new LinkedHashMap<>();
    private final Map<TemplateMarkerType, Material> markerMaterials = new EnumMap<>(TemplateMarkerType.class);
    private boolean enabled;
    private File templatesRoot;
    private File generatedRegionsFile;
    private YamlConfiguration generatedRegions;
    private int defaultCellSize;
    private int defaultCellHeight;
    private int minCriticalPathCells;
    private int maxCriticalPathCells;
    private double branchRate;
    private double loopRate;

    public WorldGenerationService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        templates.clear();
        markerMaterials.clear();

        this.enabled = plugin.configFiles().worldgen().getBoolean("worldgen.enabled", true);
        this.templatesRoot = new File(plugin.getDataFolder(), plugin.configFiles().worldgen().getString("worldgen.templates-folder", "templates"));
        this.generatedRegionsFile = new File(plugin.getDataFolder(), plugin.configFiles().worldgen().getString("worldgen.generated-regions-file", "generated-regions.yml"));
        this.generatedRegions = YamlConfiguration.loadConfiguration(generatedRegionsFile);
        this.defaultCellSize = plugin.configFiles().worldgen().getInt("worldgen.defaults.cell-size", 16);
        this.defaultCellHeight = plugin.configFiles().worldgen().getInt("worldgen.defaults.cell-height", 6);
        this.minCriticalPathCells = plugin.configFiles().worldgen().getInt("worldgen.defaults.min-critical-path-cells", 18);
        this.maxCriticalPathCells = Math.max(minCriticalPathCells, plugin.configFiles().worldgen().getInt("worldgen.defaults.max-critical-path-cells", 30));
        this.branchRate = clamp(plugin.configFiles().worldgen().getDouble("worldgen.defaults.branch-rate", 0.32D), 0.0D, 1.0D);
        this.loopRate = clamp(plugin.configFiles().worldgen().getDouble("worldgen.defaults.loop-rate", 0.10D), 0.0D, 1.0D);

        loadMarkers();
        loadTemplates();

        plugin.getLogger().info("Loaded worldgen config: enabled=" + enabled
                + ", templates=" + templates.size()
                + ", markers=" + markerMaterials.size()
                + ", templatesRoot=" + templatesRoot.getPath() + ".");
    }

    public int templateCount() {
        return templates.size();
    }

    public Collection<SchematicTemplateDefinition> allTemplates() {
        return Collections.unmodifiableCollection(templates.values());
    }

    public Optional<SchematicTemplateDefinition> template(String id) {
        return Optional.ofNullable(templates.get(normalize(id)));
    }

    public boolean worldEditAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldEdit") || Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

    public SchematicScaffoldResult scaffoldTemplates(boolean overwrite) {
        if (!worldEditAvailable()) {
            return new SchematicScaffoldResult(false, 0, 0, templates.size(), "WorldEdit or FastAsyncWorldEdit is not enabled.");
        }

        int written = 0;
        int skipped = 0;
        List<String> failures = new ArrayList<>();
        for (SchematicTemplateDefinition template : templates.values()) {
            if (template.file().isFile() && !overwrite) {
                skipped++;
                continue;
            }

            try {
                WorldEditSchematicScaffolder.write(template);
                written++;
            } catch (IOException | RuntimeException exception) {
                failures.add(template.id() + ": " + exception.getMessage());
            }
        }

        String detail = failures.isEmpty()
                ? "written=" + written + ", skipped=" + skipped
                : "written=" + written + ", skipped=" + skipped + ", failures=" + String.join("; ", failures);
        return new SchematicScaffoldResult(failures.isEmpty(), written, skipped, failures.size(), detail);
    }

    public WorldGenerationResult generate(BackroomsLevel level, int requestedSize, String seedInput) {
        if (!enabled) {
            return failure("worldgen-disabled", level, "none", "Worldgen disabled by config.");
        }
        if (!worldEditAvailable()) {
            return failure("worldgen-worldedit-missing", level, "none", "WorldEdit or FastAsyncWorldEdit is not enabled.");
        }

        World world = Bukkit.getWorld(level.world());
        if (world == null) {
            return failure("worldgen-world-not-loaded", level, "none", "World is not loaded: " + level.world());
        }

        int size = clampOdd(requestedSize, 3, 31);
        long seed = parseSeed(seedInput);
        Location anchor = level.spawn() == null ? world.getSpawnLocation() : level.spawn().toLocation(world);
        int half = size / 2;
        int startX = align(anchor.getBlockX(), defaultCellSize) - half * defaultCellSize;
        int startZ = align(anchor.getBlockZ(), defaultCellSize) - half * defaultCellSize;
        int floorY = anchor.getBlockY();
        String regionId = level.id() + "_" + startX + "_" + floorY + "_" + startZ + "_" + size + "_" + Long.toUnsignedString(seed);
        if (generatedRegions.isConfigurationSection("generated-regions." + regionId)) {
            return failure("worldgen-region-exists", level, regionId, "Region already generated.");
        }

        List<PlannedCell> plan = planCells(level.id(), size, seed);
        if (plan.isEmpty()) {
            return failure("worldgen-no-templates", level, regionId, "No cells could be planned.");
        }

        int pasted = 0;
        int approximateBlocks = 0;
        try {
            for (PlannedCell cell : plan) {
                int pasteX = startX + cell.x() * defaultCellSize;
                int pasteZ = startZ + cell.z() * defaultCellSize;
                pasteTemplate(world, cell.template(), cell.rotation(), pasteX, floorY, pasteZ);
                pasted++;
                approximateBlocks += cell.template().cellSize() * cell.template().cellSize() * cell.template().footprintY()
                        * cell.template().footprintX() * cell.template().footprintZ();
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Worldgen paste failed for region " + regionId + ": " + exception.getMessage());
            exception.printStackTrace();
            return failure("worldgen-paste-failed", level, regionId, exception.getMessage());
        }

        String markers = scanMarkers(world, startX, floorY, startZ, size);
        saveGeneratedRegion(regionId, level, world, size, seed, startX, floorY, startZ, pasted, markers);
        return new WorldGenerationResult(true, "worldgen-generate-success", level.id(), world.getName(), regionId,
                plan.size(), pasted, approximateBlocks, markers, "ok");
    }

    private void loadMarkers() {
        ConfigurationSection section = plugin.configFiles().worldgen().getConfigurationSection("worldgen.markers");
        if (section == null) {
            return;
        }

        for (TemplateMarkerType type : TemplateMarkerType.values()) {
            String materialName = section.getString(type.configName());
            if (materialName == null) {
                continue;
            }
            Material material = Material.matchMaterial(materialName);
            if (material == null || material.isAir()) {
                plugin.getLogger().warning("Skipping invalid worldgen marker material for " + type.configName() + ": " + materialName);
                continue;
            }
            markerMaterials.put(type, material);
        }
    }

    private void loadTemplates() {
        ConfigurationSection section = plugin.configFiles().worldgen().getConfigurationSection("worldgen.templates");
        if (section == null) {
            plugin.getLogger().warning("Worldgen is enabled, but 'worldgen.templates' is missing.");
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection templateSection = section.getConfigurationSection(id);
            if (templateSection == null) {
                skipped++;
                continue;
            }

            Optional<SchematicTemplateDefinition> template = loadTemplate(id, templateSection);
            if (template.isEmpty()) {
                skipped++;
                continue;
            }
            templates.put(normalize(id), template.get());
        }

        plugin.getLogger().info("Loaded schematic templates: definitions=" + templates.size() + ", skipped=" + skipped + ".");
    }

    private Optional<SchematicTemplateDefinition> loadTemplate(String id, ConfigurationSection section) {
        String level = section.getString("level", "").trim();
        if (level.isBlank()) {
            plugin.getLogger().warning("Skipping schematic template '" + id + "' because level is blank.");
            return Optional.empty();
        }
        if (plugin.levels().get(level).isEmpty()) {
            plugin.getLogger().warning("Schematic template '" + id + "' references unknown level '" + level + "'.");
        }

        Set<TemplateConnector> connectors = new HashSet<>();
        for (String connectorName : section.getStringList("connectors")) {
            TemplateConnector.fromConfig(connectorName).ifPresentOrElse(connectors::add,
                    () -> plugin.getLogger().warning("Schematic template '" + id + "' has invalid connector '" + connectorName + "'."));
        }

        List<Integer> rotations = section.getIntegerList("rotations").stream()
                .filter(rotation -> Math.floorMod(rotation, 90) == 0)
                .map(rotation -> Math.floorMod(rotation, 360))
                .distinct()
                .toList();
        boolean pasteAir = section.getBoolean("paste-air", plugin.configFiles().worldgen().getBoolean("worldgen.defaults.paste-air", false));
        File file = new File(templatesRoot, section.getString("file", id + ".schem"));
        if (!file.exists()) {
            plugin.getLogger().warning("Schematic template '" + id + "' file does not exist yet: " + file.getPath());
        }

        return Optional.of(new SchematicTemplateDefinition(
                id,
                section.getBoolean("enabled", true),
                section.getString("display-name", id),
                level,
                file,
                Math.max(1, section.getInt("cell-size", defaultCellSize)),
                Math.max(1, section.getInt("footprint.x", 1)),
                Math.max(1, section.getInt("footprint.z", 1)),
                Math.max(1, section.getInt("footprint.y", defaultCellHeight)),
                connectors,
                Set.copyOf(section.getStringList("tags")),
                Math.max(1, section.getInt("weight", 1)),
                rotations,
                section.getInt("max-per-region", -1),
                section.getBoolean("unique", false),
                Math.max(0, section.getInt("min-distance-from-spawn-cells", 0)),
                pasteAir
        ));
    }

    private List<PlannedCell> planCells(String levelId, int size, long seed) {
        Random random = new Random(seed);
        List<CellCoordinate> graph = buildGraph(size, random);
        Map<CellCoordinate, Set<TemplateConnector>> connectors = buildConnectors(graph);
        Map<String, Integer> usage = new HashMap<>();
        List<PlannedCell> plan = new ArrayList<>();
        CellCoordinate spawn = new CellCoordinate(size / 2, size / 2);
        CellCoordinate exit = graph.get(graph.size() - 1);

        for (CellCoordinate coordinate : graph) {
            boolean exitCell = coordinate.equals(exit);
            int distance = manhattan(spawn, coordinate);
            Optional<TemplateChoice> choice = chooseTemplate(levelId, connectors.getOrDefault(coordinate, Set.of()), exitCell, distance, usage, random);
            if (choice.isEmpty()) {
                plugin.getLogger().warning("No schematic template matched cell " + coordinate + " connectors=" + connectors.getOrDefault(coordinate, Set.of()));
                return List.of();
            }
            SchematicTemplateDefinition template = choice.get().template();
            usage.merge(template.id(), 1, Integer::sum);
            plan.add(new PlannedCell(coordinate.x(), coordinate.z(), template, choice.get().rotation()));
        }
        return plan;
    }

    private List<CellCoordinate> buildGraph(int size, Random random) {
        CellCoordinate spawn = new CellCoordinate(size / 2, size / 2);
        List<CellCoordinate> criticalPath = new ArrayList<>();
        Set<CellCoordinate> used = new HashSet<>();
        criticalPath.add(spawn);
        used.add(spawn);

        int targetLength = Math.min(size * size, random.nextInt(minCriticalPathCells, maxCriticalPathCells + 1));
        CellCoordinate current = spawn;
        while (criticalPath.size() < targetLength) {
            List<CellCoordinate> candidates = neighbors(current, size).stream().filter(cell -> !used.contains(cell)).toList();
            if (candidates.isEmpty()) {
                break;
            }
            current = candidates.get(random.nextInt(candidates.size()));
            criticalPath.add(current);
            used.add(current);
        }

        ArrayDeque<CellCoordinate> branchQueue = new ArrayDeque<>(criticalPath);
        while (!branchQueue.isEmpty()) {
            CellCoordinate base = branchQueue.removeFirst();
            if (random.nextDouble() > branchRate) {
                continue;
            }
            int length = 1 + random.nextInt(3);
            CellCoordinate branch = base;
            for (int step = 0; step < length; step++) {
                List<CellCoordinate> candidates = neighbors(branch, size).stream().filter(cell -> !used.contains(cell)).toList();
                if (candidates.isEmpty()) {
                    break;
                }
                branch = candidates.get(random.nextInt(candidates.size()));
                used.add(branch);
            }
        }

        return new ArrayList<>(used);
    }

    private Map<CellCoordinate, Set<TemplateConnector>> buildConnectors(List<CellCoordinate> graph) {
        Set<CellCoordinate> graphSet = new HashSet<>(graph);
        Map<CellCoordinate, Set<TemplateConnector>> connectors = new HashMap<>();
        for (CellCoordinate cell : graph) {
            Set<TemplateConnector> cellConnectors = new HashSet<>();
            for (TemplateConnector connector : TemplateConnector.values()) {
                CellCoordinate neighbor = new CellCoordinate(cell.x() + connector.deltaX(), cell.z() + connector.deltaZ());
                if (graphSet.contains(neighbor)) {
                    cellConnectors.add(connector);
                } else if (loopRate > 0.0D && Math.abs(cell.x() - neighbor.x()) + Math.abs(cell.z() - neighbor.z()) == 1) {
                    // Loop candidates are left to future generated-region metadata; schematics still need a matching connector now.
                }
            }
            connectors.put(cell, cellConnectors);
        }
        return connectors;
    }

    private Optional<TemplateChoice> chooseTemplate(String levelId, Set<TemplateConnector> requiredConnectors, boolean exitCell,
                                                    int distanceFromSpawn, Map<String, Integer> usage, Random random) {
        List<TemplateChoice> choices = new ArrayList<>();
        for (SchematicTemplateDefinition template : templates.values()) {
            if (!template.enabled() || !template.appliesToLevel(levelId)) {
                continue;
            }
            if (exitCell != template.hasTag("exit")) {
                continue;
            }
            if (template.unique() && usage.getOrDefault(template.id(), 0) > 0) {
                continue;
            }
            if (template.maxPerRegion() >= 0 && usage.getOrDefault(template.id(), 0) >= template.maxPerRegion()) {
                continue;
            }
            if (distanceFromSpawn < template.minDistanceFromSpawnCells()) {
                continue;
            }

            for (int rotation : template.rotations()) {
                Set<TemplateConnector> rotated = template.connectorsAfterRotation(rotation);
                if (rotated.containsAll(requiredConnectors)) {
                    choices.add(new TemplateChoice(template, rotation));
                }
            }
        }

        if (choices.isEmpty() && exitCell) {
            return chooseTemplate(levelId, requiredConnectors, false, distanceFromSpawn, usage, random);
        }
        if (choices.isEmpty()) {
            return Optional.empty();
        }

        int totalWeight = choices.stream().mapToInt(choice -> choice.template().weight()).sum();
        int roll = random.nextInt(Math.max(1, totalWeight));
        for (TemplateChoice choice : choices) {
            roll -= choice.template().weight();
            if (roll < 0) {
                return Optional.of(choice);
            }
        }
        return Optional.of(choices.get(choices.size() - 1));
    }

    private void pasteTemplate(World world, SchematicTemplateDefinition template, int rotation, int x, int y, int z) {
        if (!template.file().exists()) {
            throw new IllegalStateException("Missing schematic file for template '" + template.id() + "': " + template.file().getPath());
        }
        WorldEditSchematicPaster.paste(world, template, rotation, x, y, z);
    }

    private String scanMarkers(World world, int startX, int floorY, int startZ, int size) {
        Map<TemplateMarkerType, Integer> counts = new EnumMap<>(TemplateMarkerType.class);
        int maxX = startX + size * defaultCellSize - 1;
        int maxY = floorY + defaultCellHeight - 1;
        int maxZ = startZ + size * defaultCellSize - 1;
        for (int x = startX; x <= maxX; x++) {
            for (int y = floorY; y <= maxY; y++) {
                for (int z = startZ; z <= maxZ; z++) {
                    Material material = world.getBlockAt(x, y, z).getType();
                    for (Map.Entry<TemplateMarkerType, Material> entry : markerMaterials.entrySet()) {
                        if (material == entry.getValue()) {
                            counts.merge(entry.getKey(), 1, Integer::sum);
                        }
                    }
                }
            }
        }

        if (counts.isEmpty()) {
            return "none";
        }
        return counts.entrySet().stream()
                .map(entry -> entry.getKey().configName() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    private void saveGeneratedRegion(String regionId, BackroomsLevel level, World world, int size, long seed,
                                     int startX, int floorY, int startZ, int pasted, String markers) {
        String path = "generated-regions." + regionId;
        generatedRegions.set(path + ".level", level.id());
        generatedRegions.set(path + ".world", world.getName());
        generatedRegions.set(path + ".size", size);
        generatedRegions.set(path + ".cell-size", defaultCellSize);
        generatedRegions.set(path + ".seed", Long.toUnsignedString(seed));
        generatedRegions.set(path + ".origin.x", startX);
        generatedRegions.set(path + ".origin.y", floorY);
        generatedRegions.set(path + ".origin.z", startZ);
        generatedRegions.set(path + ".templates", pasted);
        generatedRegions.set(path + ".markers", markers);
        try {
            generatedRegions.save(generatedRegionsFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save generated region metadata: " + exception.getMessage());
        }
    }

    private List<CellCoordinate> neighbors(CellCoordinate cell, int size) {
        List<CellCoordinate> neighbors = new ArrayList<>();
        for (TemplateConnector connector : TemplateConnector.values()) {
            int x = cell.x() + connector.deltaX();
            int z = cell.z() + connector.deltaZ();
            if (x >= 0 && x < size && z >= 0 && z < size) {
                neighbors.add(new CellCoordinate(x, z));
            }
        }
        return neighbors;
    }

    private WorldGenerationResult failure(String messageKey, BackroomsLevel level, String regionId, String reason) {
        return new WorldGenerationResult(false, messageKey, level.id(), level.world(), regionId, 0, 0, 0, "none", reason);
    }

    private int align(int value, int cellSize) {
        return Math.floorDiv(value, cellSize) * cellSize;
    }

    private int clampOdd(int value, int min, int max) {
        int clamped = Math.max(min, Math.min(max, value));
        return clamped % 2 == 0 ? clamped + 1 : clamped;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int manhattan(CellCoordinate first, CellCoordinate second) {
        return Math.abs(first.x() - second.x()) + Math.abs(first.z() - second.z());
    }

    private long parseSeed(String seedInput) {
        if (seedInput == null || seedInput.isBlank()) {
            return System.currentTimeMillis();
        }
        try {
            return Long.parseLong(seedInput);
        } catch (NumberFormatException ignored) {
            return seedInput.hashCode();
        }
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    private record CellCoordinate(int x, int z) {
    }

    private record TemplateChoice(SchematicTemplateDefinition template, int rotation) {
    }

    private record PlannedCell(int x, int z, SchematicTemplateDefinition template, int rotation) {
    }

    public record SchematicScaffoldResult(boolean success, int written, int skipped, int failed, String detail) {
    }
}
