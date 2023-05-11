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

package dev.th3shadowbroker.ouroboros.mines.util;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.thirdparty.itemsadder.PlaceCustomBlockEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventException;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class ReplacementTask implements Runnable {

    private final BukkitTask task;

    private final Material minedMaterial;

    private final Location location;

    private final BlockData blockData;

    private final boolean isAttachableBlock;

    private MaterialIdentifier materialIdentifier;

    private final Map<String, Object> properties;

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    public ReplacementTask(Location blockLocation, Material material, long cooldownSeconds, Map<String, Object> properties) {
        this.location = blockLocation;
        this.minedMaterial = material;
        this.task = plugin.getServer().getScheduler().runTaskLater(plugin, this, cooldownSeconds);
        this.plugin.getTaskManager().register(this);
        this.blockData = blockLocation.getBlock().getState().getBlockData();
        this.isAttachableBlock = BlockUtils.isAttachable(location.getBlock());
        this.properties = properties;
    }

    private ReplacementTask(Location blockLocation, Material material, BlockData blockData, long cooldownSeconds, Map<String, Object> properties) {
        this.location = blockLocation;
        this.minedMaterial = material;
        this.task = plugin.getServer().getScheduler().runTaskLater(plugin, this, cooldownSeconds);
        this.plugin.getTaskManager().register(this);
        this.blockData = blockData;
        this.isAttachableBlock = BlockUtils.isAttachable(location.getBlock());
        this.properties = properties;
    }

    @Override
    public void run() {
        // Stable to place block?
        if (!BlockUtils.isStableBlock(location.getBlock())) {
            if (!task.isCancelled()) {
                retryLater();
            }
            return;
        }


        if (!plugin.getConfig().getBoolean("retryDirectionals", true) || (!isAttachableBlock || BlockUtils.isStableToAttach(location.getBlock()))) {
            Block relative = location.getBlock().getRelative(BlockFace.DOWN);
            if (BlockUtils.isStackable(minedMaterial) && relative.getType().isAir()) {
                if (!getTask().isCancelled()) {
                    retryLater();
                }
            } else  {
                var identifier = Optional.ofNullable(materialIdentifier);
                if (identifier.isEmpty() || identifier.get().isInDefaultNamespace()) {
                    location.getBlock().setType(minedMaterial);

                    if (properties != null && properties.containsKey("resetAge")) {
                        var resetAge = (boolean) properties.get("resetAge");
                        if (resetAge && blockData instanceof Ageable ageable) ageable.setAge(0);
                    }
                    location.getBlock().setBlockData(blockData);
                } else {
                    // Call has to be forced to guarantee execution after /stop command
                    PlaceCustomBlockEvent pcbe = new PlaceCustomBlockEvent(location, identifier.get());
                    Arrays.stream(PlaceCustomBlockEvent.getHandlerList().getRegisteredListeners()).forEach(rl -> {
                        try {
                            rl.callEvent(pcbe);
                        } catch (EventException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } else {
            retryLater();
        }

        if (!getTask().isCancelled()) {
            plugin.getTaskManager().unregister(this);
        }
    }

    public void retryLater() {
        new ReplacementTask(location, minedMaterial, blockData, plugin.getConfig().getInt("retryInterval", 5), properties);
        plugin.getTaskManager().unregister(this);
    }

    public BukkitTask getTask() {
        return task;
    }

    public Material getMinedMaterial() {
        return minedMaterial;
    }

    public boolean isAttachableBlock() {
        return isAttachableBlock;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public ReplacementTask withMaterialIdentifier(MaterialIdentifier materialIdentifier) {
        this.materialIdentifier = materialIdentifier;
        return this;
    }

}
