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
    private int lvl;
    private float exp;

    public Grave(String playerName, UUID graveId, Location location, ItemStack[] items, ItemStack[] armorItems, ItemStack offHand, UUID armorStandId, long maxActiveTime, boolean expired, int lvl, float exp) {
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
        this.lvl = lvl;
        this.exp = exp;
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


    public int getLvl() {
        return lvl;
    }

    public float getExp() {
        return exp;
    }
}