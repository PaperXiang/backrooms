package org.monday.backrooms.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.monday.backrooms.Backrooms;
import org.monday.backrooms.base.BaseClaimResult;
import org.monday.backrooms.base.BaseClaimStatus;
import org.monday.backrooms.base.BaseDefinition;
import org.monday.backrooms.items.BackroomsItemDefinition;
import org.monday.backrooms.level.BackroomsLevel;
import org.monday.backrooms.loot.LootSourceDefinition;
import org.monday.backrooms.loot.LootSourceRewardResult;
import org.monday.backrooms.loot.LootSourceRewardStatus;
import org.monday.backrooms.loot.LootTableDefinition;
import org.monday.backrooms.message.MessageService;
import org.monday.backrooms.player.PlayerLevelState;
import org.monday.backrooms.resource.ResourceBlockDefinition;
import org.monday.backrooms.room.RoomDefinition;
import org.monday.backrooms.room.RoomGenerationResult;
import org.monday.backrooms.transition.TransitionDefinition;
import org.monday.backrooms.util.PaperTeleports;
import org.monday.backrooms.worldgen.SchematicTemplateDefinition;
import org.monday.backrooms.worldgen.WorldGenerationResult;

public final class BrCommand implements TabExecutor {

    public static final String BASE_PERMISSION = "backrooms.command.br";
    private static final String RELOAD_PERMISSION = "backrooms.command.reload";
    private static final String LEVEL_TP_PERMISSION = "backrooms.command.level.tp";
    private static final String DEBUG_CURRENT_PERMISSION = "backrooms.command.debug.current";
    private static final String DEBUG_CONFIG_PERMISSION = "backrooms.command.debug.config";
    private static final String TRANSITIONS_PERMISSION = "backrooms.command.transitions";
    private static final String TRANSITION_INFO_PERMISSION = "backrooms.command.transition.info";
    private static final String TRANSITION_TRIGGER_PERMISSION = "backrooms.command.transition.trigger";
    private static final String TRANSITION_GUIDE_PERMISSION = "backrooms.command.transition.guide";
    private static final String ROOM_LIST_PERMISSION = "backrooms.command.room.list";
    private static final String ROOM_INFO_PERMISSION = "backrooms.command.room.info";
    private static final String ROOM_GENERATE_PERMISSION = "backrooms.command.room.generate";
    private static final String LOOT_LIST_PERMISSION = "backrooms.command.loot.list";
    private static final String LOOT_INFO_PERMISSION = "backrooms.command.loot.info";
    private static final String LOOT_ROLL_PERMISSION = "backrooms.command.loot.roll";
    private static final String LOOT_SOURCE_LIST_PERMISSION = "backrooms.command.loot.source.list";
    private static final String LOOT_SOURCE_INFO_PERMISSION = "backrooms.command.loot.source.info";
    private static final String LOOT_SOURCE_TRIGGER_PERMISSION = "backrooms.command.loot.source.trigger";
    private static final String ITEM_LIST_PERMISSION = "backrooms.command.item.list";
    private static final String ITEM_INFO_PERMISSION = "backrooms.command.item.info";
    private static final String ITEM_GIVE_PERMISSION = "backrooms.command.item.give";
    private static final String RESOURCE_LIST_PERMISSION = "backrooms.command.resource.list";
    private static final String RESOURCE_INFO_PERMISSION = "backrooms.command.resource.info";
    private static final String WORLDGEN_TEMPLATES_PERMISSION = "backrooms.command.worldgen.templates";
    private static final String WORLDGEN_GENERATE_PERMISSION = "backrooms.command.worldgen.generate";
    private static final String BASE_LIST_PERMISSION = "backrooms.command.base.list";
    private static final String BASE_INFO_PERMISSION = "backrooms.command.base.info";
    private static final String BASE_CLAIM_PERMISSION = "backrooms.command.base.claim";

    private final Backrooms plugin;

    public BrCommand(Backrooms plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, label, args);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
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

        if (is(args[0], "resources")) {
            sendResources(sender);
            return true;
        }

        if (is(args[0], "items")) {
            sendItems(sender);
            return true;
        }

        if (is(args[0], "worldgen")) {
            if (args.length < 2 || is(args[1], "templates")) {
                sendSchematicTemplates(sender);
                return true;
            }

            if (is(args[1], "generate")) {
                if (args.length < 4) {
                    plugin.messages().send(sender, "worldgen-generate-usage");
                    return true;
                }
                generateWorldRegion(sender, args[2], args[3], args.length >= 5 ? args[4] : null);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
            return true;
        }

        if (is(args[0], "bases")) {
            sendBases(sender);
            return true;
        }

        if (is(args[0], "base")) {
            if (args.length < 2 || is(args[1], "list")) {
                sendBases(sender);
                return true;
            }

            if (is(args[1], "info")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "base-info-usage");
                    return true;
                }
                sendBaseInfo(sender, args[2]);
                return true;
            }

