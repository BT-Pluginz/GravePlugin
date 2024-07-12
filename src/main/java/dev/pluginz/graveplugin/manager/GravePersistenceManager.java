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

package dev.pluginz.graveplugin.manager;

import dev.pluginz.graveplugin.GravePlugin;
import dev.pluginz.graveplugin.util.Grave;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class GravePersistenceManager {

    private final GravePlugin plugin;
    private GraveManager graveManager;


    public GravePersistenceManager(GravePlugin plugin) {
        this.plugin = plugin;
        this.graveManager = plugin.getGraveManager();
    }

    public void saveGraves() {
        File file = new File(plugin.getDataFolder(), "graves.yml");
        YamlConfiguration config = new YamlConfiguration();
        Map<UUID, Grave> graves = graveManager.getGraves();

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
            config.set(path + ".lvl", grave.getLvl());
            config.set(path + ".exp", grave.getExp());
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
                int lvl = config.getInt(path + ".lvl", 0);
                float exp = (float) config.getDouble(path + ".exp", 0.0);

                Grave grave =  new Grave(playerName, UUID.fromString(key), location, items, armorItems, offHand, armorStandId, maxActiveTime, expired, lvl,exp);
                graveManager.addGrave(UUID.fromString(key), grave);
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
