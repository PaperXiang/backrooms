package org.monday.backrooms;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.monday.backrooms.command.BrCommand;
import org.monday.backrooms.config.ConfigFileService;
import org.monday.backrooms.hud.NoopSanityHudService;
import org.monday.backrooms.hud.SanityHudListener;
import org.monday.backrooms.hud.SanityHudService;
import org.monday.backrooms.hud.VectorDisplaysSanityHudService;
import org.monday.backrooms.items.BackroomsItemListener;
import org.monday.backrooms.items.BackroomsItemService;
import org.monday.backrooms.items.SanityService;
import org.monday.backrooms.level.LevelConfigLoader;
import org.monday.backrooms.level.LevelRegistry;
import org.monday.backrooms.level.LevelTitleService;
import org.monday.backrooms.loot.LootTableService;
import org.monday.backrooms.message.MessageService;
import org.monday.backrooms.player.PlayerLevelListener;
import org.monday.backrooms.player.PlayerLevelTracker;
import org.monday.backrooms.resource.ResourceBlockService;
import org.monday.backrooms.room.RoomGenerationService;
import org.monday.backrooms.rule.LevelRuleListener;
import org.monday.backrooms.transition.TransitionListener;
import org.monday.backrooms.transition.TransitionService;
import org.monday.backrooms.worldgen.WorldGenerationService;

public final class Backrooms extends JavaPlugin {

    private ConfigFileService configFileService;
    private MessageService messageService;
    private LevelRegistry levelRegistry;
    private LevelConfigLoader levelConfigLoader;
    private LevelTitleService levelTitleService;
    private PlayerLevelTracker playerLevelTracker;
    private BackroomsItemService itemService;
    private SanityService sanityService;
    private SanityHudService sanityHudService;
    private LootTableService lootTableService;
    private ResourceBlockService resourceBlockService;
    private TransitionService transitionService;
    private RoomGenerationService roomGenerationService;
    private WorldGenerationService worldGenerationService;

    @Override
    public void onEnable() {
        getLogger().info("Enabling BackroomsCore v" + getPluginMeta().getVersion() + "...");
        saveDefaultConfig();
        this.configFileService = new ConfigFileService(this);
        this.configFileService.ensureDefaultFiles();
        this.configFileService.reload();

        this.messageService = new MessageService(this);
        this.levelRegistry = new LevelRegistry();
        this.levelConfigLoader = new LevelConfigLoader(this);
        this.levelTitleService = new LevelTitleService(this);
        this.playerLevelTracker = new PlayerLevelTracker(this);
        this.itemService = new BackroomsItemService(this);
        this.sanityService = new SanityService(this);
        this.sanityHudService = createSanityHudService();
        this.lootTableService = new LootTableService(this);
        this.resourceBlockService = new ResourceBlockService(this);
        this.transitionService = new TransitionService(this);
        this.roomGenerationService = new RoomGenerationService(this);
        this.worldGenerationService = new WorldGenerationService(this);
        getLogger().info("Core services initialized.");

        reloadRuntimeConfig();
        sanityService.start();
        registerListeners();
        registerCommands();

        getLogger().info("BackroomsCore enabled successfully: levels=" + levelRegistry.size()
                + ", enabled=" + levelRegistry.enabledCount()
                + ", disabled=" + levelRegistry.disabledCount()
                + ", items=" + itemService.definitionCount()
                + ", lootTables=" + lootTableService.definitionCount()
                + ", resourceBlocks=" + resourceBlockService.definitionCount()
                + ", transitions=" + transitionService.definitionCount()
                + ", rooms=" + roomGenerationService.definitionCount()
                + ", schematicTemplates=" + worldGenerationService.templateCount() + ".");
    }

    @Override
    public void onDisable() {
        if (levelRegistry != null) {
            levelRegistry.clear();
        }
        if (playerLevelTracker != null) {
            playerLevelTracker.clear();
        }
        if (itemService != null) {
            itemService.clear();
        }
        if (sanityService != null) {
            sanityService.stop();
            sanityService.clear();
        }
        if (sanityHudService != null) {
            sanityHudService.clear();
        }
    }

