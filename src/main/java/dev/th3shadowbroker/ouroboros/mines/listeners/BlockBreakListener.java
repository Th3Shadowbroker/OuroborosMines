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
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.util.MetaUtils;
import dev.th3shadowbroker.ouroboros.mines.util.MineableMaterial;
import dev.th3shadowbroker.ouroboros.mines.util.ReplacementTask;
import dev.th3shadowbroker.ouroboros.mines.util.WorldUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<ApplicableRegionSet> blockRegions = WorldUtils.getBlockRegions(event.getBlock());

        if ( blockRegions.isPresent() && blockRegions.get().testState(null, OuroborosMines.FLAG)) {
            Optional<MineableMaterial> minedMaterial = plugin.getMaterialManager().getMaterialProperties(event.getBlock().getType(), WorldUtils.getTopRegion(blockRegions.get()).get(), BukkitAdapter.adapt(event.getBlock().getWorld()));
            if (minedMaterial.isPresent()) {

                if (minedMaterial.get().canBeRich()) {
                    //Draw for richness
                    if (!MetaUtils.isRich(event.getBlock())) {
                        int drawnRichness = minedMaterial.get().getDrawnRichness();
                        if (drawnRichness > 0) {
                            MetaUtils.setRichness(event.getBlock(), drawnRichness);
                        }
                    }

                    //If rich
                    if (MetaUtils.isRich(event.getBlock())) {
                        event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
                        event.getBlock().setType(minedMaterial.get().getMaterial());
                        MetaUtils.decreaseRichness(event.getBlock());
                        event.setCancelled(true);
                        return;
                    }
                }

                //If richness was never set or hit 0
                if (!plugin.getTaskManager().hasPendingReplacementTask(event.getBlock())) {
                    new ReplacementTask(event.getBlock().getLocation(), event.getBlock().getType(), minedMaterial.get().getCooldown());
                }

                event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
                event.getBlock().setType(minedMaterial.get().getReplacement());
            }

            event.setCancelled(true);
        }
    }

}
