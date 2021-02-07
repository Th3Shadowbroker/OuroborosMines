/*
 * Copyright 2021 Jens Fischer
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

package dev.th3shadowbroker.ouroboros.mines.regions.providers.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import dev.th3shadowbroker.ouroboros.mines.regions.MiningRegion;
import dev.th3shadowbroker.ouroboros.mines.regions.ProviderDescription;
import dev.th3shadowbroker.ouroboros.mines.regions.RegionProvider;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

@ProviderDescription(
        providerName = "WorldGuard"
)
public class WorldGuardProvider extends RegionProvider {

    public WorldGuardProvider() {
        super(new WorldGuardFlag("ouroboros-mine"));
    }

    @Override
    public Optional<MiningRegion> getGlobalRegion(Location location) {
        Optional<World> world = Optional.ofNullable(location.getWorld());

        if (world.isPresent()) {
            Optional<RegionManager> regionManager = Optional.ofNullable(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world.get())));

            if (regionManager.isPresent()) {
                if (regionManager.get().hasRegion("__global__")) {
                    ProtectedRegion region = regionManager.get().getRegion("__global__");
                    return Optional.ofNullable(region != null && region.getType() == RegionType.GLOBAL ? toMiningRegion(region) : null);
                } else {
                    GlobalProtectedRegion region = new GlobalProtectedRegion("__global__");
                    regionManager.get().addRegion(region);
                    return Optional.of(toMiningRegion(region));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<MiningRegion> getRegion(Location location) {
        Optional<World> world = Optional.ofNullable(location.getWorld());
        if (world.isPresent()) {
            Optional<RegionManager> regionManager = Optional.ofNullable(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world.get())));
            if (regionManager.isPresent()) {
                ApplicableRegionSet regionSet = regionManager.get().getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());

                ProtectedRegion region = null;

                for (ProtectedRegion protectedRegion : regionSet.getRegions()) {
                    if(region == null || protectedRegion.getPriority() > region.getPriority()) {
                        region = protectedRegion;
                    }
                }

                return Optional.ofNullable(region).map(this::toMiningRegion);
            }
        }

        return Optional.empty();
    }

    private MiningRegion toMiningRegion(ProtectedRegion protectedRegion) {
        return new MiningRegion(protectedRegion.getId(), protectedRegion.getFlag((StateFlag) flag.getPluginFlag()) == StateFlag.State.ALLOW);
    }

    @Override
    public Optional<MiningRegion> getRegion(String regionId, World world) {
        Optional<RegionManager> regionManager = Optional.ofNullable(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)));
        return regionManager.flatMap(manager -> Optional.ofNullable(manager.getRegion(regionId)).map(r -> new MiningRegion(r.getId(), r.getFlag((StateFlag) flag.getPluginFlag()) == StateFlag.State.ALLOW)));
    }

    @Override
    public void onLoad() {
        WorldGuard.getInstance().getFlagRegistry().register((StateFlag) flag.getPluginFlag());
    }

}
