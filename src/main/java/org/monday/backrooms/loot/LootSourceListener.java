package org.monday.backrooms.loot;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.monday.backrooms.Backrooms;

public final class LootSourceListener implements Listener {

    private final Backrooms plugin;

    public LootSourceListener(Backrooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BlockState state)) {
            return;
        }

        plugin.lootSources().handleVanillaContainerOpen(player, state.getBlock(), event.getInventory());
    }
}
