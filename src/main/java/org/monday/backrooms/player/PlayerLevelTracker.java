package org.monday.backrooms.player;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;

public final class PlayerLevelTracker {

    private final Backrooms plugin;
    private final Map<UUID, PlayerLevelState> states = new HashMap<>();

    public PlayerLevelTracker(Backrooms plugin) {
        this.plugin = plugin;
    }

    public Optional<PlayerLevelState> current(Player player) {
        return current(player.getUniqueId());
    }

    public Optional<PlayerLevelState> current(UUID playerId) {
        return Optional.ofNullable(states.get(playerId));
    }

    public void updateFromWorld(Player player, boolean showTitle) {
        plugin.levels().getByWorld(player.getWorld().getName()).ifPresentOrElse(
                level -> enter(player, level, showTitle, false),
                () -> leave(player.getUniqueId())
        );
    }

    public void enter(Player player, BackroomsLevel level, boolean showTitle, boolean forceTitle) {
        PlayerLevelState previous = states.get(player.getUniqueId());
        boolean sameLevel = previous != null && previous.levelId().equalsIgnoreCase(level.id());
        Instant enteredAt = sameLevel ? previous.enteredAt() : Instant.now();

        states.put(player.getUniqueId(), new PlayerLevelState(
                player.getUniqueId(),
                level.id(),
                player.getWorld().getName(),
                enteredAt
        ));

        if (showTitle && (forceTitle || !sameLevel)) {
            plugin.levelTitles().show(player, level);
        }
    }

    public void leave(UUID playerId) {
        states.remove(playerId);
    }

    public void reconcileOnlinePlayers(boolean showTitle) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updateFromWorld(player, showTitle);
        }
    }

    public void clear() {
        states.clear();
    }
}
