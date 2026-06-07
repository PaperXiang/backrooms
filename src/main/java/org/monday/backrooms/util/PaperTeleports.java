package org.monday.backrooms.util;

import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperTeleports {

    private PaperTeleports() {
    }

    public static void teleportAsync(
            JavaPlugin plugin,
            Player player,
            Location location,
            PlayerTeleportEvent.TeleportCause cause,
            BiConsumer<Boolean, Throwable> callback
    ) {
        // Paper completes chunk preparation asynchronously; gameplay state is finalized on the server thread.
        player.teleportAsync(location, cause).whenComplete((success, throwable) -> {
            if (!plugin.isEnabled()) {
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(Boolean.TRUE.equals(success), unwrap(throwable)));
        });
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return throwable;
    }
}
