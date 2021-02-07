/*
 * Copyright 2019 Jens Fischer
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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.DepositDiscoveredEvent;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialMinedEvent;
import dev.th3shadowbroker.ouroboros.mines.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        Optional<ApplicableRegionSet> blockRegions = WorldUtils.getBlockRegions(event.getBlock());
        if ( blockRegions.isPresent() && blockRegions.get().testState(null, OuroborosMines.FLAG)) {

            // Check for mining permission
            if (!event.getPlayer().hasPermission(Permissions.FEATURE_MINE.permission)) {
                return;
            }

            Optional<ProtectedRegion> region = WorldUtils.getTopRegion(blockRegions.get());
            Optional<MineableMaterial> minedMaterial = plugin.getMaterialManager().getMaterialProperties(event.getBlock().getType(), region.orElseGet(() -> WorldUtils.getGlobalRegion(blockLocation.getWorld()).get()), BukkitAdapter.adapt(event.getBlock().getWorld()));
            if (minedMaterial.isPresent()) {

                // Abort if opening hours are enabled an the mines are closed
                if (plugin.getAnnouncementManager().hasAny()) {
                    Optional<RegionConfiguration> matchingConfig = plugin.getMaterialManager().getMineableMaterialOverrides().stream().filter(rc -> {
                        boolean worldMatches = rc.getWorld() == event.getBlock().getWorld();
                        boolean idMatches = false;

                        Optional<ProtectedRegion> topRegion = WorldUtils.getTopRegion(blockRegions.get());
                        if (topRegion.isPresent() && topRegion.get().getId().equals(rc.getRegionId())) {
                            idMatches = true;
                        }

                        return worldMatches && idMatches;
                    }).findFirst();

                    if (matchingConfig.isPresent() && !matchingConfig.get().minesAreOpen()) {
                        event.getPlayer().sendMessage(TemplateMessage.from("chat.messages.minesClosed", matchingConfig.get().getConfiguration()).colorize().toString());
                        event.setCancelled(true);
                        return;
                    }
                }

                if (minedMaterial.get().canBeRich()) {
                    //Draw for richness
                    if (!MetaUtils.isRich(event.getBlock())) {
                        int drawnRichness = minedMaterial.get().getDrawnRichness();
                        if (drawnRichness > 0) {
                            MetaUtils.setRichness(event.getBlock(), drawnRichness);
                            Bukkit.getPluginManager().callEvent(new DepositDiscoveredEvent(event.getBlock(), event.getPlayer(), minedMaterial.get(),drawnRichness + 1));
                        }
                    }

                    //If rich
                    if (MetaUtils.isRich(event.getBlock())) {
                        //event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
                        breakBlock(event, minedMaterial.get(), event.getPlayer().getInventory().getItemInMainHand());
                        event.getBlock().setType(minedMaterial.get().getMaterial());
                        MetaUtils.decreaseRichness(event.getBlock());

                        //Fire event for mined material
                        Bukkit.getPluginManager().callEvent(new MaterialMinedEvent(minedMaterial.get(), event.getBlock(), true, event.getPlayer()));

                        event.setCancelled(true);
                        return;
                    }
                }

                //If richness was never set or hit 0
                if (!plugin.getTaskManager().hasPendingReplacementTask(event.getBlock())) {
                    new ReplacementTask(event.getBlock().getLocation(), event.getBlock().getType(), minedMaterial.get().getCooldown());
                }

                //Fire event for mined material
                Bukkit.getPluginManager().callEvent(new MaterialMinedEvent(minedMaterial.get(), event.getBlock(), false, event.getPlayer()));

                //Break it! Replace it!
                //event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
                breakBlock(event, minedMaterial.get(), event.getPlayer().getInventory().getItemInMainHand());
                event.getBlock().setType(minedMaterial.get().getReplacement());
            }

            event.setCancelled(true);
        }
    }

    private void breakBlock(BlockBreakEvent event, MineableMaterial mineableMaterial, ItemStack tool) {
        boolean autoPickup = OuroborosMines.INSTANCE.getConfig().getBoolean("autoPickup", false) || event.getPlayer().hasPermission(Permissions.FEATURE_AUTO_PICKUP.permission);

        // Check for drop group
        if (mineableMaterial.getDropGroup().isPresent()) {
            breakNaturally(event, !mineableMaterial.getDropGroup().get().isOverriding(), autoPickup);
            mineableMaterial.getDropGroup().get().drop(event.getPlayer(), event.getBlock().getLocation());
        // No drop-group assigned
        } else {
            breakNaturally(event, true, autoPickup);
        }

        decreaseToolDurability(event.getPlayer(), tool);
        updateStats(event.getPlayer(), mineableMaterial.getMaterial());
    }

    private void breakNaturally(BlockBreakEvent event, boolean dropNaturalItems, boolean autoPickup) {
        if (autoPickup) {
            event.setDropItems(dropNaturalItems);
            Collection<ItemStack> drops = event.getBlock().getDrops();

            Player player = event.getPlayer();
            Location blockLocation = event.getBlock().getLocation();
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(drops.stream().toArray(ItemStack[]::new));
            overflow.values().forEach(i -> blockLocation.getWorld().dropItem(blockLocation, i));
        } else {
            event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
        }
    }

    private void updateStats(Player player, Material material) {
        player.incrementStatistic(Statistic.MINE_BLOCK, material);
    }

    private void decreaseToolDurability(Player player, ItemStack tool) {
        Optional<ItemStack> miningTool = Optional.ofNullable(tool);
        miningTool.ifPresent( itemStack -> {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof Damageable && tool.getType().getMaxDurability() > 0) {
                Damageable damageable = (Damageable) meta;
                if (damageable.getDamage() + 1 < tool.getType().getMaxDurability()) {
                    damageable.setDamage(damageable.getDamage() + 1);
                    tool.setItemMeta((ItemMeta) damageable);
                } else {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                }
            }
        });
    }

}
