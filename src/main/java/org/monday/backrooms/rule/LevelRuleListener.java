package org.monday.backrooms.rule;

import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.ProjectileSource;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;
import org.monday.backrooms.player.PlayerLevelState;

public final class LevelRuleListener implements Listener {

    private static final String BUILD_BYPASS_PERMISSION = "backrooms.bypass.build";

    private final Backrooms plugin;

    public LevelRuleListener(Backrooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<BackroomsLevel> level = currentLevel(event.getPlayer());
        if (level.isEmpty()) {
            return;
        }

        if (plugin.resources().handleBreak(event, level.get())) {
            return;
        }

        if (!level.get().rules().allowBlockBreak() && !event.getPlayer().hasPermission(BUILD_BYPASS_PERMISSION)) {
            event.setCancelled(true);
            plugin.messages().send(event.getPlayer(), "build-break-denied");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Optional<BackroomsLevel> level = currentLevel(event.getPlayer());
        if (level.isEmpty()) {
            return;
        }

        if (!level.get().rules().allowBlockPlace() && !event.getPlayer().hasPermission(BUILD_BYPASS_PERMISSION)) {
            event.setCancelled(true);
            plugin.messages().send(event.getPlayer(), "build-place-denied");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Optional<BackroomsLevel> level = currentLevel(event.getPlayer());
        if (level.isEmpty()) {
            return;
        }

        plugin.resources().handleInteract(event, level.get());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = attackingPlayer(event.getDamager());
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        Optional<BackroomsLevel> victimLevel = currentLevel(victim);
        Optional<BackroomsLevel> attackerLevel = currentLevel(attacker);
        if (victimLevel.isEmpty() && attackerLevel.isEmpty()) {
            return;
        }

        boolean pvpAllowed = victimLevel.map(BackroomsLevel::pvp).orElse(true)
                && attackerLevel.map(BackroomsLevel::pvp).orElse(true);
        if (!pvpAllowed) {
            event.setCancelled(true);
            plugin.messages().send(attacker, "pvp-denied");
        }
    }

    private Optional<BackroomsLevel> currentLevel(Player player) {
        return plugin.playerLevels().current(player)
                .map(PlayerLevelState::levelId)
                .flatMap(levelId -> plugin.levels().get(levelId).filter(BackroomsLevel::enabled));
    }

    private Player attackingPlayer(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }
}
