package org.monday.backrooms;

import org.monday.backrooms.command.BrCommand;
import org.monday.backrooms.level.LevelConfigLoader;
import org.monday.backrooms.level.LevelRegistry;
import org.monday.backrooms.message.MessageService;
import org.bukkit.plugin.java.JavaPlugin;

public final class Backrooms extends JavaPlugin {

    private MessageService messageService;
    private LevelRegistry levelRegistry;
    private LevelConfigLoader levelConfigLoader;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.messageService = new MessageService(this);
        this.levelRegistry = new LevelRegistry();
        this.levelConfigLoader = new LevelConfigLoader(this);

        reloadRuntimeConfig();
        registerCommands();

        getLogger().info("BackroomsCore enabled with " + levelRegistry.size() + " configured levels.");
    }

    @Override
    public void onDisable() {
        if (levelRegistry != null) {
            levelRegistry.clear();
        }
    }

    public void reloadRuntimeConfig() {
        reloadConfig();
        messageService.reload();
        levelRegistry.clear();
        levelConfigLoader.loadInto(levelRegistry);
    }

    public MessageService messages() {
        return messageService;
    }

    public LevelRegistry levels() {
        return levelRegistry;
    }

    private void registerCommands() {
        BrCommand brCommand = new BrCommand(this);

        var command = getCommand("br");
        if (command == null) {
            getLogger().severe("Command 'br' is not declared in paper-plugin.yml.");
            return;
        }

        command.setExecutor(brCommand);
        command.setTabCompleter(brCommand);
    }
}
