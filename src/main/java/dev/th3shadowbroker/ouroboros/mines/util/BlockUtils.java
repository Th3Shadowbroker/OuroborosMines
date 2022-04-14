/*
 * Copyright 2022 Jens Fischer
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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Attachable;
import org.bukkit.block.data.Directional;

import java.util.*;

public class BlockUtils {

    private static final Map<Material, List<Material>> dependantMaterials = new HashMap<>(){{
        put(Material.BAMBOO, List.of(Material.BAMBOO));
        put(Material.SUGAR_CANE, List.of(Material.SUGAR_CANE));
        put(Material.CACTUS, List.of(Material.CACTUS));
        put(Material.KELP_PLANT, List.of(Material.KELP_PLANT, Material.KELP));
    }};

    public static boolean isStackable(Block block) {
        return isStackable(block.getType());
    }

    public static boolean isStackable(Material material) {
        return dependantMaterials.containsKey(material);
    }

    public static boolean isAttachable(Block block) {
        return block.getState().getBlockData() instanceof Attachable &&
               block.getState().getBlockData() instanceof Directional;
    }

    public static boolean isStableToAttach(Block block) {
        Directional directional = (Directional) block.getState().getBlockData();
        Block relative = block.getRelative(directional.getFacing());
        return relative.getType().isSolid() && !relative.getType().isAir();
    }

    public static boolean isStableBlock(Block block) {
        Block current = block;
        while (isStableMaterial((current = current.getRelative(BlockFace.DOWN)).getType())) {
            if (!isStackable(current)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStableMaterial(Material material) {
        if (material.isAir()) return false;
        if (material == Material.WATER) return false;
        return true;
    }

    public static List<Block> getConnectedBlocks(Block block) {
        List<Block> blocks = new ArrayList<>();
        blocks.addAll(getAttachedBlocks(block));
        blocks.addAll(getStackedBlocks(block));
        return blocks;
    }

    private static List<Block> getAttachedBlocks(Block block) {
        final List<Block> blocks = new ArrayList<>();

        Arrays.stream(BlockFace.values())
              .forEach(blockFace -> {
                  Block relative = block.getRelative(blockFace);
                  if (isAttachable(relative)) {
                      Directional directional = (Directional) relative.getState().getBlockData();
                      if (relative.getRelative(directional.getFacing()).equals(block)) {
                          blocks.add(relative);
                      }
                  }
              });

        return blocks;
    }

    private static List<Block> getStackedBlocks(Block block) {
        final List<Block> blocks = new ArrayList<>();
        final Material sourceBlockMaterial = block.getType();
        final List<Material> stackableMaterials = Optional.ofNullable(dependantMaterials.get(sourceBlockMaterial))
                                                          .orElse(Collections.emptyList());

        if (stackableMaterials.size() == 0) return Collections.emptyList();

        Block current = block;
        while (stackableMaterials.contains((current = current.getRelative(BlockFace.UP)).getType())) {
            blocks.add(current);
        }

        return blocks;
    }

}
