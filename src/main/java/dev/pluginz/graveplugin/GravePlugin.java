package dev.pluginz.graveplugin;

import dev.pluginz.graveplugin.command.GraveCommand;
import dev.pluginz.graveplugin.listener.GraveListener;
import dev.pluginz.graveplugin.manager.ConfigManager;
import dev.pluginz.graveplugin.manager.GraveManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GravePlugin extends JavaPlugin {
    private GraveManager graveManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        graveManager = new GraveManager(this);
        graveManager.loadGraves();
        getServer().getPluginManager().registerEvents(new GraveListener(this), this);
        getCommand("grave").setExecutor(new GraveCommand(this));
    }

    @Override
    public void onDisable() {
        graveManager.saveGraves();
        configManager.saveConfig();
    }

    public GraveManager getGraveManager() {
        return graveManager;
    }
    public ConfigManager getConfigManager(){
        return configManager;
    }
}
