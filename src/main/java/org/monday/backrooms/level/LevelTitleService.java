package org.monday.backrooms.level;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.monday.backrooms.Backrooms;

public final class LevelTitleService {

    private static final long MILLIS_PER_TICK = 50L;

    private final Backrooms plugin;

    public LevelTitleService(Backrooms plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, BackroomsLevel level) {
        if (!plugin.getConfig().getBoolean("level-title.enabled", true)) {
            return;
        }

        Component title = plugin.messages().parse(level.title());
        Component subtitle = plugin.messages().parse(level.subtitle());
        Title.Times times = Title.Times.times(
                ticksToDuration(plugin.getConfig().getLong("level-title.fade-in-ticks", 10L)),
                ticksToDuration(plugin.getConfig().getLong("level-title.stay-ticks", 60L)),
                ticksToDuration(plugin.getConfig().getLong("level-title.fade-out-ticks", 20L))
        );

        player.showTitle(Title.title(title, subtitle, times));
    }

    public boolean showOnJoin() {
        return plugin.getConfig().getBoolean("level-title.show-on-join", false);
    }

    private Duration ticksToDuration(long ticks) {
        return Duration.ofMillis(Math.max(0L, ticks) * MILLIS_PER_TICK);
    }
}
