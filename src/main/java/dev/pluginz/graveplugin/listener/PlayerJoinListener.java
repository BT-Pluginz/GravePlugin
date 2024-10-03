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

package dev.pluginz.graveplugin.listener;

import dev.pluginz.graveplugin.GravePlugin;
import dev.pluginz.graveplugin.util.VersionChecker;
import dev.pluginz.graveplugin.util.VersionChecker.VersionInfo;
import dev.pluginz.graveplugin.util.VersionChecker.UpdateUrgency;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final GravePlugin plugin;
    private final VersionInfo versionInfo;

    public PlayerJoinListener(GravePlugin plugin) {
        this.plugin = plugin;
        this.versionInfo = plugin.getVersionInfo();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player has admin privileges
        if (player.hasPermission("btgraves.admin") && plugin.getConfigManager().isCheckVersion()) {
            // Alert if a critical update is available
            if (versionInfo.isNewVersionAvailable && versionInfo.urgency == UpdateUrgency.CRITICAL || versionInfo.urgency == UpdateUrgency.HIGH) {
                player.sendMessage(  plugin.getPluginPrefix() + ChatColor.RED + "A critical update for BT Grave is available!");
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "Please update to version: " + versionInfo.latestVersion);
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "Backup your config");
                // only works this way and idk why
                TextComponent modrinthLink = new TextComponent(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Modrinth");
                    modrinthLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/bt-graves/version/" + versionInfo.latestVersion));
                    modrinthLink.setUnderlined(true);
                TextComponent message = new TextComponent(plugin.getPluginPrefix() + ChatColor.RED + "Download the new version now from ");
                message.addExtra(modrinthLink);

                player.spigot().sendMessage(message);
            }
        }
    }
}