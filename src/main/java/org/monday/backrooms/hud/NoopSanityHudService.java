package org.monday.backrooms.hud;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.monday.backrooms.Backrooms;

public final class NoopSanityHudService implements SanityHudService {

    private final Backrooms plugin;
    private final String reason;
    private boolean warned;

    public NoopSanityHudService(Backrooms plugin, String reason) {
        this.plugin = plugin;
        this.reason = reason;
    }

    @Override
    public void reload() {
        ConfigurationSection section = plugin.configFiles().items().getConfigurationSection("sanity.hud");
        boolean enabled = section != null && section.getBoolean("enabled", false);
        String provider = section == null ? "NONE" : section.getString("provider", "NONE");
        if (enabled && "VECTOR_DISPLAYS".equalsIgnoreCase(provider) && !warned) {
            warned = true;
            plugin.getLogger().warning("Sanity HUD is configured for VectorDisplays, but it is inactive: " + reason);
        }
    }

    @Override
    public void update(Player player, SanityHudSnapshot snapshot) {
        // Intentionally empty: the configured HUD provider is not available.
    }

    @Override
    public void hide(Player player) {
        // Intentionally empty.
    }

    @Override
    public void clear() {
        // Intentionally empty.
    }
}
