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

package dev.th3shadowbroker.ouroboros.mines.regions;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Optional;

public abstract class RegionProvider {

    protected final OuroborosMines plugin;

    protected final String providerName;

    protected final AbstractFlag flag;

    public RegionProvider(String providerName, AbstractFlag flag) {
        this.plugin = OuroborosMines.INSTANCE;
        this.providerName = providerName;
        this.flag = flag;
    }

    public abstract Optional<MiningRegion> getGlobalRegion(Location location);

    public Optional<MiningRegion> getGlobalRegion(Block block) {
        return getGlobalRegion(block.getLocation());
    }

    public abstract Optional<MiningRegion> getRegion(Location location);

    public abstract Optional<MiningRegion> getRegion(String regionId, World world);

    public Optional<MiningRegion> getRegion(Block block) {
        return getRegion(block.getLocation());
    }

    public abstract boolean isAvailable();

    public abstract void onLoad();

    public AbstractFlag getFlag() {
        return flag;
    }

    public String getProviderName() {
        return providerName;
    }

    public static Optional<RegionProvider> getProvider(RegionProvider... providers) {
        for (RegionProvider provider : providers) {
            if (provider.isAvailable()) return Optional.of(provider);
        }
        return Optional.empty();
    }

}
