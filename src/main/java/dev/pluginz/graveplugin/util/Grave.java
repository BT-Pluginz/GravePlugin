package dev.pluginz.graveplugin.util;

import dev.pluginz.graveplugin.GravePlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Grave {
    private final GravePlugin plugin;
    private final UUID graveId;
    private final Location location;
    private final ItemStack[] items;
    private final ItemStack[] armorItems;
    private final ItemStack offHand;
    private final UUID armorStandId;

    public Grave(GravePlugin plugin, UUID graveId, Location location, ItemStack[] items, ItemStack[] armorItems, ItemStack offHand, UUID armorStandId) {
        this.plugin = plugin;
        this.graveId = graveId;
        this.location = location;
        this.items = items;
        this.armorItems = armorItems;
        this.offHand = offHand;
        this.armorStandId = armorStandId;
    }

    public UUID getGraveId() {
        return graveId;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public ItemStack[] getArmorItems() {
        return armorItems;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public UUID getArmorStandId() {
        return armorStandId;
    }
}
