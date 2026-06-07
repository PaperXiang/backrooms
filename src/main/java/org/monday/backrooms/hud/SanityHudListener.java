package org.monday.backrooms.hud;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.monday.backrooms.Backrooms;

public final class SanityHudListener implements Listener {

    private final Backrooms plugin;

    public SanityHudListener(Backrooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.sanityHud().hide(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.sanityHud().hide(event.getPlayer());
    }
}
