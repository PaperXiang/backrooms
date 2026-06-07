package org.monday.backrooms.base;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.transition.BlockPosition;
import org.monday.backrooms.transition.CuboidRegion;

public final class BaseService {

    private static final String BASE_BYPASS_PERMISSION = "backrooms.base.bypass";

    private final Backrooms plugin;
    private final Map<String, BaseDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, BaseClaim> claims = new LinkedHashMap<>();
    private boolean enabled;
    private int maxClaimsPerPlayer = 1;
    private File dataFile;

    public BaseService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        definitions.clear();
        claims.clear();

        enabled = plugin.configFiles().bases().getBoolean("bases.enabled", true);
        maxClaimsPerPlayer = Math.max(1, plugin.configFiles().bases().getInt("bases.max-claims-per-player", 1));
        dataFile = new File(plugin.getDataFolder(), plugin.configFiles().bases().getString("bases.data-file", "base-claims.yml"));

        ConfigurationSection section = plugin.configFiles().bases().getConfigurationSection("bases.definitions");
        if (section == null) {
            plugin.getLogger().warning("Bases are enabled, but 'bases.definitions' is missing.");
            loadClaims();
            return;
        }

        int skipped = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection definitionSection = section.getConfigurationSection(id);
            if (definitionSection == null) {
                skipped++;
                continue;
            }
            Optional<BaseDefinition> definition = loadDefinition(id, definitionSection);
            if (definition.isEmpty()) {
                skipped++;
                continue;
            }
            definitions.put(definition.get().id(), definition.get());
        }

        loadClaims();
        plugin.getLogger().info("Loaded bases: enabled=" + enabled
                + ", definitions=" + definitions.size()
                + ", claims=" + claims.size()
                + ", skipped=" + skipped + ".");
    }

    public int definitionCount() {
        return definitions.size();
    }

    public int claimCount() {
        return claims.size();
    }

    public Collection<BaseDefinition> all() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public Optional<BaseDefinition> get(String id) {
        return Optional.ofNullable(definitions.get(normalize(id)));
    }

    public Optional<BaseClaim> claim(String id) {
        return Optional.ofNullable(claims.get(normalize(id)));
    }

    public Optional<BaseDefinition> getByTerminal(Block block) {
        if (block == null) {
            return Optional.empty();
        }
        return definitions.values().stream()
                .filter(BaseDefinition::enabled)
                .filter(definition -> definition.terminal() != null)
                .filter(definition -> definition.world().equals(block.getWorld().getName()))
                .filter(definition -> definition.terminal().matches(block))
                .findFirst();
    }

    public boolean canBuild(Player player, Location location) {
        if (!enabled || player.hasPermission(BASE_BYPASS_PERMISSION)) {
            return player.hasPermission(BASE_BYPASS_PERMISSION);
        }
        return claims.values().stream()
                .filter(claim -> claim.owner().equals(player.getUniqueId()))
                .map(claim -> definitions.get(claim.baseId()))
                .filter(definition -> definition != null && definition.enabled())
                .anyMatch(definition -> definition.world().equals(location.getWorld().getName())
                        && definition.region().contains(location));
    }

    public BaseClaimResult claim(Player player, String id) {
        BaseDefinition definition = definitions.get(normalize(id));
        if (definition == null) {
            return BaseClaimResult.failed(BaseClaimStatus.NOT_FOUND, null, null);
        }
        if (!enabled || !definition.enabled()) {
            return BaseClaimResult.failed(BaseClaimStatus.DISABLED, definition, claims.get(definition.id()));
        }
        if (claims.containsKey(definition.id())) {
            return BaseClaimResult.failed(BaseClaimStatus.ALREADY_CLAIMED, definition, claims.get(definition.id()));
        }
        if (!player.getWorld().getName().equals(definition.world()) || !definition.region().contains(player.getLocation())) {
            return BaseClaimResult.failed(BaseClaimStatus.WRONG_LEVEL, definition, null);
        }
        long ownedClaims = claims.values().stream().filter(claim -> claim.owner().equals(player.getUniqueId())).count();
        if (ownedClaims >= maxClaimsPerPlayer) {
            return BaseClaimResult.failed(BaseClaimStatus.CLAIM_LIMIT_REACHED, definition, null);
        }

        BaseClaim claim = new BaseClaim(definition.id(), player.getUniqueId(), player.getName(), Instant.now());
        claims.put(definition.id(), claim);
        if (!saveClaims()) {
            claims.remove(definition.id());
            return BaseClaimResult.failed(BaseClaimStatus.SAVE_FAILED, definition, claim);
        }
        plugin.getLogger().info("Base '" + definition.id() + "' claimed by " + player.getName() + ".");
        return BaseClaimResult.success(definition, claim);
    }

    public BaseClaimResult forceClaim(String id, UUID owner, String ownerName) {
        BaseDefinition definition = definitions.get(normalize(id));
        if (definition == null) {
            return BaseClaimResult.failed(BaseClaimStatus.NOT_FOUND, null, null);
        }
        if (!enabled || !definition.enabled()) {
            return BaseClaimResult.failed(BaseClaimStatus.DISABLED, definition, claims.get(definition.id()));
        }
        if (claims.containsKey(definition.id())) {
            return BaseClaimResult.failed(BaseClaimStatus.ALREADY_CLAIMED, definition, claims.get(definition.id()));
        }

        BaseClaim claim = new BaseClaim(definition.id(), owner, ownerName == null || ownerName.isBlank() ? owner.toString() : ownerName, Instant.now());
        claims.put(definition.id(), claim);
        if (!saveClaims()) {
            claims.remove(definition.id());
            return BaseClaimResult.failed(BaseClaimStatus.SAVE_FAILED, definition, claim);
        }
        plugin.getLogger().info("Base '" + definition.id() + "' force-claimed by " + claim.ownerName() + " (" + owner + ").");
        return BaseClaimResult.success(definition, claim);
    }

    public BaseUnclaimResult unclaim(String id) {
        BaseDefinition definition = definitions.get(normalize(id));
        if (definition == null) {
            return new BaseUnclaimResult(BaseUnclaimStatus.NOT_FOUND, null, null);
        }
        BaseClaim claim = claims.remove(definition.id());
        if (claim == null) {
            return new BaseUnclaimResult(BaseUnclaimStatus.NOT_CLAIMED, definition, null);
        }
        if (!saveClaims()) {
            claims.put(definition.id(), claim);
            return new BaseUnclaimResult(BaseUnclaimStatus.SAVE_FAILED, definition, claim);
        }
        plugin.getLogger().info("Base '" + definition.id() + "' unclaimed from " + claim.ownerName() + " (" + claim.owner() + ").");
        return new BaseUnclaimResult(BaseUnclaimStatus.SUCCESS, definition, claim);
    }

    private Optional<BaseDefinition> loadDefinition(String id, ConfigurationSection section) {
        String normalizedId = normalize(id);
        String level = normalize(section.getString("level", ""));
        if (level.isBlank() || plugin.levels().get(level).isEmpty()) {
            plugin.getLogger().warning("Skipping base '" + id + "' because level is invalid.");
            return Optional.empty();
        }
        String world = section.getString("world", plugin.levels().get(level).map(levelConfig -> levelConfig.world()).orElse(""));
        if (world.isBlank()) {
            plugin.getLogger().warning("Skipping base '" + id + "' because world is invalid.");
            return Optional.empty();
        }

        Optional<CuboidRegion> region = loadRegion(section.getConfigurationSection("region"), id);
        if (region.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new BaseDefinition(
                normalizedId,
                section.getBoolean("enabled", true),
                section.getString("display-name", id),
                level,
                world,
                region.get(),
                loadBlockPosition(section.getConfigurationSection("terminal")).orElse(null)
        ));
    }

    private Optional<CuboidRegion> loadRegion(ConfigurationSection section, String id) {
        if (section == null) {
            plugin.getLogger().warning("Skipping base '" + id + "' because region is missing.");
            return Optional.empty();
        }
        return Optional.of(new CuboidRegion(
                section.getDouble("min.x"),
                section.getDouble("min.y"),
                section.getDouble("min.z"),
                section.getDouble("max.x"),
                section.getDouble("max.y"),
                section.getDouble("max.z")
        ));
    }

    private Optional<BlockPosition> loadBlockPosition(ConfigurationSection section) {
        if (section == null) {
            return Optional.empty();
        }
        return Optional.of(new BlockPosition(section.getInt("x"), section.getInt("y"), section.getInt("z")));
    }

    private void loadClaims() {
        if (dataFile == null || !dataFile.exists()) {
            return;
        }

        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = data.getConfigurationSection("claims");
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            String normalizedId = normalize(id);
            if (!definitions.containsKey(normalizedId)) {
                plugin.getLogger().warning("Ignoring claim for unknown base '" + id + "'.");
                continue;
            }
            String owner = section.getString(id + ".owner", "");
            try {
                claims.put(normalizedId, new BaseClaim(
                        normalizedId,
                        UUID.fromString(owner),
                        section.getString(id + ".owner-name", "unknown"),
                        Instant.ofEpochMilli(section.getLong(id + ".claimed-at", System.currentTimeMillis()))
                ));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Ignoring claim for base '" + id + "' because owner UUID is invalid.");
            }
        }
    }

    private boolean saveClaims() {
        YamlConfiguration data = new YamlConfiguration();
        for (BaseClaim claim : claims.values()) {
            String path = "claims." + claim.baseId();
            data.set(path + ".owner", claim.owner().toString());
            data.set(path + ".owner-name", claim.ownerName());
            data.set(path + ".claimed-at", claim.claimedAt().toEpochMilli());
        }

        try {
            File parent = dataFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                plugin.getLogger().warning("Could not create base claims directory: " + parent.getPath());
                return false;
            }
            data.save(dataFile);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save base claims: " + exception.getMessage());
            return false;
        }
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    public enum BaseUnclaimStatus {
        SUCCESS,
        NOT_FOUND,
        NOT_CLAIMED,
        SAVE_FAILED
    }

    public record BaseUnclaimResult(
            BaseUnclaimStatus status,
            BaseDefinition definition,
            BaseClaim claim
    ) {
    }
}
