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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitTask;

public class ReplacementTask implements Runnable {

    private final BukkitTask task;

    private final Material minedMaterial;

    private final Location location;

    private final BlockData blockData;

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    public ReplacementTask(Location blockLocation, Material material, long cooldownSeconds) {
        this.location = blockLocation;
        this.minedMaterial = material;
        this.task = plugin.getServer().getScheduler().runTaskLater(plugin, this, cooldownSeconds);
        this.plugin.getTaskManager().register(this);
        this.blockData = blockLocation.getBlock().getBlockData();
        //plugin.getLogger().info(String.format("Scheduled restoration of %s %s %s in %s as %s", location.getX(), location.getY(), location.getZ(), cooldownSeconds, minedMaterial.name()));
    }

    @Override
    public void run() {
        location.getBlock().setType(minedMaterial);
        location.getBlock().setBlockData(blockData);
        if (!getTask().isCancelled()) plugin.getTaskManager().unregister(this);
    }

    public BukkitTask getTask() {
        return task;
    }

    public Material getMinedMaterial() {
        return minedMaterial;
    }

    public Block getBlock() {
        return location.getBlock();
    }
}
