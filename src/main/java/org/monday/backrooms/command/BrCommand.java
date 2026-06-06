package org.monday.backrooms.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;
import org.monday.backrooms.message.MessageService;

public final class BrCommand implements TabExecutor {

    private static final String BASE_PERMISSION = "backrooms.command.br";
    private static final String RELOAD_PERMISSION = "backrooms.command.reload";

    private final Backrooms plugin;

    public BrCommand(Backrooms plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(BASE_PERMISSION)) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0 || is(args[0], "help")) {
            plugin.messages().send(sender, "help");
            return true;
        }

        if (is(args[0], "levels")) {
            sendLevels(sender);
            return true;
        }

        if (is(args[0], "level")) {
            if (args.length < 2) {
                plugin.messages().send(sender, "unknown-command");
                return true;
            }
            sendLevelInfo(sender, args[1]);
            return true;
        }

        if (is(args[0], "reload")) {
            if (!sender.hasPermission(RELOAD_PERMISSION)) {
                plugin.messages().send(sender, "no-permission");
                return true;
            }

            plugin.reloadRuntimeConfig();
            plugin.messages().send(sender, "reload", plugin.messages().text("count", String.valueOf(plugin.levels().size())));
            return true;
        }

        plugin.messages().send(sender, "unknown-command");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(BASE_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("help", "levels", "level"));
            if (sender.hasPermission(RELOAD_PERMISSION)) {
                options.add("reload");
            }
            return filter(options, args[0]);
        }

        if (args.length == 2 && is(args[0], "level")) {
            return filter(plugin.levels().all().stream().map(BackroomsLevel::id).toList(), args[1]);
        }

        return List.of();
    }

    private void sendLevels(CommandSender sender) {
        MessageService messages = plugin.messages();

        if (plugin.levels().all().isEmpty()) {
            messages.send(sender, "levels-empty");
            return;
        }

        messages.send(sender, "levels-header");
        for (BackroomsLevel level : plugin.levels().all()) {
            messages.send(sender, "level-line",
                    messages.text("id", level.id()),
                    messages.mini("display", level.displayName()),
                    messages.text("world", level.world()),
                    messages.bool("enabled", level.enabled())
            );
        }
    }

    private void sendLevelInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        plugin.levels().get(id).ifPresentOrElse(level -> messages.send(sender, "level-info",
                messages.text("id", level.id()),
                messages.mini("display", level.displayName()),
                messages.text("world", level.world()),
                messages.bool("enabled", level.enabled()),
                messages.bool("pvp", level.pvp()),
                messages.mini("title", level.title()),
                messages.mini("subtitle", level.subtitle()),
                messages.text("description", level.description())
        ), () -> messages.send(sender, "level-not-found", messages.text("id", id)));
    }

    private boolean is(String input, String expected) {
        return input.equalsIgnoreCase(expected);
    }

    private List<String> filter(List<String> options, String prefix) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
                .toList();
    }
}
