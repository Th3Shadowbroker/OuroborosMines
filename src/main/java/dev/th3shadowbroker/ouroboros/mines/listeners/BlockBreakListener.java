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

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.DefaultDropsCheckEvent;
import dev.th3shadowbroker.ouroboros.mines.events.DepositDiscoveredEvent;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialMinedEvent;
import dev.th3shadowbroker.ouroboros.mines.events.RegionCheckEvent;
import dev.th3shadowbroker.ouroboros.mines.events.thirdparty.itemsadder.RemoveCustomBlockEvent;
import dev.th3shadowbroker.ouroboros.mines.regions.MiningRegion;
import dev.th3shadowbroker.ouroboros.mines.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<MiningRegion> region = plugin.getRegionProvider().getRegion(event.getBlock());
        if ((region.isPresent() && region.get().isMiningRegion()) || plugin.getConfig().getBoolean("default", false)) {
          
            // Check for mining permission
            if (!event.getPlayer().hasPermission(Permissions.FEATURE_MINE.permission)) {
                return;
            }

            // Send a region check event to allow thirdparty modules to apply their own logic.
            if (!performRegionCheck(event.getPlayer(), region.orElse(null), event.getBlock())) {
                return;
            }
          
            Optional<MineableMaterial> minedMaterial = plugin.getMaterialManager().getMaterialProperties(event.getBlock(), event.getBlock().getType(), region.orElse(null), event.getBlock().getWorld());

            if (minedMaterial.isPresent()) {

                // Abort if opening hours are enabled an the mines are closed
                if (plugin.getAnnouncementManager().hasAny()) {
                    Optional<RegionConfiguration> matchingConfig = getRegionConfiguration(event.getBlock());

                    if (matchingConfig.isPresent() && !matchingConfig.get().minesAreOpen()) {
                        event.getPlayer().sendMessage(TemplateMessage.from("chat.messages.minesClosed", matchingConfig.get().getConfiguration()).colorize().toString());
                        event.setCancelled(true);
                        return;
                    }
                }

                // Richness
                if (minedMaterial.get().canBeRich()) {
                    //Draw for richness
                    if (!MetaUtils.isRich(event.getBlock())) {
                        int drawnRichness = minedMaterial.get().getDrawnRichness();
                        if (drawnRichness > 0) {
                            MetaUtils.setRichness(event.getBlock(), drawnRichness);
                            Bukkit.getPluginManager().callEvent(new DepositDiscoveredEvent(event.getBlock(), event.getPlayer(), minedMaterial.get(), drawnRichness + 1));
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
                    long cooldown = minedMaterial.get().getCooldown();
                    new ReplacementTask(event.getBlock().getLocation(), event.getBlock().getType(), cooldown)
                                .withMaterialIdentifier(minedMaterial.get().getMaterialIdentifier());

                    // Bamboo or Sugar cane?
                    if (BlockUtils.isStackable(event.getBlock())) {
                        List<Block> dependants = BlockUtils.getConnectedBlocks(event.getBlock());
                        dependants.forEach(d -> {
                            Bukkit.getPluginManager().callEvent(new MaterialMinedEvent(minedMaterial.get(), event.getBlock(), false, event.getPlayer()));
                            breakBlock(event, minedMaterial.get(), event.getPlayer().getInventory().getItemInMainHand());
                        });
                        WorldUtils.replaceInSequence(cooldown, event.getBlock().getType(), dependants);
                    }
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

    private Optional<RegionConfiguration> getRegionConfiguration(Block block) {
        return plugin.getMaterialManager().getMineableMaterialOverrides().stream().filter(rc -> {
            boolean worldMatches = rc.getWorld() == block.getLocation().getWorld();
            boolean idMatches = false;

            Optional<MiningRegion> topRegion = plugin.getRegionProvider().getRegion(block);
            if (topRegion.isPresent() && topRegion.get().getRegionId().equals(rc.getRegionId())) {
                idMatches = true;
            }

            return worldMatches && idMatches;
        }).findFirst();
    }

    private void breakBlock(BlockBreakEvent event, MineableMaterial mineableMaterial, ItemStack tool) {
        boolean autoPickup = OuroborosMines.INSTANCE.getConfig().getBoolean("autoPickup", false) || event.getPlayer().hasPermission(Permissions.FEATURE_AUTO_PICKUP.permission);

        // Check for drop group
        if (mineableMaterial.getDropGroup().isPresent()) {
            breakNaturally(event, mineableMaterial, !mineableMaterial.getDropGroup().get().isOverriding(), autoPickup);
            mineableMaterial.getDropGroup().get().drop(event.getPlayer(), event.getBlock().getLocation());
        // No drop-group assigned
        } else {
            breakNaturally(event, mineableMaterial, true, autoPickup);
        }

        decreaseToolDurability(event.getPlayer(), tool);
        updateStats(event.getPlayer(), mineableMaterial.getMaterial());
    }

    private void breakNaturally(BlockBreakEvent event, MineableMaterial mineableMaterial, boolean dropNaturalItems, boolean autoPickup) {
        if (dropNaturalItems) {
            if (autoPickup) {
                event.setDropItems(false);
                Collection<ItemStack> drops = getCustomDefaultDrops(event.getPlayer().getInventory().getItemInMainHand(), mineableMaterial, event.getBlock())
                                              .orElse(event.getBlock().getDrops());

                Player player = event.getPlayer();
                Location blockLocation = event.getBlock().getLocation();
                Map<Integer, ItemStack> overflow = player.getInventory().addItem(drops.stream().toArray(ItemStack[]::new));
                overflow.values().forEach(i -> blockLocation.getWorld().dropItem(blockLocation, i));
            } else {
                Optional<Collection<ItemStack>> customDefaultDrops = getCustomDefaultDrops(event.getPlayer().getInventory().getItemInMainHand(), mineableMaterial, event.getBlock());
                if (!customDefaultDrops.isPresent()) {
                    event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
                } else {
                    event.setDropItems(false);

                    Location blockLocation = event.getBlock().getLocation();

                    removeCustomBlockClaim(event.getBlock());
                    blockLocation.getBlock().setType(Material.AIR);

                    customDefaultDrops.get().forEach(
                            itemStack -> {
                                blockLocation.getWorld().dropItemNaturally(blockLocation, itemStack);
                            }
                    );
                }
            }
        }
    }

    private Optional<Collection<ItemStack>> getCustomDefaultDrops(ItemStack tool, MineableMaterial mineableMaterial, Block block) {
        DefaultDropsCheckEvent event = new DefaultDropsCheckEvent(tool, mineableMaterial, block);
        Bukkit.getPluginManager().callEvent(event);
        return event.hasCustomDefaultDrops() ? Optional.of(event.getDrops()) : Optional.empty();
    }

    private void updateStats(Player player, Material material) {
        player.incrementStatistic(Statistic.MINE_BLOCK, material);
    }

    private void decreaseToolDurability(Player player, ItemStack tool) {
        Optional<ItemStack> miningTool = Optional.ofNullable(tool);
        miningTool.ifPresent( itemStack -> {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof Damageable && tool.getType().getMaxDurability() > 0 && !meta.isUnbreakable()) {
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

    private boolean performRegionCheck(Player player, MiningRegion miningRegion, Block block) {
        RegionCheckEvent regionCheckEvent = new RegionCheckEvent(player, miningRegion, block);
        Bukkit.getPluginManager().callEvent(regionCheckEvent);
        return !regionCheckEvent.isCancelled();
    }

    private void removeCustomBlockClaim(Block block) {
        Bukkit.getServer().getPluginManager().callEvent(new RemoveCustomBlockEvent(block));
    }

}
