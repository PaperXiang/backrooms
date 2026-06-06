package org.monday.backrooms.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.level.BackroomsLevel;
import org.monday.backrooms.message.MessageService;
import org.monday.backrooms.player.PlayerLevelState;
import org.monday.backrooms.room.RoomDefinition;
import org.monday.backrooms.room.RoomGenerationResult;
import org.monday.backrooms.transition.TransitionDefinition;

public final class BrCommand implements TabExecutor {

    private static final String BASE_PERMISSION = "backrooms.command.br";
    private static final String RELOAD_PERMISSION = "backrooms.command.reload";
    private static final String LEVEL_TP_PERMISSION = "backrooms.command.level.tp";
    private static final String DEBUG_CURRENT_PERMISSION = "backrooms.command.debug.current";
    private static final String TRANSITIONS_PERMISSION = "backrooms.command.transitions";
    private static final String TRANSITION_INFO_PERMISSION = "backrooms.command.transition.info";
    private static final String TRANSITION_TRIGGER_PERMISSION = "backrooms.command.transition.trigger";
    private static final String TRANSITION_GUIDE_PERMISSION = "backrooms.command.transition.guide";
    private static final String ROOM_LIST_PERMISSION = "backrooms.command.room.list";
    private static final String ROOM_INFO_PERMISSION = "backrooms.command.room.info";
    private static final String ROOM_GENERATE_PERMISSION = "backrooms.command.room.generate";

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

        if (is(args[0], "transitions")) {
            sendTransitions(sender);
            return true;
        }

        if (is(args[0], "rooms")) {
            sendRooms(sender);
            return true;
        }

