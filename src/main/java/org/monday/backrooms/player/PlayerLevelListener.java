package org.monday.backrooms.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.monday.backrooms.Backrooms;

public final class PlayerLevelListener implements Listener {

    private final Backrooms plugin;

    public PlayerLevelListener(Backrooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.playerLevels().updateFromWorld(event.getPlayer(), plugin.levelTitles().showOnJoin());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.playerLevels().leave(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.playerLevels().updateFromWorld(event.getPlayer(), true);
    }
}
