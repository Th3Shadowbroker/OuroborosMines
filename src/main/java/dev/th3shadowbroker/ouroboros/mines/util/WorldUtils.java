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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Optional;

public class WorldUtils {

    public static Optional<ApplicableRegionSet> getBlockRegions(Block block) {
        Optional<RegionManager> regionManager = Optional.ofNullable( WorldGuard.getInstance().getPlatform().getRegionContainer().get( BukkitAdapter.adapt(block.getWorld()) ) );
        return regionManager.map(manager -> manager.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation())));
    }

    public static Optional<ApplicableRegionSet> getPlayerRegions(Player player) {
        Optional<RegionManager> regionManager = Optional.ofNullable( WorldGuard.getInstance().getPlatform().getRegionContainer().get( BukkitAdapter.adapt(player.getWorld()) ) );
        return regionManager.map(manager -> manager.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint()));
    }

    public static boolean isAccessible(Block block) {
        BlockFace[] accessibleFaces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        for (BlockFace face : accessibleFaces)
        {
            Material relativeMaterial = block.getRelative(face).getType();
            if ( relativeMaterial == Material.AIR || relativeMaterial == Material.CAVE_AIR || relativeMaterial == Material.VOID_AIR ) return true;
        }
        return false;
    }

    public static boolean compareLocations(Location a, Location b) {
        return a.getBlockX() == b.getBlockX() &&
               a.getBlockY() == b.getBlockY() &&
               a.getBlockZ() == b.getBlockZ() &&
               a.getWorld()  == b.getWorld();
    }

}
