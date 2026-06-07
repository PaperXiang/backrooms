package org.monday.backrooms;

import org.monday.backrooms.command.BrCommand;
import org.monday.backrooms.config.ConfigFileService;
import org.monday.backrooms.level.LevelConfigLoader;
import org.monday.backrooms.level.LevelRegistry;
import org.monday.backrooms.level.LevelTitleService;
import org.monday.backrooms.message.MessageService;
import org.monday.backrooms.player.PlayerLevelListener;
import org.monday.backrooms.player.PlayerLevelTracker;
import org.monday.backrooms.resource.ResourceBlockService;
import org.monday.backrooms.room.RoomGenerationService;
import org.monday.backrooms.rule.LevelRuleListener;
import org.monday.backrooms.transition.TransitionListener;
import org.monday.backrooms.transition.TransitionService;
import org.bukkit.plugin.java.JavaPlugin;

public final class Backrooms extends JavaPlugin {

    private ConfigFileService configFileService;
    private MessageService messageService;
    private LevelRegistry levelRegistry;
    private LevelConfigLoader levelConfigLoader;
    private LevelTitleService levelTitleService;
    private PlayerLevelTracker playerLevelTracker;
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

        var command = getCommand("br");
        if (command == null) {
            getLogger().severe("Command 'br' is not declared in paper-plugin.yml; command registration failed.");
            return;
        }

        command.setExecutor(brCommand);
        command.setTabCompleter(brCommand);
        getLogger().info("Registered command: /br.");
    }
}
