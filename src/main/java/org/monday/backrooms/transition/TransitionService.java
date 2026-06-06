package org.monday.backrooms.transition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;
import org.monday.backrooms.player.PlayerLevelState;

public final class TransitionService {

    private static final String USE_PERMISSION = "backrooms.transition.use";
    private static final String COOLDOWN_BYPASS_PERMISSION = "backrooms.transition.bypass.cooldown";

    private final Backrooms plugin;
    private final Map<String, TransitionDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, List<TransitionDefinition>> bySourceLevel = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> postTeleportImmunity = new HashMap<>();
    private boolean enabled;
    private long postTeleportImmunityTicks;

    public TransitionService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        definitions.clear();
        bySourceLevel.clear();
        cooldowns.clear();
        postTeleportImmunity.clear();

        this.enabled = plugin.configFiles().transitions().getBoolean("transitions.enabled", true);
        this.postTeleportImmunityTicks = plugin.configFiles().transitions().getLong("transitions.defaults.post-teleport-immunity-ticks", 40L);
        if (!enabled) {
            plugin.getLogger().info("Transitions disabled by config.");
            return;
        }

        ConfigurationSection section = plugin.configFiles().transitions().getConfigurationSection("transitions.definitions");
        if (section == null) {
            plugin.getLogger().warning("Transitions are enabled, but 'transitions.definitions' is missing.");
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection definitionSection = section.getConfigurationSection(id);
            if (definitionSection == null) {
                plugin.getLogger().warning("Skipping invalid transition definition section: " + id);
                skipped++;
                continue;
            }

            Optional<TransitionDefinition> definition = loadDefinition(id, definitionSection);
            if (definition.isEmpty()) {
                skipped++;
                continue;
            }

            TransitionDefinition loaded = definition.get();
            String normalizedId = normalize(loaded.id());
            if (definitions.containsKey(normalizedId)) {
                plugin.getLogger().warning("Skipping duplicate transition id: " + loaded.id());
                skipped++;
                continue;
            }

            definitions.put(normalizedId, loaded);
            bySourceLevel.computeIfAbsent(normalize(loaded.fromLevel()), ignored -> new ArrayList<>()).add(loaded);
        }

