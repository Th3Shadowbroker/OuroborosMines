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
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.regions.AbstractFlag;
import org.bukkit.World;

import java.util.Optional;


public class WorldGuardFlag extends AbstractFlag {

    public WorldGuardFlag(String flagName) {
        super(flagName, new StateFlag(flagName, OuroborosMines.INSTANCE.getConfig().getBoolean("default")));
    }

    @Override
    public boolean isActive(World world, String regionId) {
        Optional<RegionManager> regionManager = Optional.ofNullable(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)));

        if (regionManager.isPresent()) {
            Optional<ProtectedRegion> region = Optional.ofNullable(regionManager.get().getRegion(regionId));
            if (region.isPresent()) {
                Optional<StateFlag> flag = Optional.ofNullable(pluginFlag != null && pluginFlag.getClass().isAssignableFrom(AbstractFlag.class) ? (StateFlag) pluginFlag : null);
                if (flag.isPresent()) {
                    return region.get().getFlag(flag.get()) == StateFlag.State.ALLOW;
                }
            }
        }

        return false;
    }

}
