package dev.pluginz.graveplugin.manager;

import dev.pluginz.graveplugin.GravePlugin;
import dev.pluginz.graveplugin.util.Grave;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GraveManager {
    private final GravePlugin plugin;
    private Map<UUID, Grave> graves;
    private Map<Inventory, UUID> inventoryGraveMap;
    private final long graveExpirationTime;

    public GraveManager(GravePlugin plugin) {
        this.plugin = plugin;
        this.graves = new HashMap<>();
        this.inventoryGraveMap = new HashMap<>();
        this.graveExpirationTime = plugin.getConfig().getLong("graveExpirationTime", 12000);
    }

    public UUID createGrave(Player player, Location location, ItemStack[] items, ItemStack[] armor, ItemStack offHand) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomName(player.getName() + "'s Grave");
        armorStand.setCustomNameVisible(true);
        armorStand.setInvulnerable(true);

        UUID graveId = UUID.randomUUID();
        UUID armorStandId = armorStand.getUniqueId();
        long expirationTime = System.currentTimeMillis() + graveExpirationTime * 50L;
        graves.put(graveId, new Grave(graveId, location, items, armor, offHand, armorStandId, expirationTime));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (graves.containsKey(graveId) && System.currentTimeMillis() >= expirationTime) {
                    removeGrave(graveId);
                }
            }
        }.runTaskLater(plugin, graveExpirationTime);

        plugin.getLogger().info("Created grave for player " + player.getName() + " at " + location);
        for (ItemStack item : items) {
            if (item != null) {
                plugin.getLogger().info("Item: " + item.getType());
            }
        }
        return graveId;
    }
    public boolean isGraveArmorStand(UUID uuid) {
        return graves.containsKey(uuid);
    }

    public void createGraveText(Location location, String playerName, long expirationTime) {
        ArmorStand textStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        textStand.setVisible(false);
        textStand.setGravity(false);
        textStand.setCustomName(playerName + "'s Grave\nExpires in: " + (expirationTime / 1200) + " minutes");
        textStand.setCustomNameVisible(true);
        textStand.setInvulnerable(true);

        plugin.getLogger().info("Created grave text for player " + playerName + " at " + location);
    }

    public void openGrave(Player player, UUID graveId) {
        Grave grave = graves.get(graveId);
        if (grave == null) return;
        int inventorySize = 54;

        Inventory graveInventory = Bukkit.createInventory(null, inventorySize, "Grave of " + player.getName());

        // Add Armor (First Row)
        int startIndex = 0;
        for (int i = 3; i >= 0; i--) {
            ItemStack armor = grave.getArmorItems()[i];
            if (armor != null && armor.getType() != Material.AIR) {
                graveInventory.setItem(startIndex++, armor);
            } else startIndex++;
        }
        ItemStack offHand = grave.getOffHand();
        if (offHand != null && offHand.getType() != Material.AIR) {
            graveInventory.setItem(8, offHand);
        }

        // Add Inventory (Rows 2-4)
        for (int i = 0; i < 27; i++) {
            ItemStack item = grave.getItems()[i + 9];
            if (item != null && item.getType() != Material.AIR) {
                graveInventory.setItem(i + 9, item);
            }
        }

        // Add Hotbar (Row 5)
        for (int i = 0; i < 9; i++) {
            ItemStack item = grave.getItems()[i];
            if (item != null && item.getType() != Material.AIR) {
                graveInventory.setItem(i + 36, item);
            }
        }

        ItemStack greenPane = createGlassPane(Material.GREEN_STAINED_GLASS_PANE, "Restore Items");
        ItemStack redPane = createGlassPane(Material.RED_STAINED_GLASS_PANE, "Drop Items");

        graveInventory.setItem(45, greenPane);
        graveInventory.setItem(53, redPane);

        inventoryGraveMap.put(graveInventory, graveId);

        player.openInventory(graveInventory);
    }

    public boolean isGraveInventory(InventoryView inventoryView) {
        return inventoryView.getTitle().startsWith("Grave of ");
    }

    public void restoreInventory(Player player, Inventory inventory, UUID graveId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack[] armorItems = { inventory.getItem(0), inventory.getItem(1), inventory.getItem(2), inventory.getItem(3), inventory.getItem(8) };
                PlayerInventory inv = player.getInventory();
                for (int i = 0; i < armorItems.length; i++) {
                    ItemStack item = armorItems[i];
                    switch (i) {
                        case 0:
                            if (inv.getHelmet() == null) {
                                inv.setHelmet(item);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                            break;
                        case 1:
                            if (inv.getChestplate() == null) {
                                inv.setChestplate(item);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                            break;
                        case 2:
                            if (inv.getLeggings() == null) {
                                inv.setLeggings(item);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                            break;
                        case 3:
                            if (inv.getBoots() == null) {
                                inv.setBoots(item);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                            break;
                        case 4:
                            if (inv.getItemInOffHand().getType() == Material.AIR) {
                                inv.setItemInOffHand(item);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                            break;
                    }
                }
                for (int i = 9; i < 45; i++) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null && item.getType() != Material.AIR) {
                        if (i >= 36 && i < 45) {
                            if (player.getInventory().getItem(i - 36) == null) {
                                player.getInventory().setItem(i - 36, item);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                        } else {
                            if (player.getInventory().getItem(i) == null) {
                                player.getInventory().setItem(i, item);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                        }
                        plugin.getLogger().info(i + " " + item.getType());
                    }
                }
                removeGrave(graveId);
                inventoryGraveMap.remove(inventory);
            }
        }.runTask(plugin);
    }


    public void dropGraveItems(Inventory inventory, Player player, UUID graveId) {
        Location location = player.getLocation();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                location.getWorld().dropItemNaturally(location, item);
            }
        }
        removeGrave(graveId);
        inventoryGraveMap.remove(inventory);
    }


    public void removeGrave(UUID graveId) {
        Grave grave = graves.get(graveId);
        if (grave != null) {
            ArmorStand armorStand = grave.getLocation().getWorld().getEntitiesByClass(ArmorStand.class).stream()
                .filter(entity -> entity.getUniqueId().equals(grave.getArmorStandId()))
                .findFirst()
                .orElse(null);
            if (armorStand != null) {
                armorStand.remove();
            }
            graves.remove(graveId);
            plugin.getLogger().info("Removed grave with UUID: " + graveId);
        }
    }
    public void saveGraves() {
        //TODO: save functionality
    }

    public void loadGraves() {
        //TODO: load functionality
    }

    private ItemStack createGlassPane(Material material, String name) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            pane.setItemMeta(meta);
        }
        return pane;
    }


    public UUID getGraveIdFromInventory(Inventory inventory) {
        return inventoryGraveMap.get(inventory);
    }


    public long getGraveExpirationTime() {
        return graveExpirationTime;
    }
    public Grave getGraveFromUUID(UUID armorStandUUID) {
        for (Grave grave : graves.values()) {
            if (grave.getArmorStandId().equals(armorStandUUID)) {
                return grave;
            }
        }
        return null;
    }

    public UUID getGraveIdFromArmorStand(UUID armorStandId) {
        for (Map.Entry<UUID, Grave> entry : graves.entrySet()) {
            if (entry.getValue().getArmorStandId().equals(armorStandId)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
