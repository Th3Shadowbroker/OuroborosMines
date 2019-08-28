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
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.util.MineableMaterial;
import dev.th3shadowbroker.ouroboros.mines.util.ReplacementTask;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<MineableMaterial> minedMaterial = plugin.getMaterialManager().getMaterialProperties(event.getBlock().getType());
        Optional<ApplicableRegionSet> blockRegions = getBlockRegions(event.getBlock());

        if ( minedMaterial.isPresent() && blockRegions.isPresent() && blockRegions.get().testState(null, OuroborosMines.FLAG)) {

            if (!plugin.getTaskManager().hasPendingReplacementTask(event.getBlock())) {
                new ReplacementTask(event.getBlock().getLocation(), event.getBlock().getType(), minedMaterial.get().getCooldown());
            }

            event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
            event.getBlock().setType(minedMaterial.get().getReplacement());

            event.setCancelled(true);
        }
    }

    private Optional<ApplicableRegionSet> getBlockRegions(Block block) {
        Optional<RegionManager> regionManager = Optional.ofNullable( WorldGuard.getInstance().getPlatform().getRegionContainer().get( BukkitAdapter.adapt(block.getWorld()) ) );
        return regionManager.map(manager -> manager.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation())));
    }

}