    public boolean reloadRuntimeConfig() {
        long startMillis = System.currentTimeMillis();
        getLogger().info("Reloading runtime config...");

        ConfigFileService previousConfigFileService = configFileService;
        MessageService previousMessageService = messageService;
        LevelRegistry previousRegistry = levelRegistry;
        LevelConfigLoader previousLevelConfigLoader = levelConfigLoader;
        BackroomsItemService previousItemService = itemService;
        SanityService previousSanityService = sanityService;
        SanityHudService previousSanityHudService = sanityHudService;
        LootTableService previousLootTableService = lootTableService;
        ResourceBlockService previousResourceBlockService = resourceBlockService;
        TransitionService previousTransitionService = transitionService;
        RoomGenerationService previousRoomGenerationService = roomGenerationService;
        WorldGenerationService previousWorldGenerationService = worldGenerationService;
        try {
            reloadConfig();

            ConfigFileService loadedConfigFileService = new ConfigFileService(this);
            loadedConfigFileService.ensureDefaultFiles();
            loadedConfigFileService.reload();
            configFileService = loadedConfigFileService;

            MessageService loadedMessageService = new MessageService(this);
            messageService = loadedMessageService;

            LevelRegistry loadedLevels = new LevelRegistry();
            LevelConfigLoader loadedLevelConfigLoader = new LevelConfigLoader(this);
            loadedLevelConfigLoader.loadInto(loadedLevels);
            if (previousRegistry != null && previousRegistry.size() > 0 && loadedLevels.size() == 0) {
                getLogger().severe("Reload aborted because no levels were loaded; keeping previous level registry to avoid fail-open protection.");
                restoreRuntimeConfig(previousConfigFileService, previousMessageService, previousRegistry, previousLevelConfigLoader,
                        previousItemService, previousSanityService, previousSanityHudService, previousLootTableService, previousResourceBlockService, previousTransitionService,
                        previousRoomGenerationService, previousWorldGenerationService);
                return false;
            }

            // Build fresh service instances first; a failed reload cannot clear or corrupt the live runtime maps.
            levelRegistry = loadedLevels;
            levelConfigLoader = loadedLevelConfigLoader;
            itemService = new BackroomsItemService(this);
            itemService.reload();
            sanityHudService = createSanityHudService();
            sanityHudService.reload();
            if (sanityService == null) {
                sanityService = new SanityService(this);
            }
            sanityService.reload();
            lootTableService = new LootTableService(this);
            lootTableService.reload();
            resourceBlockService = new ResourceBlockService(this);
            resourceBlockService.reload();
            transitionService = new TransitionService(this);
            transitionService.reload();
            roomGenerationService = new RoomGenerationService(this);
            roomGenerationService.reload();
            worldGenerationService = new WorldGenerationService(this);
            worldGenerationService.reload();
            if (playerLevelTracker != null) {
                playerLevelTracker.reconcileOnlinePlayers(false);
            }
            if (previousSanityHudService != null && previousSanityHudService != sanityHudService) {
                previousSanityHudService.clear();
            }
        } catch (RuntimeException exception) {
            if (sanityHudService != null && sanityHudService != previousSanityHudService) {
                sanityHudService.clear();
            }
            restoreRuntimeConfig(previousConfigFileService, previousMessageService, previousRegistry, previousLevelConfigLoader,
                    previousItemService, previousSanityService, previousSanityHudService, previousLootTableService, previousResourceBlockService, previousTransitionService,
                    previousRoomGenerationService, previousWorldGenerationService);
            getLogger().severe("Runtime config reload failed; restored previous runtime snapshot. Cause: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }

        getLogger().info("Runtime config reloaded in " + (System.currentTimeMillis() - startMillis) + "ms: levels="
                + levelRegistry.size() + ", enabled=" + levelRegistry.enabledCount()
                + ", disabled=" + levelRegistry.disabledCount()
                + ", items=" + itemService.definitionCount()
                + ", lootTables=" + lootTableService.definitionCount()
                + ", resourceBlocks=" + resourceBlockService.definitionCount()
                + ", transitions=" + transitionService.definitionCount()
                + ", rooms=" + roomGenerationService.definitionCount()
                + ", schematicTemplates=" + worldGenerationService.templateCount()
                + ", onlinePlayers=" + getServer().getOnlinePlayers().size() + ".");
        return true;
    }

    private void restoreRuntimeConfig(
            ConfigFileService previousConfigFileService,
            MessageService previousMessageService,
            LevelRegistry previousRegistry,
            LevelConfigLoader previousLevelConfigLoader,
            BackroomsItemService previousItemService,
            SanityService previousSanityService,
            SanityHudService previousSanityHudService,
            LootTableService previousLootTableService,
            ResourceBlockService previousResourceBlockService,
            TransitionService previousTransitionService,
            RoomGenerationService previousRoomGenerationService,
            WorldGenerationService previousWorldGenerationService
    ) {
        if (previousConfigFileService != null) {
            configFileService = previousConfigFileService;
        }
        if (previousMessageService != null) {
            messageService = previousMessageService;
        }
        if (previousRegistry != null) {
            levelRegistry = previousRegistry;
        }
        if (previousLevelConfigLoader != null) {
            levelConfigLoader = previousLevelConfigLoader;
        }
        if (previousItemService != null) {
            itemService = previousItemService;
        }
        if (previousSanityService != null) {
            sanityService = previousSanityService;
        }
        if (previousSanityHudService != null) {
            sanityHudService = previousSanityHudService;
        }
        if (previousLootTableService != null) {
            lootTableService = previousLootTableService;
        }
        if (previousResourceBlockService != null) {
            resourceBlockService = previousResourceBlockService;
        }
        if (previousTransitionService != null) {
            transitionService = previousTransitionService;
        }
        if (previousRoomGenerationService != null) {
            roomGenerationService = previousRoomGenerationService;
        }
        if (previousWorldGenerationService != null) {
            worldGenerationService = previousWorldGenerationService;
        }
    }

    public ConfigFileService configFiles() {
        return configFileService;
    }

    public MessageService messages() {
        return messageService;
    }

    public LevelRegistry levels() {
        return levelRegistry;
    }

    public LevelTitleService levelTitles() {
        return levelTitleService;
    }

    public PlayerLevelTracker playerLevels() {
        return playerLevelTracker;
    }

    public BackroomsItemService items() {
        return itemService;
    }

    public SanityService sanity() {
        return sanityService;
    }

    public SanityHudService sanityHud() {
        return sanityHudService;
    }

    private SanityHudService createSanityHudService() {
        if (getServer().getPluginManager().getPlugin("VectorDisplays") == null) {
            return new NoopSanityHudService(this, "VectorDisplays plugin is not installed or not loaded.");
        }
        try {
            Class.forName("top.mrxiaom.hologram.vector.displays.TerminalManager", false, getClassLoader());
            return new VectorDisplaysSanityHudService(this);
        } catch (ClassNotFoundException | LinkageError exception) {
            return new NoopSanityHudService(this, "VectorDisplays API is not on BackroomsCore classpath: " + exception.getMessage());
        }
    }

    public LootTableService lootTables() {
        return lootTableService;
    }

    public ResourceBlockService resources() {
        return resourceBlockService;
    }

    public TransitionService transitions() {
        return transitionService;
    }

    public RoomGenerationService rooms() {
        return roomGenerationService;
    }

    public WorldGenerationService worldgen() {
        return worldGenerationService;
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLevelListener(this), this);
        getServer().getPluginManager().registerEvents(new TransitionListener(this), this);
        getServer().getPluginManager().registerEvents(new LevelRuleListener(this), this);
        getServer().getPluginManager().registerEvents(new BackroomsItemListener(this), this);
        getServer().getPluginManager().registerEvents(new SanityHudListener(this), this);
        getLogger().info("Registered listeners: PlayerLevelListener, TransitionListener, LevelRuleListener, BackroomsItemListener, SanityHudListener.");
    }

    private void registerCommands() {
        BrCommand brCommand = new BrCommand(this);
        registerCommand("br", "Backrooms main command", List.of("backrooms"), new PaperBrCommand(brCommand));
        getLogger().info("Registered Paper command handler: /br.");
    }

    private static final class PaperBrCommand implements BasicCommand {

        private final BrCommand delegate;

        private PaperBrCommand(BrCommand delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(CommandSourceStack stack, String[] args) {
            delegate.execute(stack.getSender(), "br", args);
        }

        @Override
        public Collection<String> suggest(CommandSourceStack stack, String[] args) {
            return delegate.complete(stack.getSender(), "br", args);
        }

        @Override
        public boolean canUse(CommandSender sender) {
            return sender.hasPermission(BrCommand.BASE_PERMISSION);
        }
    }
}
