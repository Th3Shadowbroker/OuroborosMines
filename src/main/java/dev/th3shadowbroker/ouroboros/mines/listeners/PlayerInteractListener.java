/*
 * Copyright 2020 Jens Fischer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.th3shadowbroker.ouroboros.mines.listeners;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.drops.DropGroupCreator;
import dev.th3shadowbroker.ouroboros.mines.util.TemplateMessage;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) {
            if (DropGroupCreator.creationPending(event.getPlayer())) {
                Player player = event.getPlayer();
                Container container = (Container) event.getClickedBlock().getState();
                String dropGroupName = DropGroupCreator.getDropGroupName(event.getPlayer());

                // Drop group does not exist
                if (!OuroborosMines.INSTANCE.getDropManager().getDropGroup(dropGroupName).isPresent()) {
                    try {
                        DropGroupCreator.saveDropGroup(container.getInventory(), dropGroupName);
                        player.sendMessage(TemplateMessage.from("chat.messages.dropGroupCreated").insert("name", dropGroupName).colorize().toString());
                    } catch (IOException ex) {
                        player.sendMessage(TemplateMessage.from("chat.messages.error").insert("error", ex.getMessage()).colorize().toString());
                    }

                    // Done!
                    DropGroupCreator.clearCreationStatus(player);
                // Drop group exists
                } else {
                    player.sendMessage(TemplateMessage.from("chat.messages.dropGroupExists").insert("name", dropGroupName).colorize().toString());
                }

                event.setCancelled(true);
            }
        }
    }

}
