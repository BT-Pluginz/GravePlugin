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
import dev.pluginz.graveplugin.manager.GraveInventoryManager;
import dev.pluginz.graveplugin.manager.GraveManager;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final GravePlugin plugin;
    private final GraveManager graveManager;
    private final GraveInventoryManager graveInventoryManager;

    public PlayerDeathListener(GravePlugin plugin) {
        this.plugin = plugin;
        this.graveManager = plugin.getGraveManager();
        this.graveInventoryManager = plugin.getGraveInventoryManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) {
            return;
        }

        ItemStack[] items = player.getInventory().getContents();
        boolean hasItems = Arrays.stream(items).anyMatch(item -> item != null && item.getType() != Material.AIR);
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        int lvl = (int) (player.getLevel() * ((double) plugin.getConfigManager().getExpPercentage() / 100));
        float xp = player.getExp();

        if (!hasItems) {
            return;
        }

        Location location = player.getLocation();
        location = getGroundLocation(location);

        event.getDrops().clear();
        event.setDroppedExp(0);

        UUID graveId = graveManager.createGrave(player, location, items, armor, offHand,  lvl, xp);
        placePlayerHead(player, location);
        graveInventoryManager.openGrave(player, graveId);
    }

    private Location getGroundLocation(Location location) {
        World world = location.getWorld();
        int maxSearchDistance = 10; // Adjust this value as needed
        Location original = location.clone();
        boolean isNether = world.getEnvironment() == World.Environment.NETHER;
        int netherRoofY = isNether ? 127 : Integer.MAX_VALUE; // Nether roof is at Y=127

        // First, check the original location
        if (isSuitableLocation(original)) {
            return adjustLocation(original);
        }

        // Search downwards first
        for (int y = 1; y <= maxSearchDistance; y++) {
            Location check = original.clone().subtract(0, y, 0);
            if (isSuitableLocation(check) && (check.getY() <= netherRoofY || original.getY() > netherRoofY)) {
                return adjustLocation(check);
            }
        }

        // If not found, search upwards
        for (int y = 1; y <= maxSearchDistance; y++) {
            Location check = original.clone().add(0, y, 0);
            if (isSuitableLocation(check) && (check.getY() <= netherRoofY || original.getY() > netherRoofY)) {
                return adjustLocation(check);
            }
        }

        // If no suitable location found, return the original location
        Location finalLocation = adjustLocation(original);

        // Check for liquids and move up if necessary
        while (finalLocation.getBlock().getType() == Material.LAVA ||
               finalLocation.getBlock().getType() == Material.WATER ||
               finalLocation.getBlock().getType() == Material.BUBBLE_COLUMN) {
            finalLocation.add(0, 1, 0);
            // Prevent moving above Nether roof if death was below it
            if (isNether && finalLocation.getY() > netherRoofY && original.getY() <= netherRoofY) {
                finalLocation.setY(netherRoofY);
                break;
            }
        }

        return finalLocation;
    }

    private boolean isSuitableLocation(Location location) {
        Block block = location.getBlock();
        Block above = block.getRelative(BlockFace.UP);
        Block below = block.getRelative(BlockFace.DOWN);

        return (block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) &&
               (above.getType() == Material.AIR || above.getType() == Material.CAVE_AIR) &&
                (below.getType().isSolid() || below.isLiquid());
    }
    private Location adjustLocation(Location location) {
        location.setX(location.getBlockX() >= 0 ? Math.round(location.getBlockX()) : Math.floor(location.getBlockX()) + 1);
        location.setY(Math.round(location.getBlockY()));
        location.setZ(location.getBlockZ() >= 0 ? Math.round(location.getBlockZ()) : Math.floor(location.getBlockZ()) + 1);

        double x = location.getX() >= 0 ? 0.5 : -0.5;
        double z = location.getZ() >= 0 ? 0.5 : -0.5;
        location.add(x, 0, z);
        return location;
    }

    private void placePlayerHead(Player player, Location location) {
        Block block = location.getBlock();
        block.setType(Material.PLAYER_HEAD);

        Skull skull = (Skull) block.getState();
        skull.setOwningPlayer(player);
        skull.update();
    }
}
