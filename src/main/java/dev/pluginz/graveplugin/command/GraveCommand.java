package dev.pluginz.graveplugin.command;

import dev.pluginz.graveplugin.GravePlugin;
import dev.pluginz.graveplugin.manager.GraveManager;
import dev.pluginz.graveplugin.util.Grave;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GraveCommand implements CommandExecutor {
    private final GravePlugin plugin;
    private final GraveManager graveManager;
    private final String prefix;

    public GraveCommand(GravePlugin plugin) {
        this.plugin = plugin;
        this.graveManager = plugin.getGraveManager();
        this.prefix = plugin.getPluginPrefix();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                handleInfo(sender, args);
                return true;
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        plugin.sendPluginMessages(sender, "title");
        sender.sendMessage(ChatColor.GREEN + "Author: BTPluginz");
        sender.sendMessage(ChatColor.GREEN + "Version: " + plugin.getVersion());
        if (plugin.getConfigManager().isCheckVersion()) {
            if (plugin.isNewVersion()) {
                sender.sendMessage(ChatColor.YELLOW + "A new version is available! Update at: " + ChatColor.UNDERLINE + "https://modrinth.com/project/bts-combatlogger");
            } else {
                sender.sendMessage(ChatColor.GREEN + "You are using the latest version!");
            }
        } else {
            sender.sendMessage(ChatColor.GOLD +  "Version checking is disabled!");
        }
        TextComponent githubLink = new TextComponent(ChatColor.DARK_GRAY + "" + ChatColor.UNDERLINE + "GitHub");
        githubLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/BT-Pluginz/CombatLogger"));
        githubLink.setUnderlined(true);
        sender.spigot().sendMessage(githubLink);

        TextComponent discordLink = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "Discord");
        discordLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.pluginz.dev"));
        discordLink.setUnderlined(true);
        sender.spigot().sendMessage(discordLink);

        sender.sendMessage("If you have any issues please report them on GitHub or on the Discord.");
        plugin.sendPluginMessages(sender, "line");
    }

    public boolean handleReload(CommandSender sender) {
        if (sender.hasPermission("graveplugin.reload")) {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(prefix + "The config reloaded.");
        } else {
            sender.sendMessage(prefix + ChatColor.RED +  "You do not have permission to reload the config.");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(prefix + "Grave Plugin Commands:");
        sender.sendMessage(prefix + "/grave info - Show plugin information");
        sender.sendMessage(prefix + "/grave list - List all active graves");
        sender.sendMessage(prefix + "/grave list <player> - List graves for a specific player");
    }
}
