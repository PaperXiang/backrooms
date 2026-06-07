package org.monday.backrooms;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.monday.backrooms.base.BaseListener;
import org.monday.backrooms.base.BaseService;
import org.monday.backrooms.command.BrCommand;
import org.monday.backrooms.config.ConfigFileService;
import org.monday.backrooms.corpse.CorpseListener;
import org.monday.backrooms.corpse.CorpseService;
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
import org.monday.backrooms.loot.LootSourceListener;
import org.monday.backrooms.loot.LootSourceService;
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
    private LootSourceService lootSourceService;
    private CorpseService corpseService;
    private BaseService baseService;
    private ResourceBlockService resourceBlockService;
    private TransitionService transitionService;
    private RoomGenerationService roomGenerationService;
    private WorldGenerationService worldGenerationService;
    private RuntimeSnapshot stagedRuntime;

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
        this.lootSourceService = new LootSourceService(this);
        this.corpseService = new CorpseService(this);
        this.baseService = new BaseService(this);
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
                + ", lootSources=" + lootSourceService.definitionCount()
                + ", pendingInsurance=" + corpseService.pendingInsuranceCount()
                + ", bases=" + baseService.definitionCount()
                + ", baseClaims=" + baseService.claimCount()
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

        RuntimeSnapshot previousRuntime = captureRuntime();
        RuntimeSnapshot loadedRuntime;
        try {
            reloadConfig();

            ConfigFileService loadedConfigFileService = new ConfigFileService(this);
            loadedConfigFileService.ensureDefaultFiles();
            loadedConfigFileService.reload();

            MessageService loadedMessageService = new MessageService(this);
            stagedRuntime = previousRuntime.withConfigAndMessages(loadedConfigFileService, loadedMessageService);

            LevelRegistry loadedLevels = new LevelRegistry();
            LevelConfigLoader loadedLevelConfigLoader = new LevelConfigLoader(this);
            loadedLevelConfigLoader.loadInto(loadedLevels);
            if (previousRuntime.levelRegistry() != null && previousRuntime.levelRegistry().size() > 0 && loadedLevels.size() == 0) {
                getLogger().severe("Reload aborted because no levels were loaded; keeping previous level registry to avoid fail-open protection.");
                stagedRuntime = null;
                return false;
            }
            stagedRuntime = stagedRuntime.withLevels(loadedLevels, loadedLevelConfigLoader);

            BackroomsItemService loadedItemService = new BackroomsItemService(this);
            stagedRuntime = stagedRuntime.withItemService(loadedItemService);
            loadedItemService.reload();

            SanityHudService loadedSanityHudService = createSanityHudService();
            stagedRuntime = stagedRuntime.withSanityHudService(loadedSanityHudService);
            loadedSanityHudService.reload();

            SanityService loadedSanityService = new SanityService(this);
            if (previousRuntime.sanityService() != null) {
                previousRuntime.sanityService().copyRuntimeStateTo(loadedSanityService);
            }
            stagedRuntime = stagedRuntime.withSanityService(loadedSanityService);
            loadedSanityService.reload();

            LootTableService loadedLootTableService = new LootTableService(this);
            stagedRuntime = stagedRuntime.withLootTableService(loadedLootTableService);
            loadedLootTableService.reload();

            LootSourceService loadedLootSourceService = new LootSourceService(this);
            stagedRuntime = stagedRuntime.withLootSourceService(loadedLootSourceService);
            loadedLootSourceService.reload();

            CorpseService loadedCorpseService = new CorpseService(this);
            if (previousRuntime.corpseService() != null) {
                previousRuntime.corpseService().copyRuntimeStateTo(loadedCorpseService);
            }
            stagedRuntime = stagedRuntime.withCorpseService(loadedCorpseService);
            loadedCorpseService.reload();

            BaseService loadedBaseService = new BaseService(this);
            stagedRuntime = stagedRuntime.withBaseService(loadedBaseService);
            loadedBaseService.reload();

            ResourceBlockService loadedResourceBlockService = new ResourceBlockService(this);
            stagedRuntime = stagedRuntime.withResourceBlockService(loadedResourceBlockService);
            loadedResourceBlockService.reload();

            TransitionService loadedTransitionService = new TransitionService(this);
            stagedRuntime = stagedRuntime.withTransitionService(loadedTransitionService);
            loadedTransitionService.reload();

            RoomGenerationService loadedRoomGenerationService = new RoomGenerationService(this);
            stagedRuntime = stagedRuntime.withRoomGenerationService(loadedRoomGenerationService);
            loadedRoomGenerationService.reload();

            WorldGenerationService loadedWorldGenerationService = new WorldGenerationService(this);
            stagedRuntime = stagedRuntime.withWorldGenerationService(loadedWorldGenerationService);
            loadedWorldGenerationService.reload();

            loadedRuntime = stagedRuntime;
            stagedRuntime = null;
        } catch (RuntimeException exception) {
            RuntimeSnapshot failedRuntime = stagedRuntime;
            stagedRuntime = null;
            if (failedRuntime != null
                    && failedRuntime.sanityHudService() != null
                    && failedRuntime.sanityHudService() != previousRuntime.sanityHudService()) {
                failedRuntime.sanityHudService().clear();
            }
            getLogger().severe("Runtime config reload failed before staged runtime commit; live runtime was kept unchanged. Cause: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }

        commitRuntime(loadedRuntime);
        if (previousRuntime.sanityService() != null && previousRuntime.sanityService() != sanityService) {
            previousRuntime.sanityService().stop();
            previousRuntime.sanityService().clear();
        }
        if (sanityService != null) {
            sanityService.start();
        }
        if (playerLevelTracker != null) {
            playerLevelTracker.reconcileOnlinePlayers(false);
        }
        if (previousRuntime.sanityHudService() != null && previousRuntime.sanityHudService() != sanityHudService) {
            previousRuntime.sanityHudService().clear();
        }

        getLogger().info("Runtime config reloaded in " + (System.currentTimeMillis() - startMillis) + "ms: levels="
                + levelRegistry.size() + ", enabled=" + levelRegistry.enabledCount()
                + ", disabled=" + levelRegistry.disabledCount()
                + ", items=" + itemService.definitionCount()
                + ", lootTables=" + lootTableService.definitionCount()
                + ", lootSources=" + lootSourceService.definitionCount()
                + ", pendingInsurance=" + corpseService.pendingInsuranceCount()
                + ", bases=" + baseService.definitionCount()
                + ", baseClaims=" + baseService.claimCount()
                + ", resourceBlocks=" + resourceBlockService.definitionCount()
                + ", transitions=" + transitionService.definitionCount()
                + ", rooms=" + roomGenerationService.definitionCount()
                + ", schematicTemplates=" + worldGenerationService.templateCount()
                + ", onlinePlayers=" + getServer().getOnlinePlayers().size() + ".");
        return true;
    }

    public ConfigFileService configFiles() {
        return stagedRuntime == null ? configFileService : stagedRuntime.configFileService();
    }

    public MessageService messages() {
        return stagedRuntime == null ? messageService : stagedRuntime.messageService();
    }

    public LevelRegistry levels() {
        return stagedRuntime == null ? levelRegistry : stagedRuntime.levelRegistry();
    }

    public LevelTitleService levelTitles() {
        return levelTitleService;
    }

    public PlayerLevelTracker playerLevels() {
        return playerLevelTracker;
    }

    public BackroomsItemService items() {
        return stagedRuntime == null ? itemService : stagedRuntime.itemService();
    }

    public SanityService sanity() {
        return stagedRuntime == null ? sanityService : stagedRuntime.sanityService();
    }

    public SanityHudService sanityHud() {
        return stagedRuntime == null ? sanityHudService : stagedRuntime.sanityHudService();
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
        return stagedRuntime == null ? lootTableService : stagedRuntime.lootTableService();
    }

    public LootSourceService lootSources() {
        return stagedRuntime == null ? lootSourceService : stagedRuntime.lootSourceService();
    }

    public CorpseService corpses() {
        return stagedRuntime == null ? corpseService : stagedRuntime.corpseService();
    }

    public BaseService bases() {
        return stagedRuntime == null ? baseService : stagedRuntime.baseService();
    }

    public ResourceBlockService resources() {
        return stagedRuntime == null ? resourceBlockService : stagedRuntime.resourceBlockService();
    }

    public TransitionService transitions() {
        return stagedRuntime == null ? transitionService : stagedRuntime.transitionService();
    }

    public RoomGenerationService rooms() {
        return stagedRuntime == null ? roomGenerationService : stagedRuntime.roomGenerationService();
    }

    public WorldGenerationService worldgen() {
        return stagedRuntime == null ? worldGenerationService : stagedRuntime.worldGenerationService();
    }

    private RuntimeSnapshot captureRuntime() {
        return new RuntimeSnapshot(
                configFileService,
                messageService,
                levelRegistry,
                levelConfigLoader,
                itemService,
                sanityService,
                sanityHudService,
                lootTableService,
                lootSourceService,
                corpseService,
                baseService,
                resourceBlockService,
                transitionService,
                roomGenerationService,
                worldGenerationService
        );
    }

    private void commitRuntime(RuntimeSnapshot runtime) {
        configFileService = runtime.configFileService();
        messageService = runtime.messageService();
        levelRegistry = runtime.levelRegistry();
        levelConfigLoader = runtime.levelConfigLoader();
        itemService = runtime.itemService();
        sanityService = runtime.sanityService();
        sanityHudService = runtime.sanityHudService();
        lootTableService = runtime.lootTableService();
        lootSourceService = runtime.lootSourceService();
        corpseService = runtime.corpseService();
        baseService = runtime.baseService();
        resourceBlockService = runtime.resourceBlockService();
        transitionService = runtime.transitionService();
        roomGenerationService = runtime.roomGenerationService();
        worldGenerationService = runtime.worldGenerationService();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLevelListener(this), this);
        getServer().getPluginManager().registerEvents(new TransitionListener(this), this);
        getServer().getPluginManager().registerEvents(new LevelRuleListener(this), this);
        getServer().getPluginManager().registerEvents(new BackroomsItemListener(this), this);
        getServer().getPluginManager().registerEvents(new SanityHudListener(this), this);
        getServer().getPluginManager().registerEvents(new LootSourceListener(this), this);
        getServer().getPluginManager().registerEvents(new CorpseListener(this), this);
        getServer().getPluginManager().registerEvents(new BaseListener(this), this);
        getLogger().info("Registered listeners: PlayerLevelListener, TransitionListener, LevelRuleListener, BackroomsItemListener, SanityHudListener, LootSourceListener, CorpseListener, BaseListener.");
    }

    private void registerCommands() {
        BrCommand brCommand = new BrCommand(this);
        registerCommand("br", "Backrooms main command", List.of("backrooms"), new PaperBrCommand(brCommand));
        getLogger().info("Registered Paper command handler: /br.");
    }

    private record RuntimeSnapshot(
            ConfigFileService configFileService,
            MessageService messageService,
            LevelRegistry levelRegistry,
            LevelConfigLoader levelConfigLoader,
            BackroomsItemService itemService,
            SanityService sanityService,
            SanityHudService sanityHudService,
            LootTableService lootTableService,
            LootSourceService lootSourceService,
            CorpseService corpseService,
            BaseService baseService,
            ResourceBlockService resourceBlockService,
            TransitionService transitionService,
            RoomGenerationService roomGenerationService,
            WorldGenerationService worldGenerationService
    ) {

        private RuntimeSnapshot withConfigAndMessages(ConfigFileService configFileService, MessageService messageService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withLevels(LevelRegistry levelRegistry, LevelConfigLoader levelConfigLoader) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withItemService(BackroomsItemService itemService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withSanityService(SanityService sanityService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withSanityHudService(SanityHudService sanityHudService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withLootTableService(LootTableService lootTableService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withLootSourceService(LootSourceService lootSourceService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withCorpseService(CorpseService corpseService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withBaseService(BaseService baseService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withResourceBlockService(ResourceBlockService resourceBlockService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withTransitionService(TransitionService transitionService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withRoomGenerationService(RoomGenerationService roomGenerationService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }

        private RuntimeSnapshot withWorldGenerationService(WorldGenerationService worldGenerationService) {
            return new RuntimeSnapshot(configFileService, messageService, levelRegistry, levelConfigLoader,
                    itemService, sanityService, sanityHudService, lootTableService, lootSourceService, corpseService, baseService, resourceBlockService,
                    transitionService, roomGenerationService, worldGenerationService);
        }
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
