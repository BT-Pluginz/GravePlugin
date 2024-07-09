package dev.pluginz.graveplugin.manager;

import dev.pluginz.graveplugin.GravePlugin;
import dev.pluginz.graveplugin.util.Grave;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GraveManager {
    private static GravePlugin plugin = null;
    private Map<UUID, Grave> graves;
    private Map<Inventory, UUID> inventoryGraveMap;
    private long lastUpdateTime;

    public GraveManager(GravePlugin plugin) {
        this.plugin = plugin;
        this.graves = new HashMap<>();
        this.inventoryGraveMap = new HashMap<>();
        this.startGraveTimeoutTask();
    }

    public UUID createGrave(Player player, Location location, ItemStack[] items, ItemStack[] armor, ItemStack offHand) {
        double x = location.getX() >= 0 ? 0.5 : -0.5;
        double y = -1.0;
        double z = location.getZ() >= 0 ? 0.5 : -0.5;
        location.add(x, y, z);
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomName(player.getName() + "'s Grave");
        armorStand.setCustomNameVisible(true);
        armorStand.setInvulnerable(true);

        UUID graveId = UUID.randomUUID();
        UUID armorStandId = armorStand.getUniqueId();

        long maxActiveTime = plugin.getConfigManager().getGraveTimeout() * 60 * 1000;
        if(plugin.getConfigManager().getGraveTimeout() == -1) {
            maxActiveTime = -1;
        }

        Grave grave = new Grave(player.getName(), graveId, location, items, armor, offHand, armorStandId, maxActiveTime, false);
        graves.put(graveId, grave);

        plugin.getLogger().info("Created grave for player " + player.getName() + " at " + location);
        saveGraves();
        return graveId;
    }

    public static String getColoredTime(Grave grave) {
        long remainingTime = grave.getMaxActiveTime() - grave.getActiveTime();
        int remainingSeconds = (int) (remainingTime / 1000); // Convert milliseconds to seconds

        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;

        String time = String.format("%02dh %02dm %02ds", hours, minutes, seconds);

        double ratio = (double) grave.getActiveTime() / grave.getMaxActiveTime();
        ChatColor color;
        if (ratio < 0.33) {
            color = ChatColor.GREEN;
        } else if (ratio < 0.66) {
            color = ChatColor.GOLD;
        } else {
            color = ChatColor.RED;
        }
        return color + time;
    }

    public void openGrave(Player player, UUID graveId) {
        Grave grave = graves.get(graveId);
        if (grave == null || grave.isExpired()){
            return;
        }
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
                    if (item == null || item.getType() == Material.AIR) {
                        continue;
                    }
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

    public void dropGraveItems(Grave grave) {
        World world = grave.getLocation().getWorld();
        if (world == null) return;

        // Drop main inventory items
        for (ItemStack item : grave.getItems()) {
            if (item != null) {
                world.dropItemNaturally(grave.getLocation(), item);
            }
        }
    }

    public void removeGrave(UUID graveId) {
        Grave grave = graves.remove(graveId);
        if (grave != null) {
            ArmorStand armorStand = (ArmorStand) grave.getLocation().getWorld().getEntitiesByClass(ArmorStand.class).stream()
                    .filter(entity -> entity.getUniqueId().equals(grave.getArmorStandId()))
                    .findFirst()
                    .orElse(null);
            if (armorStand != null) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> armorStand.remove(), 1);
                armorStand.remove();
            }

            Block block = grave.getLocation().getBlock();
            if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
                block.setType(Material.AIR);
            }

            saveGraves();
            plugin.getLogger().info("Removed grave with UUID: " + graveId);
        }
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

    public Grave getGraveFromUUID(UUID armorStandUUID) {
        for (Grave grave : graves.values()) {
            if (grave.getArmorStandId().equals(armorStandUUID)) {
                return grave;
            }
        }
        return null;
    }

    public UUID getArmorStandIdFromGraveId(UUID graveId) {
        Grave grave = graves.get(graveId);
        if (grave != null) {
            return grave.getArmorStandId();
        }
        return null;
    }

    public boolean isGraveArmorStand(UUID uuid) {
        return graves.containsKey(uuid);
    }

    public UUID getGraveIdFromArmorStand(UUID armorStandId) {
        for (Map.Entry<UUID, Grave> entry : graves.entrySet()) {
            if (entry.getValue().getArmorStandId().equals(armorStandId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Grave getGraveFromGraveID(UUID graveId) {
        return graves.get(graveId);
    }

    public Map<UUID, Grave> getGraves() {
        return graves;
    }

    private void startGraveTimeoutTask() {
        lastUpdateTime = System.currentTimeMillis();
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;

            Iterator<Grave> iterator = graves.values().iterator();

            while (iterator.hasNext()) {
                Grave grave = iterator.next();
                grave.incrementActiveTime(elapsedTime);

                if (grave.getMaxActiveTime() == -1) {
                    continue;
                }

                if (grave.isExpired()) {
                    if (grave.getLocation().getWorld().isChunkLoaded(grave.getLocation().getBlockX() >> 4, grave.getLocation().getBlockZ() >> 4)) {
                        if (isPlayerNearby(grave.getLocation(), 50)) {
                            this.dropGraveItems(grave);
                            this.removeGrave(grave.getGraveId());
                            iterator.remove();
                        }
                    }
                } else if (isPlayerNearby(grave.getLocation(), 50)) {
                    long remainingTime = grave.getMaxActiveTime() - grave.getActiveTime();
                    String coloredTime = getColoredTime(grave);
                    updateGraveName(grave.getGraveId(), coloredTime);
                }
            }
        }, 0L, 20L); // 20 ticks = 1 second

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            saveGraves();
        }, 0L, 200L); // 200 ticks = 10 seconds
    }

    private boolean isPlayerNearby(Location location, double radius) {
        return location.getWorld().getPlayers().stream()
                .anyMatch(player -> player.getLocation().distanceSquared(location) <= radius * radius);
    }

    public void updateGraveName(UUID graveId, String coloredTime) {
        Grave grave = graves.get(graveId);
        if (grave != null) {
            Location location = grave.getLocation();
            World world = location.getWorld();
            if (world != null) {
                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(this.getArmorStandIdFromGraveId(graveId));
                String playerName = grave.getPlayerName(); // Assuming you have this method in Grave class
                armorStand.setCustomName(playerName + "'s Grave - " + coloredTime);
            }
        }
    }

    public void saveGraves() {
        File file = new File(plugin.getDataFolder(), "graves.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Grave> entry : graves.entrySet()) {
            String path = "graves." + entry.getKey();
            Grave grave = entry.getValue();
            config.set(path + ".playerName", grave.getPlayerName());
            config.set(path + ".location", grave.getLocation().toVector());
            config.set(path + ".world", grave.getLocation().getWorld().getName());
            config.set(path + ".armorStandId", grave.getArmorStandId().toString());
            config.set(path + ".activeTime", grave.getActiveTime());
            config.set(path + ".maxActiveTime", grave.getMaxActiveTime());
            config.set(path + ".expired", grave.isExpired());
            config.set(path + ".items", itemStackArrayToBase64(grave.getItems()));
            config.set(path + ".armorItems", itemStackArrayToBase64(grave.getArmorItems()));
            config.set(path + ".offHand", itemStackToBase64(grave.getOffHand()));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save graves: " + e.getMessage());
        }
    }

    public void loadGraves() {
        File file = new File(plugin.getDataFolder(), "graves.yml");
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("graves");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = "graves." + key;
                String worldName = config.getString(path + ".world");
                if (worldName == null) {
                    plugin.getLogger().severe("World name is null for grave " + key);
                    continue;
                }

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().severe("World " + worldName + " does not exist on the server");
                    continue;
                }

                String playerName = config.getString(path + ".playerName");
                Location location = config.getVector(path + ".location").toLocation(world);
                UUID armorStandId = UUID.fromString(config.getString(path + ".armorStandId"));
                long activeTime = config.getLong(path + ".activeTime", 0);
                long maxActiveTime = config.getLong(path + ".maxActiveTime");
                boolean expired = config.getBoolean(path + ".expired", false);
                ItemStack[] items = itemStackArrayFromBase64(config.getString(path + ".items"));
                ItemStack[] armorItems = itemStackArrayFromBase64(config.getString(path + ".armorItems"));
                ItemStack offHand = itemStackFromBase64(config.getString(path + ".offHand"));

                Grave grave =  new Grave(playerName, UUID.fromString(key), location, items, armorItems, offHand, armorStandId, maxActiveTime, expired);
                graves.put(UUID.fromString(key), grave);
                grave.incrementActiveTime(activeTime);

            }
        }
    }

    private String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    private String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    private ItemStack[] itemStackArrayFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load item stacks.", e);
        }
    }

    private ItemStack itemStackFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load item stack.", e);
        }
    }
}