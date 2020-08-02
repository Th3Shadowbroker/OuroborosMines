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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialManager {

    private final List<MineableMaterial> minableMaterials = new ArrayList<>();

    private final List<RegionConfiguration> mineableMaterialOverrides = new ArrayList<>();

    public void loadRegionConfigurations() {
        mineableMaterialOverrides.addAll(RegionConfiguration.getConfigurationsInDirectory(RegionConfiguration.REGION_CONFIG_DIR));
        OuroborosMines.INSTANCE.getLogger().info("Loaded " + mineableMaterialOverrides.size() + " region-specific configurations");
    }

    public void reloadRegionConfigurations() {
        mineableMaterialOverrides.clear();
        loadRegionConfigurations();
    }

    public void register(MineableMaterial mineableMaterial) {
        minableMaterials.add(mineableMaterial);
    }

    public Optional<MineableMaterial> getMaterialProperties(Material material, ProtectedRegion region, World regionWorld) {
         Optional<RegionConfiguration> regionConfiguration = mineableMaterialOverrides.stream()
                                                             .filter(rc -> rc.getRegionId().equals(region.getId()))
                                                             .filter(rc -> rc.getWorld().getName().equals(regionWorld.getName())).findFirst();

         //Check for region specific settings
         if (regionConfiguration.isPresent()) {
             Optional<MineableMaterial> regionMineableMaterial = regionConfiguration.get().getMaterialList().stream().filter(mineableMaterial -> mineableMaterial.getMaterial() == material).findFirst();
             if (regionMineableMaterial.isPresent()) return regionMineableMaterial;
             if (!regionConfiguration.get().isInheritingDefaults()) return Optional.empty();
         }

         return minableMaterials.stream().filter(mineableMaterial -> mineableMaterial.getMaterial() == material).findFirst();
    }

    public List<MineableMaterial> getMinableMaterials() {
        return minableMaterials;
    }

    public List<RegionConfiguration> getMineableMaterialOverrides() {
        return mineableMaterialOverrides;
    }
}
