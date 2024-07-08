package dev.pluginz.graveplugin.listener;

import dev.pluginz.graveplugin.GravePlugin;
import dev.pluginz.graveplugin.manager.GraveManager;
import dev.pluginz.graveplugin.util.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class GraveListener implements Listener {

    private final GravePlugin plugin;
    private final GraveManager graveManager;

    public GraveListener(GravePlugin plugin) {
        this.plugin = plugin;
        this.graveManager = plugin.getGraveManager();
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

        if (!hasItems) {
            return;
        }

        Location location = player.getLocation();
        plugin.getLogger().warning("" + location);
        location = getGroundLocation(location);
        plugin.getLogger().warning("" + location);

        event.getDrops().clear();
        UUID graveId = graveManager.createGrave(player, location, items, armor, offHand);
        placePlayerHead(player, location);
        graveManager.openGrave(player, graveId);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            Location blockLocation = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());
            for (Grave grave : graveManager.getGraves().values()) {
                if (grave.getLocation().getBlockX() == blockLocation.getBlockX() &&
                    grave.getLocation().getBlockY() == blockLocation.getBlockY() &&
                    grave.getLocation().getBlockZ() == blockLocation.getBlockZ()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("You cannot break a grave's player head.");
                    break;
                }
            }
        }
    }

    private Location getGroundLocation(Location location) {
        while (location.getBlock().getType() == Material.LAVA || location.getBlock().getType() == Material.WATER) {
            location.add(0, 1, 0);
        }
        while (location.getBlock().getType() == Material.AIR || location.getBlock().getType() == Material.GRASS || location.getBlock().getType() == Material.TALL_GRASS) {
            location.subtract(0, 1, 0);
        }

        location.setX(location.getBlockX() >= 0 ? Math.round(location.getBlockX()) : Math.floor(location.getBlockX()) + 1);
        location.setY(Math.round(location.getBlockY()) + 2);
        location.setZ(location.getBlockZ() >= 0 ? Math.round(location.getBlockZ()) : Math.floor(location.getBlockZ()) + 1);
        return location;
    }

    private void placePlayerHead(Player player, Location location) {
        Block block = location.getBlock();
        block.setType(Material.PLAYER_HEAD);

        Skull skull = (Skull) block.getState();
        skull.setOwningPlayer(player);
        skull.update();
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        plugin.getLogger().warning(player.getName() + " clicked on " + entity.getType() + " with the UUID " + entity.getUniqueId());
        Grave grave = graveManager.getGraveFromUUID(entity.getUniqueId());
        if (grave == null || !Objects.equals(grave.getPlayerName(), player.getName())) {
            return;
        }
        UUID graveId = grave.getGraveId();

        if (entity.getType() == EntityType.ARMOR_STAND && graveManager.isGraveArmorStand(graveId)) {
            event.setCancelled(true);
            graveManager.openGrave(player, graveId);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        InventoryView inventoryView = event.getView();

        if (graveManager.isGraveInventory(inventoryView)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                Material type = event.getCurrentItem().getType();
                UUID graveId = graveManager.getGraveIdFromInventory(inventory);
                if (graveId != null) {
                    if (type == Material.GREEN_STAINED_GLASS_PANE) {
                        player.closeInventory();
                        graveManager.restoreInventory(player, inventory, graveId);
                    } else if (type == Material.RED_STAINED_GLASS_PANE) {
                        player.closeInventory();
                        graveManager.dropGraveItems(inventory, player, graveId);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (graveManager.isGraveInventory(event.getView())) {
            event.setCancelled(true);
        }
    }
}