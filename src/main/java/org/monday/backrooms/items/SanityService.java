package org.monday.backrooms.items;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.hud.SanityHudSnapshot;
import org.monday.backrooms.level.BackroomsLevel;

public final class SanityService {

    private final Backrooms plugin;
    private final Map<UUID, Double> sanity = new HashMap<>();
    private final Map<UUID, Long> stabilizedUntil = new HashMap<>();
    private final Map<UUID, Long> lowWarningCooldowns = new HashMap<>();
    private final Map<String, Double> levelDecayPerSecond = new HashMap<>();
    private BukkitTask task;
    private boolean enabled = true;
    private double maxSanity = 100.0D;
    private double defaultSanity = 100.0D;
    private double defaultDecayPerSecond = 0.015D;
    private double lowThreshold = 35.0D;
    private double criticalThreshold = 15.0D;

    public SanityService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        ConfigurationSection section = plugin.configFiles().items().getConfigurationSection("sanity");
        if (section == null) {
            plugin.getLogger().warning("Missing 'sanity' section in items.yml; using sanity defaults.");
            return;
        }

        enabled = section.getBoolean("enabled", true);
        maxSanity = Math.max(1.0D, section.getDouble("max", 100.0D));
        defaultSanity = clamp(section.getDouble("default", maxSanity), 0.0D, maxSanity);
        defaultDecayPerSecond = Math.max(0.0D, section.getDouble("decay.default-per-second", 0.015D));
        lowThreshold = clamp(section.getDouble("thresholds.low", 35.0D), 0.0D, maxSanity);
        criticalThreshold = clamp(section.getDouble("thresholds.critical", 15.0D), 0.0D, maxSanity);

        levelDecayPerSecond.clear();
        ConfigurationSection levels = section.getConfigurationSection("decay.levels");
        if (levels != null) {
            for (String levelId : levels.getKeys(false)) {
                levelDecayPerSecond.put(normalize(levelId), Math.max(0.0D, levels.getDouble(levelId)));
            }
        }

        for (UUID playerId : sanity.keySet()) {
            sanity.computeIfPresent(playerId, (id, value) -> clamp(value, 0.0D, maxSanity));
        }
        if (!enabled) {
            plugin.sanityHud().clear();
        }
        plugin.getLogger().info("Loaded sanity config: enabled=" + enabled
                + ", default=" + defaultSanity
                + ", max=" + maxSanity
                + ", levelDecayRules=" + levelDecayPerSecond.size() + ".");
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void clear() {
        sanity.clear();
        stabilizedUntil.clear();
        lowWarningCooldowns.clear();
    }

    public double current(Player player) {
        return sanity.computeIfAbsent(player.getUniqueId(), ignored -> defaultSanity);
    }

    public double maxSanity() {
        return maxSanity;
    }

    public double restore(Player player, double amount) {
        if (amount == 0.0D) {
            return current(player);
        }
        double updated = clamp(current(player) + amount, 0.0D, maxSanity);
        sanity.put(player.getUniqueId(), updated);
        return updated;
    }

    public void stabilize(Player player, double seconds) {
        if (seconds <= 0.0D) {
            return;
        }
        long until = System.currentTimeMillis() + Math.round(seconds * 1000.0D);
        stabilizedUntil.merge(player.getUniqueId(), until, Math::max);
    }

    public boolean isStabilized(Player player) {
        return remainingStabilizedMillis(player) > 0L;
    }

    public long remainingStabilizedMillis(Player player) {
        long until = stabilizedUntil.getOrDefault(player.getUniqueId(), 0L);
        long remaining = until - System.currentTimeMillis();
        if (remaining <= 0L) {
            stabilizedUntil.remove(player.getUniqueId());
            return 0L;
        }
        return remaining;
    }

    public String percentText(Player player) {
        return String.valueOf(Math.round(current(player)));
    }

    private void tick() {
        if (!enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Optional<BackroomsLevel> level = plugin.levels().getByWorld(player.getWorld().getName());
            if (level.isPresent() && level.get().enabled()) {
                tickBackroomsPlayer(player, level.get(), now);
            } else if (current(player) < maxSanity) {
                updateHud(player, null);
            } else {
                plugin.sanityHud().hide(player);
            }
        }
    }

    private void tickBackroomsPlayer(Player player, BackroomsLevel level, long now) {
        current(player);
        if (!isStabilized(player)) {
            double decay = levelDecayPerSecond.getOrDefault(normalize(level.id()), defaultDecayPerSecond);
            restore(player, -decay);
        }

        double current = current(player);
        if (current <= criticalThreshold) {
            sendLowWarning(player, now, "sanity-critical");
        } else if (current <= lowThreshold) {
            sendLowWarning(player, now, "sanity-low");
        }

        updateHud(player, level);
    }

    private void sendLowWarning(Player player, long now, String messageKey) {
        long next = lowWarningCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (next > now) {
            return;
        }
        lowWarningCooldowns.put(player.getUniqueId(), now + 30000L);
        plugin.messages().send(player, messageKey,
                plugin.messages().text("sanity", percentText(player)),
                plugin.messages().text("max", String.valueOf(Math.round(maxSanity)))
        );
    }

    private void updateHud(Player player, BackroomsLevel level) {
        long stableSeconds = Math.max(0L, (remainingStabilizedMillis(player) + 999L) / 1000L);
        String levelId = level == null ? "safe" : level.id();
        plugin.sanityHud().update(player, new SanityHudSnapshot(
                current(player),
                maxSanity,
                percentText(player),
                String.valueOf(Math.round(maxSanity)),
                levelId,
                stableSeconds,
                lowThreshold,
                criticalThreshold
        ));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