        if (is(args[0], "room")) {
            if (args.length < 2) {
                plugin.messages().send(sender, "unknown-command");
                return true;
            }

            if (is(args[1], "list")) {
                sendRooms(sender);
                return true;
            }

            if (is(args[1], "info")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "room-info-usage");
                    return true;
                }
                sendRoomInfo(sender, args[2]);
                return true;
            }

            if (is(args[1], "generate")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "room-generate-usage");
                    return true;
                }
                generateRoom(sender, args[2], args.length >= 4 ? args[3] : null);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
            return true;
        }

        if (is(args[0], "transition")) {
            if (args.length < 2) {
                plugin.messages().send(sender, "unknown-command");
                return true;
            }

            if (is(args[1], "info")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "unknown-command");
                    return true;
                }
                sendTransitionInfo(sender, args[2]);
                return true;
            }

            if (is(args[1], "trigger")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "transition-trigger-usage");
                    return true;
                }
                triggerTransition(sender, args[2], args.length >= 4 ? args[3] : null);
                return true;
            }

            if (is(args[1], "guide")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "transition-guide-usage");
                    return true;
                }
                showTransitionGuide(sender, args[2]);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
            return true;
        }

        if (is(args[0], "level")) {
            if (args.length < 2) {
                plugin.messages().send(sender, "unknown-command");
                return true;
            }

            if (is(args[1], "tp")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "level-tp-usage");
                    return true;
                }
                teleportToLevel(sender, args[2]);
                return true;
            }

            if (is(args[1], "info")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "unknown-command");
                    return true;
                }
                sendLevelInfo(sender, args[2]);
                return true;
            }

            sendLevelInfo(sender, args[1]);
            return true;
        }

        if (is(args[0], "debug")) {
            if (args.length >= 2 && is(args[1], "current")) {
                sendDebugCurrent(sender);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
            return true;
        }

        if (is(args[0], "reload")) {
            if (!sender.hasPermission(RELOAD_PERMISSION)) {
                plugin.messages().send(sender, "no-permission");
                return true;
            }

            plugin.getLogger().info("Reload requested by " + sender.getName() + ".");
            plugin.reloadRuntimeConfig();
            plugin.messages().send(sender, "reload",
                    plugin.messages().text("count", String.valueOf(plugin.levels().size())),
                    plugin.messages().text("transition_count", String.valueOf(plugin.transitions().definitionCount())),
                    plugin.messages().text("room_count", String.valueOf(plugin.rooms().definitionCount()))
            );
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
            if (sender.hasPermission(DEBUG_CURRENT_PERMISSION)) {
                options.add("debug");
            }
            if (sender.hasPermission(TRANSITIONS_PERMISSION)) {
                options.add("transitions");
            }
            if (sender.hasPermission(ROOM_LIST_PERMISSION)) {
                options.add("rooms");
            }
            if (sender.hasPermission(ROOM_LIST_PERMISSION)
                    || sender.hasPermission(ROOM_INFO_PERMISSION)
                    || sender.hasPermission(ROOM_GENERATE_PERMISSION)) {
                options.add("room");
            }
            if (sender.hasPermission(TRANSITION_INFO_PERMISSION)
                    || sender.hasPermission(TRANSITION_TRIGGER_PERMISSION)
                    || sender.hasPermission(TRANSITION_GUIDE_PERMISSION)) {
                options.add("transition");
            }
            return filter(options, args[0]);
        }

        if (args.length == 2 && is(args[0], "transition")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(TRANSITION_INFO_PERMISSION)) {
                options.add("info");
            }
            if (sender.hasPermission(TRANSITION_TRIGGER_PERMISSION)) {
                options.add("trigger");
            }
            if (sender.hasPermission(TRANSITION_GUIDE_PERMISSION)) {
                options.add("guide");
            }
            return filter(options, args[1]);
        }

        if (args.length == 3 && is(args[0], "transition") && canCompleteTransitionIds(sender, args[1])) {
            return filter(plugin.transitions().all().stream().map(TransitionDefinition::id).toList(), args[2]);
        }

        if (args.length == 4 && is(args[0], "transition") && is(args[1], "trigger") && sender.hasPermission(TRANSITION_TRIGGER_PERMISSION)) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[3]);
        }

        if (args.length == 2 && is(args[0], "room")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(ROOM_LIST_PERMISSION)) {
                options.add("list");
            }
            if (sender.hasPermission(ROOM_INFO_PERMISSION)) {
                options.add("info");
            }
            if (sender.hasPermission(ROOM_GENERATE_PERMISSION)) {
                options.add("generate");
            }
            return filter(options, args[1]);
        }

        if (args.length == 3 && is(args[0], "room") && canCompleteRoomIds(sender, args[1])) {
            return filter(plugin.rooms().all().stream().map(RoomDefinition::id).toList(), args[2]);
        }

        if (args.length == 4 && is(args[0], "room") && is(args[1], "generate") && sender.hasPermission(ROOM_GENERATE_PERMISSION)) {
            return filter(plugin.levels().all().stream().map(BackroomsLevel::id).toList(), args[3]);
        }

        if (args.length == 2 && is(args[0], "debug") && sender.hasPermission(DEBUG_CURRENT_PERMISSION)) {
            return filter(List.of("current"), args[1]);
        }

        if (args.length == 2 && is(args[0], "level")) {
            List<String> options = new ArrayList<>(List.of("info"));
            if (sender.hasPermission(LEVEL_TP_PERMISSION)) {
                options.add("tp");
            }
            options.addAll(plugin.levels().all().stream().map(BackroomsLevel::id).toList());
            return filter(options, args[1]);
        }

        if (args.length == 3 && is(args[0], "level") && (is(args[1], "info") || is(args[1], "tp"))) {
            return filter(plugin.levels().all().stream().map(BackroomsLevel::id).toList(), args[2]);
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

    private void sendTransitions(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(TRANSITIONS_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.transitions().all().isEmpty()) {
            messages.send(sender, "transitions-empty");
            return;
        }

        messages.send(sender, "transitions-header");
        for (TransitionDefinition transition : plugin.transitions().all()) {
            messages.send(sender, "transition-line",
                    messages.text("id", transition.id()),
                    messages.mini("display", transition.displayName()),
                    messages.text("from", transition.fromLevel()),
                    messages.text("target", transition.target().describe()),
                    messages.bool("enabled", transition.enabled())
            );
        }
    }

    private void sendRooms(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(ROOM_LIST_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.rooms().all().isEmpty()) {
            messages.send(sender, "rooms-empty");
            return;
        }

        messages.send(sender, "rooms-header");
        for (RoomDefinition room : plugin.rooms().all()) {
            messages.send(sender, "room-line",
                    messages.text("id", room.id()),
                    messages.mini("display", room.displayName()),
                    messages.bool("enabled", room.enabled()),
                    messages.text("shape", room.shape().name().toLowerCase(Locale.ROOT)),
                    messages.text("size", room.sizeDescription()),
                    messages.text("levels", room.levels().isEmpty() ? "any" : String.join(",", room.levels()))
            );
        }
    }

    private void sendRoomInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(ROOM_INFO_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        plugin.rooms().get(id).ifPresentOrElse(room -> messages.send(sender, "room-info",
                messages.text("id", room.id()),
                messages.mini("display", room.displayName()),
                messages.bool("enabled", room.enabled()),
                messages.text("shape", room.shape().name().toLowerCase(Locale.ROOT)),
                messages.text("size", room.sizeDescription()),
                messages.text("levels", room.levels().isEmpty() ? "any" : String.join(",", room.levels())),
                messages.text("floor", room.floor().name()),
                messages.text("wall", room.wall().name()),
                messages.text("ceiling", room.ceiling().name()),
                messages.text("light", room.light().name()),
                messages.text("marker", room.marker().name())
        ), () -> messages.send(sender, "room-not-found", messages.text("id", id)));
    }

    private void generateRoom(CommandSender sender, String roomId, String levelId) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(ROOM_GENERATE_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }

        plugin.rooms().get(roomId).ifPresentOrElse(room -> {
            BackroomsLevel level = resolveRoomGenerationLevel(player, levelId);
            if (level == null) {
                messages.send(sender, "room-generate-level-required");
                return;
            }
            if (!level.enabled()) {
                messages.send(sender, "level-disabled", messages.text("id", level.id()));
                return;
            }
            if (!player.getWorld().getName().equals(level.world())) {
                messages.send(sender, "room-generate-wrong-world",
                        messages.text("level", level.id()),
                        messages.text("world", level.world())
                );
                return;
            }

            RoomGenerationResult result = plugin.rooms().generate(room, level, player.getLocation());
            sendRoomGenerationResult(sender, result);
        }, () -> messages.send(sender, "room-not-found", messages.text("id", roomId)));
    }

    private BackroomsLevel resolveRoomGenerationLevel(Player player, String levelId) {
        if (levelId != null && !levelId.isBlank()) {
            return plugin.levels().get(levelId).orElse(null);
        }

        return plugin.levels().getByWorld(player.getWorld().getName()).orElse(null);
    }

    private void sendRoomGenerationResult(CommandSender sender, RoomGenerationResult result) {
        MessageService messages = plugin.messages();
        messages.send(sender, result.messageKey(),
                messages.text("id", result.roomId()),
                messages.text("level", result.levelId()),
                messages.text("world", result.world()),
                messages.text("blocks", String.valueOf(result.blocksChanged())),
                messages.text("x", String.valueOf(result.originX())),
                messages.text("y", String.valueOf(result.originY())),
                messages.text("z", String.valueOf(result.originZ()))
        );
    }

    private void sendLevelInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        plugin.levels().get(id).ifPresentOrElse(level -> messages.send(sender, "level-info",
                messages.text("id", level.id()),
                messages.mini("display", level.displayName()),
                messages.text("world", level.world()),
                messages.bool("enabled", level.enabled()),
                messages.bool("pvp", level.pvp()),
                messages.text("spawn_points", String.valueOf(level.spawn() == null ? 0 : level.spawn().pointCount())),
                messages.mini("title", level.title()),
                messages.mini("subtitle", level.subtitle()),
                messages.text("description", level.description())
        ), () -> messages.send(sender, "level-not-found", messages.text("id", id)));
    }

    private void sendTransitionInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(TRANSITION_INFO_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        plugin.transitions().get(id).ifPresentOrElse(transition -> messages.send(sender, "transition-info",
                messages.text("id", transition.id()),
                messages.mini("display", transition.displayName()),
                messages.bool("enabled", transition.enabled()),
                messages.text("from", transition.fromLevel()),
                messages.text("target", transition.target().describe()),
                messages.text("trigger", transition.triggerDescription()),
                messages.text("cooldown", String.valueOf(transition.cooldownSeconds()))
        ), () -> messages.send(sender, "transition-not-found", messages.text("id", id)));
    }

    private void triggerTransition(CommandSender sender, String id, String targetName) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(TRANSITION_TRIGGER_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        Player target;
        if (targetName == null || targetName.isBlank()) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "transition-trigger-usage");
                return;
            }
            target = player;
        } else {
            target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                messages.send(sender, "player-not-found", messages.text("player", targetName));
                return;
            }
        }

        plugin.transitions().get(id).ifPresentOrElse(transition -> {
            boolean success = plugin.transitions().triggerByCommand(target, transition);
            if (success) {
                messages.send(sender, "transition-trigger-success",
                        messages.text("id", transition.id()),
                        messages.text("player", target.getName())
                );
            }
        }, () -> messages.send(sender, "transition-not-found", messages.text("id", id)));
    }

    private void showTransitionGuide(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(TRANSITION_GUIDE_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }

        plugin.transitions().get(id).ifPresentOrElse(
                transition -> plugin.transitions().showGuide(player, transition),
                () -> messages.send(sender, "transition-not-found", messages.text("id", id))
        );
    }

    private void teleportToLevel(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(LEVEL_TP_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }

        plugin.levels().get(id).ifPresentOrElse(level -> {
            if (!level.enabled()) {
                messages.send(sender, "level-disabled", messages.text("id", level.id()));
                return;
            }

            World world = Bukkit.getWorld(level.world());
            if (world == null) {
                messages.send(sender, "level-world-not-loaded",
                        messages.text("id", level.id()),
                        messages.text("world", level.world())
                );
                return;
            }

            Location location = level.spawn() == null ? world.getSpawnLocation() : level.spawn().toLocation(world);
            plugin.getLogger().info("Teleporting " + player.getName() + " to level '" + level.id() + "' at "
                    + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + ".");
            boolean teleported = player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            if (!teleported) {
                messages.send(sender, "level-teleport-failed", messages.text("id", level.id()));
                return;
            }

            plugin.playerLevels().enter(player, level, true, true);
            messages.send(sender, "level-teleport-success",
                    messages.text("id", level.id()),
                    messages.mini("display", level.displayName())
            );
        }, () -> messages.send(sender, "level-not-found", messages.text("id", id)));
    }

    private void sendDebugCurrent(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(DEBUG_CURRENT_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }

        var state = plugin.playerLevels().current(player);
        var worldLevel = plugin.levels().getByWorld(player.getWorld().getName());
        if (state.isEmpty()) {
            messages.send(sender, "debug-current-none",
                    messages.text("player", player.getName()),
                    messages.text("world", player.getWorld().getName()),
                    messages.text("world_level", worldLevel.map(BackroomsLevel::id).orElse("none"))
            );
            return;
        }

        PlayerLevelState currentState = state.get();
        plugin.levels().get(currentState.levelId()).ifPresentOrElse(level -> messages.send(sender, "debug-current",
                messages.text("player", player.getName()),
                messages.text("world", player.getWorld().getName()),
                messages.text("tracked_level", currentState.levelId()),
                messages.text("world_level", worldLevel.map(BackroomsLevel::id).orElse("none")),
                messages.mini("display", level.displayName()),
                messages.bool("pvp", level.pvp()),
                messages.bool("allow_break", level.rules().allowBlockBreak()),
                messages.bool("allow_place", level.rules().allowBlockPlace()),
                messages.bool("resource_interaction", level.rules().resourceInteraction()),
                messages.text("entered_at", currentState.enteredAt().toString())
        ), () -> messages.send(sender, "debug-current-stale",
                messages.text("player", player.getName()),
                messages.text("world", player.getWorld().getName()),
                messages.text("tracked_level", currentState.levelId()),
                messages.text("world_level", worldLevel.map(BackroomsLevel::id).orElse("none")),
                messages.text("entered_at", currentState.enteredAt().toString())
        ));
    }

    private boolean is(String input, String expected) {
        return input.equalsIgnoreCase(expected);
    }

    private boolean canCompleteTransitionIds(CommandSender sender, String subcommand) {
        return (is(subcommand, "info") && sender.hasPermission(TRANSITION_INFO_PERMISSION))
                || (is(subcommand, "trigger") && sender.hasPermission(TRANSITION_TRIGGER_PERMISSION))
                || (is(subcommand, "guide") && sender.hasPermission(TRANSITION_GUIDE_PERMISSION));
    }

    private boolean canCompleteRoomIds(CommandSender sender, String subcommand) {
        return (is(subcommand, "info") && sender.hasPermission(ROOM_INFO_PERMISSION))
                || (is(subcommand, "generate") && sender.hasPermission(ROOM_GENERATE_PERMISSION));
    }

    private List<String> filter(List<String> options, String prefix) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
                .toList();
    }
}
