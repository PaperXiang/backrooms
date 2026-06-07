package org.monday.backrooms.base;

import java.util.Locale;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.message.MessageService;

public final class BaseListener implements Listener {

    private static final String BASE_CLAIM_PERMISSION = "backrooms.command.base.claim";

    private final Backrooms plugin;

    public BaseListener(Backrooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        plugin.bases().getByTerminal(event.getClickedBlock()).ifPresent(base -> {
            event.setCancelled(true);
            MessageService messages = plugin.messages();
            if (!event.getPlayer().hasPermission(BASE_CLAIM_PERMISSION)) {
                messages.send(event.getPlayer(), "no-permission");
                return;
            }

            BaseClaimResult result = plugin.bases().claim(event.getPlayer(), base.id());
            if (result.status() == BaseClaimStatus.SUCCESS) {
                messages.send(event.getPlayer(), "base-claim-success",
                        messages.text("id", result.definition().id()),
                        messages.mini("display", result.definition().displayName())
                );
                return;
            }

            switch (result.status()) {
                case NOT_FOUND -> messages.send(event.getPlayer(), "base-not-found", messages.text("id", base.id()));
                case DISABLED -> messages.send(event.getPlayer(), "base-disabled", messages.text("id", result.definition().id()));
                case WRONG_LEVEL -> messages.send(event.getPlayer(), "base-claim-wrong-location",
                        messages.text("id", result.definition().id()),
                        messages.text("world", result.definition().world()),
                        messages.text("region", result.definition().regionDescription())
                );
                case ALREADY_CLAIMED -> messages.send(event.getPlayer(), "base-already-claimed",
                        messages.text("id", result.definition().id()),
                        messages.text("owner", result.claim().ownerName())
                );
                case CLAIM_LIMIT_REACHED -> messages.send(event.getPlayer(), "base-claim-limit");
                case SAVE_FAILED -> messages.send(event.getPlayer(), "base-claim-save-failed", messages.text("id", result.definition().id()));
                default -> plugin.getLogger().warning("Unhandled base terminal claim status: " + result.status().name().toLowerCase(Locale.ROOT));
            }
        });
    }
}
