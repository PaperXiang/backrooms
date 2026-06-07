package org.monday.backrooms;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.monday.backrooms.command.BrCommand;
import org.monday.backrooms.config.ConfigFileService;
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

public final class Backrooms extends JavaPlugin {

    private ConfigFileService configFileService;
    private MessageService messageService;
    private LevelRegistry levelRegistry;
    private LevelConfigLoader levelConfigLoader;
    private LevelTitleService levelTitleService;
    private PlayerLevelTracker playerLevelTracker;
    private LootTableService lootTableService;
    private ResourceBlockService resourceBlockService;
    private TransitionService transitionService;
    private RoomGenerationService roomGenerationService;

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
        this.lootTableService = new LootTableService(this);
        this.resourceBlockService = new ResourceBlockService(this);
        this.transitionService = new TransitionService(this);
        this.roomGenerationService = new RoomGenerationService(this);
        getLogger().info("Core services initialized.");

        reloadRuntimeConfig();
        registerListeners();
        registerCommands();

        getLogger().info("BackroomsCore enabled successfully: levels=" + levelRegistry.size()
                + ", enabled=" + levelRegistry.enabledCount()
                + ", disabled=" + levelRegistry.disabledCount()
                + ", lootTables=" + lootTableService.definitionCount()
                + ", resourceBlocks=" + resourceBlockService.definitionCount()
                + ", transitions=" + transitionService.definitionCount()
                + ", rooms=" + roomGenerationService.definitionCount() + ".");
    }

    @Override
    public void onDisable() {
        if (levelRegistry != null) {
            levelRegistry.clear();
        }
        if (playerLevelTracker != null) {
            playerLevelTracker.clear();
        }
    }

    public boolean reloadRuntimeConfig() {
        long startMillis = System.currentTimeMillis();
        getLogger().info("Reloading runtime config...");

        LevelRegistry previousRegistry = levelRegistry;
        try {
            reloadConfig();
            configFileService.reload();
            messageService.reload();

            LevelRegistry loadedLevels = new LevelRegistry();
            levelConfigLoader.loadInto(loadedLevels);
            if (previousRegistry != null && previousRegistry.size() > 0 && loadedLevels.size() == 0) {
                getLogger().severe("Reload aborted because no levels were loaded; keeping previous level registry to avoid fail-open protection.");
                return false;
            }

            levelRegistry = loadedLevels;
            lootTableService.reload();
            resourceBlockService.reload();
            transitionService.reload();
            roomGenerationService.reload();
            if (playerLevelTracker != null) {
                playerLevelTracker.reconcileOnlinePlayers(false);
            }
        } catch (RuntimeException exception) {
            if (previousRegistry != null) {
                levelRegistry = previousRegistry;
            }
            getLogger().severe("Runtime config reload failed; kept previous level registry. Cause: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }

        getLogger().info("Runtime config reloaded in " + (System.currentTimeMillis() - startMillis) + "ms: levels="
                + levelRegistry.size() + ", enabled=" + levelRegistry.enabledCount()
                + ", disabled=" + levelRegistry.disabledCount()
                + ", lootTables=" + lootTableService.definitionCount()
                + ", resourceBlocks=" + resourceBlockService.definitionCount()
                + ", transitions=" + transitionService.definitionCount()
                + ", rooms=" + roomGenerationService.definitionCount()
                + ", onlinePlayers=" + getServer().getOnlinePlayers().size() + ".");
        return true;
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

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLevelListener(this), this);
        getServer().getPluginManager().registerEvents(new TransitionListener(this), this);
        getServer().getPluginManager().registerEvents(new LevelRuleListener(this), this);
        getLogger().info("Registered listeners: PlayerLevelListener, TransitionListener, LevelRuleListener.");
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
