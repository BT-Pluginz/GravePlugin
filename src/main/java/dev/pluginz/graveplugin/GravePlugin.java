package dev.pluginz.graveplugin;

import dev.pluginz.graveplugin.command.GraveCommand;
import dev.pluginz.graveplugin.listener.GraveListener;
import dev.pluginz.graveplugin.manager.GraveManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GravePlugin extends JavaPlugin {
    private GraveManager graveManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        graveManager = new GraveManager(this);
        getServer().getPluginManager().registerEvents(new GraveListener(this, graveManager), this);
        getCommand("grave").setExecutor(new GraveCommand(this));
    }

    @Override
    public void onDisable() {

    }

    public GraveManager getGraveManager() {
        return graveManager;
    }
}