            if (is(args[1], "claim")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "base-claim-usage");
                    return true;
                }
                claimBase(sender, args[2]);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
            return true;
        }

        if (is(args[0], "loot")) {
            if (args.length < 2 || is(args[1], "list")) {
                sendLootTables(sender);
                return true;
            }

            if (is(args[1], "sources")) {
                sendLootSources(sender);
                return true;
            }

            if (is(args[1], "source")) {
                if (args.length >= 4 && is(args[2], "info")) {
                    sendLootSourceInfo(sender, args[3]);
                    return true;
                }
                if (args.length >= 4 && is(args[2], "trigger")) {
                    triggerLootSource(sender, args[3], args.length >= 5 ? args[4] : null);
                    return true;
                }
                if (args.length >= 3 && is(args[2], "trigger")) {
                    plugin.messages().send(sender, "loot-source-trigger-usage");
                    return true;
                }
                if (args.length >= 3 && is(args[2], "info")) {
                    plugin.messages().send(sender, "loot-source-info-usage");
                    return true;
                }
                plugin.messages().send(sender, "loot-source-info-usage");
                return true;
            }

            if (is(args[1], "info")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "loot-info-usage");
                    return true;
                }
                sendLootInfo(sender, args[2]);
                return true;
            }

            if (is(args[1], "roll")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "loot-roll-usage");
                    return true;
                }
                rollLootTable(sender, args[2], args.length >= 4 ? args[3] : null);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
            return true;
        }

        if (is(args[0], "item")) {
            if (args.length < 2 || is(args[1], "list")) {
                sendItems(sender);
                return true;
            }

            if (is(args[1], "info")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "item-info-usage");
                    return true;
                }
                sendItemInfo(sender, args[2]);
                return true;
            }

            if (is(args[1], "give")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "item-give-usage");
                    return true;
                }
                giveItem(sender, args[2], args.length >= 4 ? args[3] : null, args.length >= 5 ? args[4] : null);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
            return true;
        }

        if (is(args[0], "resource")) {
            if (args.length < 2 || is(args[1], "list")) {
                sendResources(sender);
                return true;
            }

            if (is(args[1], "info")) {
                if (args.length < 3) {
                    plugin.messages().send(sender, "resource-info-usage");
                    return true;
                }
                sendResourceInfo(sender, args[2]);
                return true;
            }

            plugin.messages().send(sender, "unknown-command");
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

            if (args.length >= 2 && is(args[1], "config")) {
                sendDebugConfig(sender);
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
            boolean reloaded = plugin.reloadRuntimeConfig();
            plugin.messages().send(sender, reloaded ? "reload" : "reload-failed",
                    plugin.messages().text("count", String.valueOf(plugin.levels().size())),
                    plugin.messages().text("item_count", String.valueOf(plugin.items().definitionCount())),
                    plugin.messages().text("loot_count", String.valueOf(plugin.lootTables().definitionCount())),
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
        return complete(sender, label, args);
    }

    public List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(BASE_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("help", "levels", "level"));
            if (sender.hasPermission(RELOAD_PERMISSION)) {
                options.add("reload");
            }
            if (sender.hasPermission(DEBUG_CURRENT_PERMISSION) || sender.hasPermission(DEBUG_CONFIG_PERMISSION)) {
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
            if (sender.hasPermission(LOOT_LIST_PERMISSION)
                    || sender.hasPermission(LOOT_INFO_PERMISSION)
                    || sender.hasPermission(LOOT_ROLL_PERMISSION)
                    || sender.hasPermission(LOOT_SOURCE_LIST_PERMISSION)
                    || sender.hasPermission(LOOT_SOURCE_INFO_PERMISSION)
                    || sender.hasPermission(LOOT_SOURCE_TRIGGER_PERMISSION)) {
                options.add("loot");
            }
            if (sender.hasPermission(ITEM_LIST_PERMISSION)) {
                options.add("items");
            }
            if (sender.hasPermission(ITEM_LIST_PERMISSION)
                    || sender.hasPermission(ITEM_INFO_PERMISSION)
                    || sender.hasPermission(ITEM_GIVE_PERMISSION)) {
                options.add("item");
            }
            if (sender.hasPermission(RESOURCE_LIST_PERMISSION)) {
                options.add("resources");
            }
            if (sender.hasPermission(RESOURCE_LIST_PERMISSION)
                    || sender.hasPermission(RESOURCE_INFO_PERMISSION)) {
                options.add("resource");
            }
            if (sender.hasPermission(WORLDGEN_TEMPLATES_PERMISSION)
                    || sender.hasPermission(WORLDGEN_GENERATE_PERMISSION)) {
                options.add("worldgen");
            }
            if (sender.hasPermission(BASE_LIST_PERMISSION)) {
                options.add("bases");
            }
            if (sender.hasPermission(BASE_LIST_PERMISSION)
                    || sender.hasPermission(BASE_INFO_PERMISSION)
                    || sender.hasPermission(BASE_CLAIM_PERMISSION)) {
                options.add("base");
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

        if (args.length == 2 && is(args[0], "loot")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(LOOT_LIST_PERMISSION)) {
                options.add("list");
            }
            if (sender.hasPermission(LOOT_INFO_PERMISSION)) {
                options.add("info");
            }
            if (sender.hasPermission(LOOT_ROLL_PERMISSION)) {
                options.add("roll");
            }
            if (sender.hasPermission(LOOT_SOURCE_LIST_PERMISSION)) {
                options.add("sources");
            }
            if (sender.hasPermission(LOOT_SOURCE_INFO_PERMISSION) || sender.hasPermission(LOOT_SOURCE_TRIGGER_PERMISSION)) {
                options.add("source");
            }
            return filter(options, args[1]);
        }

        if (args.length == 2 && is(args[0], "base")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(BASE_LIST_PERMISSION)) {
                options.add("list");
            }
            if (sender.hasPermission(BASE_INFO_PERMISSION)) {
                options.add("info");
            }
            if (sender.hasPermission(BASE_CLAIM_PERMISSION)) {
                options.add("claim");
            }
            return filter(options, args[1]);
        }

        if (args.length == 3 && is(args[0], "base")
                && ((is(args[1], "info") && sender.hasPermission(BASE_INFO_PERMISSION))
                || (is(args[1], "claim") && sender.hasPermission(BASE_CLAIM_PERMISSION)))) {
            return filter(plugin.bases().all().stream().map(BaseDefinition::id).toList(), args[2]);
        }

        if (args.length == 3 && is(args[0], "loot") && canCompleteLootIds(sender, args[1])) {
            return filter(plugin.lootTables().all().stream().map(LootTableDefinition::id).toList(), args[2]);
        }

        if (args.length == 3 && is(args[0], "loot") && is(args[1], "source")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(LOOT_SOURCE_INFO_PERMISSION)) {
                options.add("info");
            }
            if (sender.hasPermission(LOOT_SOURCE_TRIGGER_PERMISSION)) {
                options.add("trigger");
            }
            return filter(options, args[2]);
        }

        if (args.length == 4 && is(args[0], "loot") && is(args[1], "source") && is(args[2], "info") && sender.hasPermission(LOOT_SOURCE_INFO_PERMISSION)) {
            return filter(plugin.lootSources().all().stream().map(LootSourceDefinition::id).toList(), args[3]);
        }

        if (args.length == 4 && is(args[0], "loot") && is(args[1], "source") && is(args[2], "trigger") && sender.hasPermission(LOOT_SOURCE_TRIGGER_PERMISSION)) {
            return filter(plugin.lootSources().all().stream()
                    .filter(source -> source.type().supportsDirectReward())
                    .map(LootSourceDefinition::id)
                    .toList(), args[3]);
        }

        if (args.length == 5 && is(args[0], "loot") && is(args[1], "source") && is(args[2], "trigger") && sender.hasPermission(LOOT_SOURCE_TRIGGER_PERMISSION)) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[4]);
        }

        if (args.length == 4 && is(args[0], "loot") && is(args[1], "roll") && sender.hasPermission(LOOT_ROLL_PERMISSION)) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[3]);
        }

        if (args.length == 2 && is(args[0], "item")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(ITEM_LIST_PERMISSION)) {
                options.add("list");
            }
            if (sender.hasPermission(ITEM_INFO_PERMISSION)) {
                options.add("info");
            }
            if (sender.hasPermission(ITEM_GIVE_PERMISSION)) {
                options.add("give");
            }
            return filter(options, args[1]);
        }

        if (args.length == 3 && is(args[0], "item") && canCompleteItemIds(sender, args[1])) {
            return filter(plugin.items().all().stream().map(BackroomsItemDefinition::id).toList(), args[2]);
        }

        if (args.length == 4 && is(args[0], "item") && is(args[1], "give") && sender.hasPermission(ITEM_GIVE_PERMISSION)) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[3]);
        }

        if (args.length == 5 && is(args[0], "item") && is(args[1], "give") && sender.hasPermission(ITEM_GIVE_PERMISSION)) {
            return filter(List.of("1", "2", "4", "8", "16", "32", "64"), args[4]);
        }

        if (args.length == 2 && is(args[0], "resource")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(RESOURCE_LIST_PERMISSION)) {
                options.add("list");
            }
            if (sender.hasPermission(RESOURCE_INFO_PERMISSION)) {
                options.add("info");
            }
            return filter(options, args[1]);
        }

        if (args.length == 3 && is(args[0], "resource") && is(args[1], "info") && sender.hasPermission(RESOURCE_INFO_PERMISSION)) {
            return filter(plugin.resources().all().stream().map(ResourceBlockDefinition::id).toList(), args[2]);
        }

        if (args.length == 2 && is(args[0], "worldgen")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(WORLDGEN_TEMPLATES_PERMISSION)) {
                options.add("templates");
            }
            if (sender.hasPermission(WORLDGEN_GENERATE_PERMISSION)) {
                options.add("generate");
            }
            return filter(options, args[1]);
        }

        if (args.length == 3 && is(args[0], "worldgen") && is(args[1], "generate") && sender.hasPermission(WORLDGEN_GENERATE_PERMISSION)) {
            return filter(plugin.levels().all().stream().map(BackroomsLevel::id).toList(), args[2]);
        }

        if (args.length == 4 && is(args[0], "worldgen") && is(args[1], "generate") && sender.hasPermission(WORLDGEN_GENERATE_PERMISSION)) {
            return filter(List.of("9", "11", "15", "21"), args[3]);
        }

        if (args.length == 2 && is(args[0], "debug")) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission(DEBUG_CURRENT_PERMISSION)) {
                options.add("current");
            }
            if (sender.hasPermission(DEBUG_CONFIG_PERMISSION)) {
                options.add("config");
            }
            return filter(options, args[1]);
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

    private void sendResources(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(RESOURCE_LIST_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.resources().all().isEmpty()) {
            messages.send(sender, "resources-empty");
            return;
        }

        messages.send(sender, "resources-header");
        for (ResourceBlockDefinition resource : plugin.resources().all()) {
            messages.send(sender, "resource-line",
                    messages.text("id", resource.id()),
                    messages.text("levels", resource.levels().isEmpty() ? "any" : String.join(",", resource.levels())),
                    messages.text("materials", resource.materials().stream().map(Enum::name).toList().toString()),
                    messages.text("triggers", resource.triggers().stream().map(trigger -> trigger.name().toLowerCase(Locale.ROOT)).toList().toString()),
                    messages.text("loot_tables", resource.lootTables().isEmpty() ? "none" : String.join(",", resource.lootTables())),
                    messages.text("drops", String.valueOf(resource.drops().size())),
                    messages.text("locations", String.valueOf(resource.positions().size()))
            );
        }
    }

    private void sendBases(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(BASE_LIST_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.bases().all().isEmpty()) {
            messages.send(sender, "bases-empty");
            return;
        }

        messages.send(sender, "bases-header");
        for (BaseDefinition base : plugin.bases().all()) {
            messages.send(sender, "base-line",
                    messages.text("id", base.id()),
                    messages.mini("display", base.displayName()),
                    messages.text("level", base.level()),
                    messages.text("world", base.world()),
                    messages.bool("enabled", base.enabled()),
                    messages.text("claimed", plugin.bases().claim(base.id()).map(claim -> claim.ownerName()).orElse("none"))
            );
        }
    }

    private void sendBaseInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(BASE_INFO_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        plugin.bases().get(id).ifPresentOrElse(base -> messages.send(sender, "base-info",
                messages.text("id", base.id()),
                messages.mini("display", base.displayName()),
                messages.bool("enabled", base.enabled()),
                messages.text("level", base.level()),
                messages.text("world", base.world()),
                messages.text("region", base.regionDescription()),
                messages.text("terminal", base.terminalDescription()),
                messages.text("claimed", plugin.bases().claim(base.id()).map(claim -> claim.ownerName()).orElse("none"))
        ), () -> messages.send(sender, "base-not-found", messages.text("id", id)));
    }

    private void claimBase(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(BASE_CLAIM_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }

        BaseClaimResult result = plugin.bases().claim(player, id);
        if (result.status() == BaseClaimStatus.SUCCESS) {
            messages.send(sender, "base-claim-success",
                    messages.text("id", result.definition().id()),
                    messages.mini("display", result.definition().displayName())
            );
            return;
        }

        switch (result.status()) {
            case NOT_FOUND -> messages.send(sender, "base-not-found", messages.text("id", id));
            case DISABLED -> messages.send(sender, "base-disabled", messages.text("id", result.definition().id()));
            case WRONG_LEVEL -> messages.send(sender, "base-claim-wrong-location",
                    messages.text("id", result.definition().id()),
                    messages.text("world", result.definition().world()),
                    messages.text("region", result.definition().regionDescription())
            );
            case ALREADY_CLAIMED -> messages.send(sender, "base-already-claimed",
                    messages.text("id", result.definition().id()),
                    messages.text("owner", result.claim().ownerName())
            );
            case CLAIM_LIMIT_REACHED -> messages.send(sender, "base-claim-limit");
            case SAVE_FAILED -> messages.send(sender, "base-claim-save-failed", messages.text("id", result.definition().id()));
            default -> messages.send(sender, "unknown-command");
        }
    }

    private void sendResourceInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(RESOURCE_INFO_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        plugin.resources().all().stream()
                .filter(resource -> resource.id().equalsIgnoreCase(id))
                .findFirst()
                .ifPresentOrElse(resource -> messages.send(sender, "resource-info",
                        messages.text("id", resource.id()),
                        messages.text("levels", resource.levels().isEmpty() ? "any" : String.join(",", resource.levels())),
                        messages.text("materials", resource.materials().stream().map(Enum::name).toList().toString()),
                        messages.text("locations", String.valueOf(resource.positions().size())),
                        messages.text("triggers", resource.triggers().stream().map(trigger -> trigger.name().toLowerCase(Locale.ROOT)).toList().toString()),
                        messages.text("loot_tables", resource.lootTables().isEmpty() ? "none" : String.join(",", resource.lootTables())),
                        messages.text("drops", String.valueOf(resource.drops().size())),
                        messages.bool("remove_block", resource.removeBlock()),
                        messages.text("replacement", resource.replacement().name()),
                        messages.text("cooldown", String.valueOf(resource.cooldownSeconds()))
                ), () -> messages.send(sender, "resource-not-found", messages.text("id", id)));
    }

    private void sendItems(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(ITEM_LIST_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.items().all().isEmpty()) {
            messages.send(sender, "items-empty");
            return;
        }

        messages.send(sender, "items-header");
        for (BackroomsItemDefinition item : plugin.items().all()) {
            messages.send(sender, "item-line",
                    messages.text("id", item.id()),
                    messages.mini("display", item.displayName()),
                    messages.text("material", item.material().name()),
                    messages.text("sanity", item.sanity().hasEffect()
                            ? "+" + item.sanity().restore() + ", stable " + item.sanity().stabilizeSeconds() + "s"
                            : "none"),
                    messages.bool("enabled", item.enabled())
            );
        }
    }

    private void sendItemInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(ITEM_INFO_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        plugin.items().get(id).ifPresentOrElse(item -> messages.send(sender, "item-info",
                messages.text("id", item.id()),
                messages.mini("display", item.displayName()),
                messages.bool("enabled", item.enabled()),
                messages.text("material", item.material().name()),
                messages.text("custom_model_data", item.hasCustomModelData() ? String.valueOf(item.customModelData()) : "none"),
                messages.bool("consume", item.consumeOnRightClick()),
                messages.text("replacement", item.consumeReplacement().name()),
                messages.text("cooldown", String.valueOf(item.useCooldownSeconds())),
                messages.text("sanity_restore", String.valueOf(item.sanity().restore())),
                messages.text("sanity_stabilize", String.valueOf(item.sanity().stabilizeSeconds())),
                messages.text("message", item.hasConsumeMessage() ? item.consumeMessage() : "item-used")
        ), () -> messages.send(sender, "item-not-found", messages.text("id", id)));
    }

    private void giveItem(CommandSender sender, String id, String targetName, String amountInput) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(ITEM_GIVE_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        int amount = parsePositiveInt(amountInput, 1);
        Player target;
        if (targetName == null || targetName.isBlank()) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "item-give-usage");
                return;
            }
            target = player;
        } else if (isPositiveInt(targetName) && sender instanceof Player player) {
            target = player;
            amount = parsePositiveInt(targetName, 1);
        } else {
            target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                messages.send(sender, "player-not-found", messages.text("player", targetName));
                return;
            }
        }

        var definition = plugin.items().get(id);
        if (definition.isEmpty()) {
            messages.send(sender, "item-not-found", messages.text("id", id));
            return;
        }
        if (!definition.get().enabled()) {
            messages.send(sender, "item-disabled", messages.text("id", definition.get().id()));
            return;
        }

        ItemStack item = plugin.items().create(definition.get(), amount);
        boolean droppedLeftovers = giveRolledItems(target, List.of(item));
        messages.send(sender, "item-give-success",
                messages.text("id", definition.get().id()),
                messages.text("player", target.getName()),
                messages.text("amount", String.valueOf(amount))
        );
        if (droppedLeftovers) {
            messages.send(sender, "item-give-leftovers-dropped");
        }
    }

    private void sendSchematicTemplates(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(WORLDGEN_TEMPLATES_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.worldgen().allTemplates().isEmpty()) {
            messages.send(sender, "worldgen-templates-empty");
            return;
        }

        messages.send(sender, "worldgen-templates-header");
        for (SchematicTemplateDefinition template : plugin.worldgen().allTemplates()) {
            messages.send(sender, "worldgen-template-line",
                    messages.text("id", template.id()),
                    messages.mini("display", template.displayName()),
                    messages.bool("enabled", template.enabled()),
                    messages.text("level", template.level()),
                    messages.text("file", template.file().getPath()),
                    messages.text("footprint", template.footprintDescription()),
                    messages.text("connectors", template.connectors().stream().map(connector -> connector.configName()).toList().toString()),
                    messages.text("tags", template.tags().isEmpty() ? "none" : String.join(",", template.tags())),
                    messages.text("weight", String.valueOf(template.weight()))
            );
        }
    }

    private void generateWorldRegion(CommandSender sender, String levelId, String sizeInput, String seed) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(WORLDGEN_GENERATE_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        int size;
        try {
            size = Integer.parseInt(sizeInput);
        } catch (NumberFormatException exception) {
            messages.send(sender, "worldgen-generate-usage");
            return;
        }

        plugin.levels().get(levelId).ifPresentOrElse(level -> {
            if (!level.enabled()) {
                messages.send(sender, "level-disabled", messages.text("id", level.id()));
                return;
            }
            WorldGenerationResult result = plugin.worldgen().generate(level, size, seed);
            sendWorldGenerationResult(sender, result);
        }, () -> messages.send(sender, "level-not-found", messages.text("id", levelId)));
    }

    private void sendWorldGenerationResult(CommandSender sender, WorldGenerationResult result) {
        MessageService messages = plugin.messages();
        messages.send(sender, result.messageKey(),
                messages.text("level", result.levelId()),
                messages.text("world", result.world()),
                messages.text("region", result.regionId()),
                messages.text("cells", String.valueOf(result.cells())),
                messages.text("templates", String.valueOf(result.templates())),
                messages.text("blocks", String.valueOf(result.blocksChanged())),
                messages.text("markers", result.markers()),
                messages.text("reason", result.reason())
        );
    }

    private void sendLootTables(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(LOOT_LIST_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.lootTables().all().isEmpty()) {
            messages.send(sender, "loot-tables-empty");
            return;
        }

        messages.send(sender, "loot-tables-header");
        for (LootTableDefinition table : plugin.lootTables().all()) {
            messages.send(sender, "loot-table-line",
                    messages.text("id", table.id()),
                    messages.mini("display", table.displayName()),
                    messages.bool("enabled", table.enabled()),
                    messages.text("rolls", table.rollsDescription()),
                    messages.text("entries", String.valueOf(table.entries().size()))
            );
        }
    }

    private void sendLootInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(LOOT_INFO_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        plugin.lootTables().get(id).ifPresentOrElse(table -> messages.send(sender, "loot-info",
                messages.text("id", table.id()),
                messages.mini("display", table.displayName()),
                messages.bool("enabled", table.enabled()),
                messages.text("rolls", table.rollsDescription()),
                messages.text("entries", String.valueOf(table.entries().size()))
        ), () -> messages.send(sender, "loot-table-not-found", messages.text("id", id)));
    }

    private void sendLootSources(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(LOOT_SOURCE_LIST_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        if (plugin.lootSources().all().isEmpty()) {
            messages.send(sender, "loot-sources-empty");
            return;
        }

        messages.send(sender, "loot-sources-header");
        for (LootSourceDefinition source : plugin.lootSources().all()) {
            messages.send(sender, "loot-source-line",
                    messages.text("id", source.id()),
                    messages.bool("enabled", source.enabled()),
                    messages.text("type", source.type().name().toLowerCase(Locale.ROOT)),
                    messages.text("levels", source.levels().isEmpty() ? "any" : String.join(",", source.levels())),
                    messages.text("materials", source.materials().isEmpty() ? "none" : source.materials().stream().map(Enum::name).sorted().toList().toString()),
                    messages.text("tables", String.join(",", source.lootTables()))
            );
        }
    }

    private void sendLootSourceInfo(CommandSender sender, String id) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(LOOT_SOURCE_INFO_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        plugin.lootSources().get(id).ifPresentOrElse(source -> messages.send(sender, "loot-source-info",
                messages.text("id", source.id()),
                messages.bool("enabled", source.enabled()),
                messages.text("type", source.type().name().toLowerCase(Locale.ROOT)),
                messages.text("levels", source.levels().isEmpty() ? "any" : String.join(",", source.levels())),
                messages.text("materials", source.materials().isEmpty() ? "none" : source.materials().stream().map(Enum::name).sorted().toList().toString()),
                messages.text("locations", source.locations().isEmpty() ? "any" : source.locations().stream()
                        .map(position -> position.x() + "," + position.y() + "," + position.z())
                        .sorted()
                        .toList()
                        .toString()),
                messages.text("tables", String.join(",", source.lootTables())),
                messages.bool("one_time", source.oneTime()),
                messages.bool("fill_empty_only", source.fillEmptyOnly())
        ), () -> messages.send(sender, "loot-source-not-found", messages.text("id", id)));
    }

    private void triggerLootSource(CommandSender sender, String id, String targetName) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(LOOT_SOURCE_TRIGGER_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        Player target;
        if (targetName == null || targetName.isBlank()) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "loot-source-trigger-usage");
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

        LootSourceRewardResult result = plugin.lootSources().triggerReward(id, target);
        if (result.status() == LootSourceRewardStatus.SUCCESS) {
            messages.send(sender, "loot-source-trigger-success",
                    messages.text("id", result.source().id()),
                    messages.text("player", target.getName()),
                    messages.text("items", String.valueOf(result.items()))
            );
            if (result.droppedLeftovers()) {
                messages.send(sender, "loot-roll-leftovers-dropped");
            }
            return;
        }

        switch (result.status()) {
            case NOT_FOUND -> messages.send(sender, "loot-source-not-found", messages.text("id", id));
            case DISABLED -> messages.send(sender, "loot-source-disabled", messages.text("id", result.source().id()));
            case UNSUPPORTED_TYPE -> messages.send(sender, "loot-source-trigger-unsupported",
                    messages.text("id", result.source().id()),
                    messages.text("type", result.source().type().name().toLowerCase(Locale.ROOT))
            );
            case LEVEL_MISMATCH -> messages.send(sender, "loot-source-trigger-level-mismatch",
                    messages.text("id", result.source().id()),
                    messages.text("player", target.getName())
            );
            case ALREADY_GENERATED -> messages.send(sender, "loot-source-trigger-already-generated",
                    messages.text("id", result.source().id()),
                    messages.text("player", target.getName())
            );
            case EMPTY -> messages.send(sender, "loot-source-trigger-empty", messages.text("id", result.source().id()));
            default -> messages.send(sender, "unknown-command");
        }
    }

    private void rollLootTable(CommandSender sender, String id, String targetName) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(LOOT_ROLL_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        Player target;
        if (targetName == null || targetName.isBlank()) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "loot-roll-usage");
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

        plugin.lootTables().get(id).ifPresentOrElse(table -> {
            if (!table.enabled()) {
                messages.send(sender, "loot-table-disabled", messages.text("id", table.id()));
                return;
            }

            List<ItemStack> items = plugin.lootTables().roll(table);
            if (items.isEmpty()) {
                messages.send(sender, "loot-roll-empty", messages.text("id", table.id()));
                return;
            }

            boolean droppedLeftovers = giveRolledItems(target, items);
            messages.send(sender, "loot-roll-success",
                    messages.text("id", table.id()),
                    messages.text("player", target.getName()),
                    messages.text("items", String.valueOf(items.size()))
            );
            if (droppedLeftovers) {
                messages.send(sender, "loot-roll-leftovers-dropped");
            }
        }, () -> messages.send(sender, "loot-table-not-found", messages.text("id", id)));
    }

    private boolean giveRolledItems(Player target, List<ItemStack> items) {
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(items.toArray(ItemStack[]::new));
        for (ItemStack item : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
        }
        return !leftovers.isEmpty();
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
            PaperTeleports.teleportAsync(plugin, player, location, PlayerTeleportEvent.TeleportCause.COMMAND, (teleported, throwable) -> {
                if (!player.isOnline()) {
                    return;
                }
                if (throwable != null) {
                    plugin.getLogger().severe("Level teleport to '" + level.id() + "' failed for " + player.getName() + ": " + throwable.getMessage());
                    throwable.printStackTrace();
                }
                if (!teleported || throwable != null) {
                    messages.send(sender, "level-teleport-failed", messages.text("id", level.id()));
                    return;
                }

                plugin.playerLevels().enter(player, level, true, true);
                messages.send(sender, "level-teleport-success",
                        messages.text("id", level.id()),
                        messages.mini("display", level.displayName())
                );
            });
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

    private void sendDebugConfig(CommandSender sender) {
        MessageService messages = plugin.messages();
        if (!sender.hasPermission(DEBUG_CONFIG_PERMISSION)) {
            messages.send(sender, "no-permission");
            return;
        }

        List<String> missingLevelWorlds = plugin.levels().all().stream()
                .filter(level -> Bukkit.getWorld(level.world()) == null)
                .map(level -> level.id() + "->" + level.world())
                .toList();

        int disabledTransitions = 0;
        List<String> transitionIssues = new ArrayList<>();
        for (TransitionDefinition transition : plugin.transitions().all()) {
            if (!transition.enabled()) {
                disabledTransitions++;
            }
            if (plugin.levels().get(transition.fromLevel()).isEmpty()) {
                transitionIssues.add(transition.id() + ":from=" + transition.fromLevel());
            }
            if (Bukkit.getWorld(transition.triggerWorld()) == null) {
                transitionIssues.add(transition.id() + ":triggerWorld=" + transition.triggerWorld());
            }
            if (!messages.has(transition.messageKey())) {
                transitionIssues.add(transition.id() + ":messageKey=" + transition.messageKey());
            }
            if (transition.target().type().name().equals("LEVEL")) {
                plugin.levels().get(transition.target().level()).ifPresentOrElse(targetLevel -> {
                    if (!targetLevel.enabled()) {
                        transitionIssues.add(transition.id() + ":targetDisabled=" + targetLevel.id());
                    }
                }, () -> transitionIssues.add(transition.id() + ":targetLevel=" + transition.target().level()));
            } else if (Bukkit.getWorld(transition.target().world()) == null) {
                transitionIssues.add(transition.id() + ":targetWorld=" + transition.target().world());
            }
        }

        int disabledRooms = 0;
        List<String> roomIssues = new ArrayList<>();
        for (RoomDefinition room : plugin.rooms().all()) {
            if (!room.enabled()) {
                disabledRooms++;
            }
            for (String levelId : room.levels()) {
                if (plugin.levels().get(levelId).isEmpty()) {
                    roomIssues.add(room.id() + ":level=" + levelId);
                }
            }
        }

        messages.send(sender, "debug-config",
                messages.text("levels", String.valueOf(plugin.levels().size())),
                messages.text("levels_enabled", String.valueOf(plugin.levels().enabledCount())),
                messages.text("levels_disabled", String.valueOf(plugin.levels().disabledCount())),
                messages.text("missing_worlds", describeList(missingLevelWorlds)),
                messages.text("items", String.valueOf(plugin.items().definitionCount())),
                messages.text("loot_tables", String.valueOf(plugin.lootTables().definitionCount())),
                messages.text("loot_sources", String.valueOf(plugin.lootSources().definitionCount())),
                messages.text("pending_insurance", String.valueOf(plugin.corpses().pendingInsuranceCount())),
                messages.text("bases", String.valueOf(plugin.bases().definitionCount())),
                messages.text("base_claims", String.valueOf(plugin.bases().claimCount())),
                messages.text("resource_blocks", String.valueOf(plugin.resources().definitionCount())),
                messages.text("transitions", String.valueOf(plugin.transitions().definitionCount())),
                messages.text("transitions_disabled", String.valueOf(disabledTransitions)),
                messages.text("transition_issues", describeList(transitionIssues)),
                messages.text("rooms", String.valueOf(plugin.rooms().definitionCount())),
                messages.text("rooms_disabled", String.valueOf(disabledRooms)),
                messages.text("room_issues", describeList(roomIssues)),
                messages.text("schematic_templates", String.valueOf(plugin.worldgen().templateCount()))
        );
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

    private boolean canCompleteLootIds(CommandSender sender, String subcommand) {
        return (is(subcommand, "info") && sender.hasPermission(LOOT_INFO_PERMISSION))
                || (is(subcommand, "roll") && sender.hasPermission(LOOT_ROLL_PERMISSION));
    }

    private boolean canCompleteItemIds(CommandSender sender, String subcommand) {
        return (is(subcommand, "info") && sender.hasPermission(ITEM_INFO_PERMISSION))
                || (is(subcommand, "give") && sender.hasPermission(ITEM_GIVE_PERMISSION));
    }

    private int parsePositiveInt(String input, int fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(1, Integer.parseInt(input));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private boolean isPositiveInt(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(input) > 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String describeList(List<String> values) {
        return values.isEmpty() ? "none" : String.join(", ", values);
    }

    private List<String> filter(List<String> options, String prefix) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
                .toList();
    }
}
