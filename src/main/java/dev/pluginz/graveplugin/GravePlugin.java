/*
 * This file is part of BT's Graves, licensed under the MIT License.
 *
 *  Copyright (c) BT Pluginz <github@tubyoub.de>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package dev.pluginz.graveplugin;

import dev.pluginz.graveplugin.command.GraveCommand;
import dev.pluginz.graveplugin.command.GraveTabCompleter;
import dev.pluginz.graveplugin.listener.*;
import dev.pluginz.graveplugin.manager.*;
import dev.pluginz.graveplugin.util.VersionChecker;
import dev.pluginz.graveplugin.util.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class GravePlugin extends JavaPlugin {
    private final String version = "1.0";
    private final String project = "WGgaXko0";
    private final int pluginId = 22622;

    private GraveManager graveManager;
    private GraveInventoryManager graveInventoryManager;
    private GravePersistenceManager gravePersistenceManager;
    private GraveTimeoutManager graveTimeoutManager;
    private ConfigManager configManager;
    private boolean newVersion;

    @Override
    public void onEnable() {
        this.getLogger().info("_____________________________ ");
        this.getLogger().info("\\______   \\__    ___/  _____/ ");
        this.getLogger().info(" |    |  _/ |    | /   \\  ___ ");
        this.getLogger().info(" |    |   \\ |    | \\    \\_\\  \\");
        this.getLogger().info(" |______  / |____|  \\______  /" + "     BT Graves v" + version);
        this.getLogger().info("        \\/                 \\/ " + "     Running on " + Bukkit.getServer().getName() + " using Blackmagic");

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        if (configManager.isCheckVersion()) {
            newVersion = VersionChecker.isNewVersionAvailable(version, project);
            if (newVersion) {
                this.getLogger().warning("There is a new Version available for BT's CombatLogger");
            }
        }

        graveManager = new GraveManager(this);
        graveInventoryManager = new GraveInventoryManager(this);
        gravePersistenceManager = new GravePersistenceManager(this);
        graveTimeoutManager = new GraveTimeoutManager(this);

        graveManager.setPersistenceManager(gravePersistenceManager);
        gravePersistenceManager.loadGraves();
        graveTimeoutManager.startGraveTimeoutTask();
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPistonExtendListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new LiquidFlowListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getCommand("grave").setExecutor(new GraveCommand(this));
        getCommand("grave").setTabCompleter(new GraveTabCompleter());

        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        gravePersistenceManager.saveGraves();
        configManager.saveConfig();
        this.getLogger().info("-----");
        this.getLogger().info(" ");
        this.getLogger().warning("If this is a reload please note that this could break the Plugin");
        this.getLogger().info(" ");
        this.getLogger().info("-----");
    }

    public String getPluginPrefix() {
        return ChatColor.WHITE + "[" + ChatColor.DARK_GRAY + "BTG" + ChatColor.WHITE + "] ";
    }

    public void sendPluginMessages(CommandSender sender, String type) {
        if ("title".equals(type)) {
            sender.sendMessage(ChatColor.BLACK + "◢◤" + ChatColor.DARK_GRAY + "BT" + ChatColor.BLACK + "'"+ ChatColor.DARK_GRAY + "s" + ChatColor.DARK_PURPLE + " Graves" + ChatColor.BLACK + "◥◣");
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

    public GraveInventoryManager getGraveInventoryManager() {
        return graveInventoryManager;
    }
    public GravePersistenceManager getGravePersistenceManager() {
        return gravePersistenceManager;
    }
    public GraveTimeoutManager getGraveTimeoutManager() {
        return graveTimeoutManager;
    }
}