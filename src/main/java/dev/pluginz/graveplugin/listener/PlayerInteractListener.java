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
import dev.pluginz.graveplugin.util.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Objects;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

    private final GraveManager graveManager;
    private final GraveInventoryManager graveInventoryManager;

    public PlayerInteractListener(GravePlugin plugin) {
        this.graveManager = plugin.getGraveManager();
        this.graveInventoryManager = plugin.getGraveInventoryManager();
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        Grave grave = graveManager.getGraveFromUUID(entity.getUniqueId());
        if (grave == null || !Objects.equals(grave.getPlayerName(), player.getName())) {
            return;
        }
        UUID graveId = grave.getGraveId();

        if (entity.getType() == EntityType.ARMOR_STAND && graveManager.isGraveArmorStand(graveId)) {
            event.setCancelled(true);
            graveInventoryManager.openGrave(player, graveId);
        }
    }
}
