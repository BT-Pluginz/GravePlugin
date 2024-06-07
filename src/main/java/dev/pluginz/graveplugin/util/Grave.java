package dev.pluginz.graveplugin.util;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Grave {
    private final UUID graveId;
    private final Location location;
    private final ItemStack[] items;
    private final ItemStack[] armorItems;
    private final ItemStack offHand;
    private final UUID armorStandId;
    private final long expirationTime;

    public Grave(UUID graveId, Location location, ItemStack[] items, ItemStack[] armorItems, ItemStack offHand, UUID armorStandId, long expirationTime) {
        this.graveId = graveId;
        this.location = location;
        this.items = items;
        this.armorItems = armorItems;
        this.offHand = offHand;
        this.armorStandId = armorStandId;
        this.expirationTime = expirationTime;
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

    public long getExpirationTime() {
        return expirationTime;
    }
}