        plugin.getLogger().info("Loaded transitions: enabled=true, definitions=" + definitions.size()
                + ", skipped=" + skipped + ".");
    }

    public int definitionCount() {
        return definitions.size();
    }

    public Collection<TransitionDefinition> all() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public Optional<TransitionDefinition> get(String id) {
        return Optional.ofNullable(definitions.get(normalize(id)));
    }

    public boolean handleRegionMove(Player player, Location from, Location to) {
        if (!enabled || to == null || isImmune(player)) {
            return false;
        }

        Optional<String> sourceLevel = currentLevelId(player);
        if (sourceLevel.isEmpty()) {
            return false;
        }

        for (TransitionDefinition definition : bySourceLevel.getOrDefault(normalize(sourceLevel.get()), List.of())) {
            if (!definition.enabled() || definition.triggerType() != TransitionTriggerType.REGION || definition.region() == null) {
                continue;
            }
            if (!to.getWorld().getName().equalsIgnoreCase(definition.triggerWorld())) {
                continue;
            }
            boolean wasInside = from != null && from.getWorld().equals(to.getWorld()) && definition.region().contains(from);
            if (!wasInside && definition.region().contains(to)) {
                return execute(player, definition, false);
            }
        }
        return false;
    }

    public boolean handleBlockInteract(Player player, Block block) {
        if (!enabled || block == null || isImmune(player)) {
            return false;
        }

        Optional<String> sourceLevel = currentLevelId(player);
        if (sourceLevel.isEmpty()) {
            return false;
        }

        for (TransitionDefinition definition : bySourceLevel.getOrDefault(normalize(sourceLevel.get()), List.of())) {
            if (definition.enabled() && definition.matchesBlock(block)) {
                return execute(player, definition, false);
            }
        }
        return false;
    }

    public boolean triggerByCommand(Player player, TransitionDefinition definition) {
        return execute(player, definition, true);
    }

    public boolean showGuide(Player player, TransitionDefinition definition) {
        World world = Bukkit.getWorld(definition.triggerWorld());
        if (world == null) {
            plugin.messages().send(player, "transition-world-not-loaded", plugin.messages().text("world", definition.triggerWorld()));
            return false;
        }

        if (definition.triggerType() == TransitionTriggerType.REGION && definition.region() != null) {
            showRegionGuide(world, definition.region());
        }
        for (BlockPosition position : definition.blockPositions()) {
            showBlockGuide(world, position);
        }

        plugin.messages().send(player, "transition-guide-shown",
                plugin.messages().text("id", definition.id()),
                plugin.messages().text("world", definition.triggerWorld()),
                plugin.messages().text("trigger", definition.triggerDescription())
        );
        return true;
    }

    private boolean execute(Player player, TransitionDefinition definition, boolean commandMode) {
        if (!definition.enabled()) {
            plugin.messages().send(player, "transition-disabled", plugin.messages().text("id", definition.id()));
            return false;
        }

        if (!commandMode) {
            if (!player.hasPermission(USE_PERMISSION)) {
                plugin.messages().send(player, "no-permission");
                return false;
            }
            if (!definition.permission().isBlank() && !player.hasPermission(definition.permission())) {
                plugin.messages().send(player, "transition-no-permission");
                return false;
            }
            if (!sourceMatches(player, definition)) {
                plugin.messages().send(player, "transition-source-invalid");
                return false;
            }

            long remainingMillis = remainingCooldown(player, definition);
            if (remainingMillis > 0L && !player.hasPermission(COOLDOWN_BYPASS_PERMISSION)) {
                plugin.messages().send(player, "transition-cooldown",
                        plugin.messages().text("seconds", String.valueOf(Math.max(1L, (remainingMillis + 999L) / 1000L)))
                );
                return false;
            }
        }

        Optional<ResolvedTarget> resolvedTarget = resolveTarget(player, definition);
        if (resolvedTarget.isEmpty()) {
            return false;
        }

        ResolvedTarget target = resolvedTarget.get();
        plugin.getLogger().info("Transition '" + definition.id() + "' moving " + player.getName()
                + " to " + target.location().getWorld().getName() + " "
                + target.location().getBlockX() + "," + target.location().getBlockY() + "," + target.location().getBlockZ() + ".");
        boolean teleported = player.teleport(target.location(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        if (!teleported) {
            plugin.messages().send(player, "transition-failed", plugin.messages().text("id", definition.id()));
            return false;
        }

        startCooldown(player, definition);
        startImmunity(player);
        target.level().ifPresentOrElse(
                level -> plugin.playerLevels().enter(player, level, definition.showTitle(), true),
                () -> plugin.playerLevels().updateFromWorld(player, definition.showTitle())
        );

        if (definition.sound() != null) {
            player.playSound(player.getLocation(), definition.sound(), 0.8F, 1.0F);
        }

        plugin.messages().send(player, definition.messageKey(),
                plugin.messages().text("id", definition.id()),
                plugin.messages().mini("display", definition.displayName()),
                plugin.messages().text("from", definition.fromLevel()),
                plugin.messages().text("target", target.targetId()),
                plugin.messages().mini("target_display", target.targetDisplay()),
                plugin.messages().text("world", target.location().getWorld().getName())
        );
        return true;
    }

    private Optional<ResolvedTarget> resolveTarget(Player player, TransitionDefinition definition) {
        TransitionTarget target = definition.target();
        if (target.type() == TransitionTargetType.LEVEL) {
            Optional<BackroomsLevel> level = plugin.levels().get(target.level());
            if (level.isEmpty()) {
                plugin.messages().send(player, "transition-target-level-not-found",
                        plugin.messages().text("id", definition.id()),
                        plugin.messages().text("target", target.level())
                );
                return Optional.empty();
            }
            if (!level.get().enabled()) {
                plugin.messages().send(player, "transition-target-level-disabled",
                        plugin.messages().text("id", definition.id()),
                        plugin.messages().text("target", target.level())
                );
                return Optional.empty();
            }

            World world = Bukkit.getWorld(level.get().world());
            if (world == null) {
                plugin.messages().send(player, "transition-world-not-loaded",
                        plugin.messages().text("world", level.get().world())
                );
                return Optional.empty();
            }
            Location location = resolveLocation(world, target, level.get());
            return Optional.of(new ResolvedTarget(location, Optional.of(level.get()), level.get().id(), level.get().displayName()));
        }

        World world = Bukkit.getWorld(target.world());
        if (world == null) {
            plugin.messages().send(player, "transition-world-not-loaded", plugin.messages().text("world", target.world()));
            return Optional.empty();
        }
        return Optional.of(new ResolvedTarget(resolveLocation(world, target, null), Optional.empty(), target.world(), target.world()));
    }

    private Location resolveLocation(World world, TransitionTarget target, BackroomsLevel targetLevel) {
        return switch (target.spawnMode()) {
            case POINT -> new Location(world, target.x(), target.y(), target.z(), target.yaw(), target.pitch());
            case LEVEL_SPAWN -> targetLevel == null || targetLevel.spawn() == null ? world.getSpawnLocation() : targetLevel.spawn().toLocation(world);
            case WORLD_SPAWN -> world.getSpawnLocation();
        };
    }

    private Optional<TransitionDefinition> loadDefinition(String id, ConfigurationSection section) {
        String fromLevel = section.getString("from.level", section.getString("from", ""));
        if (fromLevel.isBlank()) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because from.level is missing.");
            return Optional.empty();
        }
        Optional<BackroomsLevel> sourceLevel = plugin.levels().get(fromLevel);
        if (sourceLevel.isEmpty()) {
            plugin.getLogger().warning("Transition '" + id + "' references unknown source level '" + fromLevel + "'.");
        }

        ConfigurationSection triggerSection = section.getConfigurationSection("trigger");
        if (triggerSection == null) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because trigger is missing.");
            return Optional.empty();
        }
        Optional<TransitionTriggerType> triggerType = TransitionTriggerType.fromConfig(triggerSection.getString("type", "region"));
        if (triggerType.isEmpty()) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because trigger.type is invalid.");
            return Optional.empty();
        }

        String triggerWorld = triggerSection.getString("world", sourceLevel.map(BackroomsLevel::world).orElse(fromLevel));
        CuboidRegion region = null;
        if (triggerType.get() == TransitionTriggerType.REGION) {
            region = loadRegion(id, triggerSection.getConfigurationSection("region")).orElse(null);
            if (region == null) {
                return Optional.empty();
            }
        }

        Set<Material> materials = loadMaterials(triggerSection.getStringList("materials"), "transition " + id);
        List<BlockPosition> positions = loadPositions(triggerSection.getMapList("locations"), id);
        if (triggerType.get() == TransitionTriggerType.RIGHT_CLICK_BLOCK && materials.isEmpty() && positions.isEmpty()) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because right_click_block needs materials or locations.");
            return Optional.empty();
        }

        Optional<TransitionTarget> target = loadTarget(id, section.getConfigurationSection("target"));
        if (target.isEmpty()) {
            return Optional.empty();
        }

        String permission = section.getString("conditions.permission", section.getString("permission", ""));
        String messageKey = section.getString("feedback.message-key",
                target.get().type() == TransitionTargetType.LEVEL ? "transition-success-level" : "transition-success-world");
        Sound sound = parseSound(section.getString("feedback.sound", "ENTITY_ENDERMAN_TELEPORT"), id).orElse(null);

        return Optional.of(new TransitionDefinition(
                id,
                section.getBoolean("enabled", true),
                section.getString("display-name", id),
                fromLevel,
                triggerWorld,
                triggerType.get(),
                region,
                Set.copyOf(materials),
                List.copyOf(positions),
                target.get(),
                section.getLong("cooldown-seconds", plugin.configFiles().transitions().getLong("transitions.defaults.cooldown-seconds", 5L)),
                permission,
                messageKey,
                section.getBoolean("feedback.title", target.get().type() == TransitionTargetType.LEVEL),
                sound
        ));
    }

    private Optional<CuboidRegion> loadRegion(String id, ConfigurationSection section) {
        if (section == null || section.getConfigurationSection("min") == null || section.getConfigurationSection("max") == null) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because trigger.region min/max is missing.");
            return Optional.empty();
        }
        ConfigurationSection min = section.getConfigurationSection("min");
        ConfigurationSection max = section.getConfigurationSection("max");
        if (!hasCoordinates(min) || !hasCoordinates(max)) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because region min/max requires x, y and z.");
            return Optional.empty();
        }
        return Optional.of(new CuboidRegion(
                min.getDouble("x"), min.getDouble("y"), min.getDouble("z"),
                max.getDouble("x"), max.getDouble("y"), max.getDouble("z")
        ));
    }

    private Optional<TransitionTarget> loadTarget(String id, ConfigurationSection section) {
        if (section == null) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because target is missing.");
            return Optional.empty();
        }
        Optional<TransitionTargetType> targetType = TransitionTargetType.fromConfig(section.getString("type", "level"));
        if (targetType.isEmpty()) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because target.type is invalid.");
            return Optional.empty();
        }

        String level = section.getString("level", "");
        String world = section.getString("world", "");
        if (targetType.get() == TransitionTargetType.LEVEL && level.isBlank()) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because target.level is missing.");
            return Optional.empty();
        }
        if (targetType.get() == TransitionTargetType.WORLD && world.isBlank()) {
            plugin.getLogger().warning("Skipping transition '" + id + "' because target.world is missing.");
            return Optional.empty();
        }

        ConfigurationSection spawn = section.getConfigurationSection("spawn");
        String fallbackMode = targetType.get() == TransitionTargetType.LEVEL ? "level-spawn" : "world-spawn";
        TransitionSpawnMode mode = TransitionSpawnMode.fromConfig(spawn == null ? fallbackMode : spawn.getString("mode", fallbackMode))
                .orElse(targetType.get() == TransitionTargetType.LEVEL ? TransitionSpawnMode.LEVEL_SPAWN : TransitionSpawnMode.WORLD_SPAWN);

        return Optional.of(new TransitionTarget(
                targetType.get(),
                level,
                world,
                mode,
                spawn == null ? 0.5D : spawn.getDouble("x", 0.5D),
                spawn == null ? 64.0D : spawn.getDouble("y", 64.0D),
                spawn == null ? 0.5D : spawn.getDouble("z", 0.5D),
                spawn == null ? 0.0F : (float) spawn.getDouble("yaw", 0.0D),
                spawn == null ? 0.0F : (float) spawn.getDouble("pitch", 0.0D)
        ));
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

    private List<BlockPosition> loadPositions(List<Map<?, ?>> maps, String id) {
        List<BlockPosition> positions = new ArrayList<>();
        for (Map<?, ?> map : maps) {
            Object x = map.get("x");
            Object y = map.get("y");
            Object z = map.get("z");
            if (!(x instanceof Number xNumber) || !(y instanceof Number yNumber) || !(z instanceof Number zNumber)) {
                plugin.getLogger().warning("Skipping invalid block location in transition '" + id + "'.");
                continue;
            }
            positions.add(new BlockPosition(xNumber.intValue(), yNumber.intValue(), zNumber.intValue()));
        }
        return positions;
    }

    private Optional<Sound> parseSound(String soundName, String id) {
        if (soundName == null || soundName.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Sound.valueOf(soundName.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Unknown sound '" + soundName + "' in transition '" + id + "'.");
            return Optional.empty();
        }
    }

    private boolean sourceMatches(Player player, TransitionDefinition definition) {
        return currentLevelId(player).map(levelId -> levelId.equalsIgnoreCase(definition.fromLevel())).orElse(false);
    }

    private void showRegionGuide(World world, CuboidRegion region) {
        double centerX = (region.minX() + region.maxX()) / 2.0D;
        double centerY = (region.minY() + region.maxY()) / 2.0D;
        double centerZ = (region.minZ() + region.maxZ()) / 2.0D;
        double offsetX = Math.max(0.25D, (region.maxX() - region.minX()) / 2.0D);
        double offsetY = Math.max(0.25D, (region.maxY() - region.minY()) / 2.0D);
        double offsetZ = Math.max(0.25D, (region.maxZ() - region.minZ()) / 2.0D);
        world.spawnParticle(Particle.PORTAL, new Location(world, centerX, centerY, centerZ), 80, offsetX, offsetY, offsetZ, 0.01D);

        spawnCorner(world, region.minX(), region.minY(), region.minZ());
        spawnCorner(world, region.minX(), region.minY(), region.maxZ());
        spawnCorner(world, region.minX(), region.maxY(), region.minZ());
        spawnCorner(world, region.minX(), region.maxY(), region.maxZ());
        spawnCorner(world, region.maxX(), region.minY(), region.minZ());
        spawnCorner(world, region.maxX(), region.minY(), region.maxZ());
        spawnCorner(world, region.maxX(), region.maxY(), region.minZ());
        spawnCorner(world, region.maxX(), region.maxY(), region.maxZ());
    }

    private void showBlockGuide(World world, BlockPosition position) {
        world.spawnParticle(Particle.END_ROD, new Location(world, position.x() + 0.5D, position.y() + 1.2D, position.z() + 0.5D), 30, 0.3D, 0.6D, 0.3D, 0.02D);
    }

    private void spawnCorner(World world, double x, double y, double z) {
        world.spawnParticle(Particle.END_ROD, new Location(world, x, y, z), 8, 0.05D, 0.05D, 0.05D, 0.0D);
    }

    private Optional<String> currentLevelId(Player player) {
        return plugin.playerLevels().current(player)
                .map(PlayerLevelState::levelId)
                .or(() -> plugin.levels().getByWorld(player.getWorld().getName()).map(BackroomsLevel::id));
    }

    private boolean hasCoordinates(ConfigurationSection section) {
        return section != null && section.contains("x") && section.contains("y") && section.contains("z");
    }

    private long remainingCooldown(Player player, TransitionDefinition definition) {
        if (definition.cooldownSeconds() <= 0L) {
            return 0L;
        }
        return cooldowns.getOrDefault(cooldownKey(player, definition), 0L) - System.currentTimeMillis();
    }

    private void startCooldown(Player player, TransitionDefinition definition) {
        if (definition.cooldownSeconds() > 0L) {
            cooldowns.put(cooldownKey(player, definition), System.currentTimeMillis() + definition.cooldownSeconds() * 1000L);
        }
    }

    private boolean isImmune(Player player) {
        return postTeleportImmunity.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis();
    }

    private void startImmunity(Player player) {
        if (postTeleportImmunityTicks <= 0L) {
            return;
        }
        postTeleportImmunity.put(player.getUniqueId(), System.currentTimeMillis() + postTeleportImmunityTicks * 50L);
    }

    private String cooldownKey(Player player, TransitionDefinition definition) {
        return definition.id() + ":" + player.getUniqueId();
    }

    private String normalize(String input) {
        return input.toLowerCase(Locale.ROOT);
    }

    private record ResolvedTarget(
            Location location,
            Optional<BackroomsLevel> level,
            String targetId,
            String targetDisplay
    ) {
    }
}
