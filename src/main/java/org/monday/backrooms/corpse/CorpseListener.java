package org.monday.backrooms.corpse;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.monday.backrooms.Backrooms;

public final class CorpseListener implements Listener {

    private final Backrooms plugin;

    public CorpseListener(Backrooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.corpses().handleDeath(event);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.corpses().handleRespawn(event);
    }
}
