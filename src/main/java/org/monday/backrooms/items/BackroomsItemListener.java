package org.monday.backrooms.items;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.monday.backrooms.Backrooms;

public final class BackroomsItemListener implements Listener {

    private final Backrooms plugin;

    public BackroomsItemListener(Backrooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack stack = event.getItem();
        plugin.items().fromStack(stack)
                .filter(BackroomsItemDefinition::consumeOnRightClick)
                .ifPresent(definition -> useConsumable(event, definition));
    }

    private void useConsumable(PlayerInteractEvent event, BackroomsItemDefinition definition) {
        Player player = event.getPlayer();
        long remainingMillis = plugin.items().remainingUseCooldown(player, definition);
        if (remainingMillis > 0L) {
            event.setCancelled(true);
            plugin.messages().send(player, "item-cooldown",
                    plugin.messages().text("seconds", String.valueOf(Math.max(1L, (remainingMillis + 999L) / 1000L)))
            );
            return;
        }

        event.setCancelled(true);
        if (definition.sanity().hasEffect()) {
            plugin.sanity().restore(player, definition.sanity().restore());
            plugin.sanity().stabilize(player, definition.sanity().stabilizeSeconds());
        }

        if (definition.hasConsumeMessage()) {
            plugin.messages().send(player, definition.consumeMessage(),
                    plugin.messages().text("item", definition.id()),
                    plugin.messages().text("sanity", plugin.sanity().percentText(player)),
                    plugin.messages().text("max", String.valueOf(Math.round(plugin.sanity().maxSanity()))),
                    plugin.messages().text("stable", String.valueOf(Math.round(definition.sanity().stabilizeSeconds())))
            );
        } else {
            plugin.messages().send(player, "item-used",
                    plugin.messages().text("item", definition.id()),
                    plugin.messages().text("sanity", plugin.sanity().percentText(player)),
                    plugin.messages().text("max", String.valueOf(Math.round(plugin.sanity().maxSanity())))
            );
        }

        plugin.items().startUseCooldown(player, definition);
        if (player.getGameMode() != GameMode.CREATIVE) {
            consumeMainHandItem(player, definition.consumeReplacement());
        }
    }

    private void consumeMainHandItem(Player player, Material replacement) {
        PlayerInventory inventory = player.getInventory();
        ItemStack stack = inventory.getItemInMainHand();
        if (stack.getType().isAir()) {
            return;
        }

        if (stack.getAmount() > 1) {
            stack.setAmount(stack.getAmount() - 1);
            inventory.setItemInMainHand(stack);
            giveReplacement(player, replacement);
            return;
        }

        if (replacement == null || replacement.isAir()) {
            inventory.setItemInMainHand(null);
            return;
        }
        inventory.setItemInMainHand(new ItemStack(replacement, 1));
    }

    private void giveReplacement(Player player, Material replacement) {
        if (replacement == null || replacement.isAir()) {
            return;
        }
        ItemStack item = new ItemStack(replacement, 1);
        player.getInventory().addItem(item).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }
}
