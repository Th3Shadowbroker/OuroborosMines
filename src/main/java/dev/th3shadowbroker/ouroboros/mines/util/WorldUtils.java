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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class WorldUtils {

    private static final List<Material> stackableMaterials = List.of(
      Material.BAMBOO,
      Material.SUGAR_CANE,
      Material.CACTUS,
      Material.KELP
    );

    public static List<Material> getStackableMaterials() {
        return Collections.unmodifiableList(stackableMaterials);
    }

    public static List<Block> getBlocksAbove(Block block) {
        List<Block> blocks = new ArrayList<>();
        Location location = block.getLocation();

        while (location.add(0, 1, 0).getBlock().getType() == block.getType()) {
            blocks.add(location.getBlock());
        }

        return Collections.unmodifiableList(blocks);
    }

    public static boolean isDirectional(BlockData blockData) {
        return blockData instanceof Directional;
    }

    public static boolean canBeAttached(Location location, BlockData blockData) {
        Directional attachable = ((Directional) blockData);
        Material anchorMaterial = location.getBlock().getRelative(attachable.getFacing()).getType();

        return anchorMaterial.isSolid() && !anchorMaterial.isAir();
    }

    public static void replaceInSequence(long offset, Material material, List<Block> blocks) {
        AtomicLong currentOffset = new AtomicLong(offset);
        for (Block block : blocks) {
            new ReplacementTask(block.getLocation(), material, currentOffset.incrementAndGet());
        }
    }

    public static boolean compareLocations(Location a, Location b) {
        return a.getBlockX() == b.getBlockX() &&
               a.getBlockY() == b.getBlockY() &&
               a.getBlockZ() == b.getBlockZ() &&
               a.getWorld()  == b.getWorld();
    }

}
