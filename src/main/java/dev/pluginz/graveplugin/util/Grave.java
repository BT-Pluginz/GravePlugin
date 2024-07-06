package dev.pluginz.graveplugin.util;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Grave {
    private final String playerName;
    private final UUID graveId;
    private final Location location;
    private final ItemStack[] items;
    private final ItemStack[] armorItems;
    private final ItemStack offHand;
    private final UUID armorStandId;
    private long activeTime;
    private final long maxActiveTime;
    private boolean expired;

    public Grave(String playerName, UUID graveId, Location location, ItemStack[] items, ItemStack[] armorItems, ItemStack offHand, UUID armorStandId, long maxActiveTime, boolean expired) {
        this.playerName = playerName;
        this.graveId = graveId;
        this.location = location;
        this.items = items;
        this.armorItems = armorItems;
        this.offHand = offHand;
        this.armorStandId = armorStandId;
        this.activeTime = 0;
        this.maxActiveTime = maxActiveTime;
        this.expired = expired;
    }

    public String getPlayerName() {
        return playerName;
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

    public void incrementActiveTime(long time) {
        this.activeTime += time;
    }

    public long getActiveTime() {
        return activeTime;
    }

    public long getMaxActiveTime() {
        return maxActiveTime;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}