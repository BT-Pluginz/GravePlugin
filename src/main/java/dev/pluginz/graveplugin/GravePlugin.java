package dev.pluginz.graveplugin;

import dev.pluginz.graveplugin.command.GraveCommand;
import dev.pluginz.graveplugin.listener.GraveListener;
import dev.pluginz.graveplugin.manager.ConfigManager;
import dev.pluginz.graveplugin.manager.GraveManager;
import dev.pluginz.graveplugin.util.VersionChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class GravePlugin extends JavaPlugin {
    private final String version = "1.0";
    private final String project = "qiyG0tnT";

    private GraveManager graveManager;
    private ConfigManager configManager;
    private boolean newVersion;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        if (configManager.isCheckVersion()) {
            newVersion = VersionChecker.isNewVersionAvailable(version, project);
            if (newVersion) {
                this.getLogger().warning("There is a new Version available for BT's CombatLogger");
            }
        }

        graveManager = new GraveManager(this);
        graveManager.loadGraves();
        getServer().getPluginManager().registerEvents(new GraveListener(this), this);
        getCommand("grave").setExecutor(new GraveCommand(this));
    }

    @Override
    public void onDisable() {
        graveManager.saveGraves();
        //configManager.saveConfig();
    }

    public String getPluginPrefix() {
        return ChatColor.WHITE + "[" + ChatColor.DARK_GRAY + "BTG" + ChatColor.WHITE + "] ";
    }

    public void sendPluginMessages(CommandSender sender, String type) {
        if ("title".equals(type)) {
            sender.sendMessage(ChatColor.BLACK + "◢◤" + ChatColor.DARK_GRAY + "BT" + ChatColor.BLACK + "'"+ ChatColor.DARK_GRAY + "s" + ChatColor.DARK_PURPLE + " CombatLogger" + ChatColor.BLACK + "◥◣");
        } else if ("line".equals(type)) {
            sender.sendMessage(ChatColor.BLACK + "-" + ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + "-" + ChatColor.YELLOW + "-" + ChatColor.LIGHT_PURPLE + "-" + ChatColor.DARK_PURPLE + "-"
                + ChatColor.BLACK + "-" + ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + "-" + ChatColor.YELLOW + "-" + ChatColor.LIGHT_PURPLE + "-" + ChatColor.DARK_PURPLE + "-"
                + ChatColor.BLACK + "-" + ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + "-" + ChatColor.YELLOW + "-" + ChatColor.LIGHT_PURPLE + "-" + ChatColor.DARK_PURPLE + "-"
                + ChatColor.BLACK + "-");
        }
    }

    public String getVersion(){
        return version;
    }
    public boolean isNewVersion(){
        return newVersion;
    }
    public GraveManager getGraveManager() {
        return graveManager;
    }
    public ConfigManager getConfigManager(){
        return configManager;
    }
}