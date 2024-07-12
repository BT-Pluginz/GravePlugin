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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final GraveManager graveManager;
    private final GraveInventoryManager graveInventoryManager;

    public InventoryListener(GravePlugin plugin) {
        this.graveManager = plugin.getGraveManager();
        this.graveInventoryManager = plugin.getGraveInventoryManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        InventoryView inventoryView = event.getView();

        if (graveInventoryManager.isGraveInventory(inventoryView)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                Material type = event.getCurrentItem().getType();
                UUID graveId = graveInventoryManager.getGraveIdFromInventory(inventory);
                Grave grave = graveManager.getGraveFromGraveID(graveId);
                if (graveId != null) {
                    if (type == Material.GREEN_STAINED_GLASS_PANE) {
                        player.closeInventory();
                        graveInventoryManager.restoreInventory(player, inventory, graveId);
                    } else if (type == Material.RED_STAINED_GLASS_PANE) {
                        player.closeInventory();
                        graveInventoryManager.dropGraveItems(grave);
                        graveManager.removeGrave(graveId);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (graveInventoryManager.isGraveInventory(event.getView())) {
            event.setCancelled(true);
        }
    }
}
