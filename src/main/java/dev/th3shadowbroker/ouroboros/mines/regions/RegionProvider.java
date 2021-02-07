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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.Optional;

public abstract class RegionProvider {

    protected final OuroborosMines plugin;

    protected final AbstractFlag flag;

    public RegionProvider(AbstractFlag flag) {
        this.plugin = OuroborosMines.INSTANCE;
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

    public abstract void onLoad();

    public AbstractFlag getFlag() {
        return flag;
    }

    public String getProviderName() {
        return getProviderDescription().providerName();
    }

    public ProviderDescription getProviderDescription() {
        return getClass().getAnnotation(ProviderDescription.class);
    }

    @SafeVarargs
    public static Optional<RegionProvider> getProvider(Class<? extends RegionProvider>... providers) {
        for (Class<? extends RegionProvider> provider : providers) {
            Optional<ProviderDescription> description = getProviderDescription(provider);
            if (description.isPresent()) {
                Optional<Plugin> plugin = Optional.ofNullable(Bukkit.getServer().getPluginManager().getPlugin(description.get().providerName()));
                if (plugin.isPresent()) {
                    try {
                        Constructor<?> providerConstructor = provider.getDeclaredConstructor();
                        return Optional.of((RegionProvider) providerConstructor.newInstance());
                    } catch (ReflectiveOperationException ex) {
                        OuroborosMines.INSTANCE.getLogger().severe("Unable to create a region provider for " + plugin.get().getName());
                        ex.printStackTrace();
                    }
                }
            } else {
                OuroborosMines.INSTANCE.getLogger().warning(String.format("The class %s does not contain a ProviderDescription annotation and will be ignored!", provider.getName()));
            }
        }
        return Optional.empty();
    }

    public static Optional<ProviderDescription> getProviderDescription(Class<? extends RegionProvider> provider) {
        return Optional.ofNullable(provider.getAnnotation(ProviderDescription.class));
    }

}
