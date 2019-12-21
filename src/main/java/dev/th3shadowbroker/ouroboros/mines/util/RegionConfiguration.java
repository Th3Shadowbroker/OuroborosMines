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
import dev.th3shadowbroker.ouroboros.mines.exceptions.InvalidMineMaterialException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class RegionConfiguration {

    private final String regionId;

    private final World world;

    private final File configurationFile;

    private final FileConfiguration configuration;

    private final List<MineableMaterial> materialList;

    private final boolean inheritDefaults;

    private static final OuroborosMines plugin = OuroborosMines.INSTANCE;

    public static final File REGION_CONFIG_DIR = new File(plugin.getDataFolder() + "/regions");

    public RegionConfiguration(String regionId, String worldName) {
        this.regionId = regionId;
        this.world = Bukkit.getWorld(worldName);
        this.configurationFile = new File(REGION_CONFIG_DIR, String.format("%s/%s.yml", worldName, regionId));
        this.configuration = YamlConfiguration.loadConfiguration(configurationFile);
        this.materialList = new ArrayList<>();
        this.inheritDefaults = configuration.getBoolean("inherit", false);
        this.loadMaterialOverrides();
    }

    private void loadMaterialOverrides() {
        if (configurationFile.exists()) {
            Optional<ConfigurationSection> materialSection = Optional.ofNullable(configuration.getConfigurationSection("materials"));
            materialSection.ifPresent(configurationSection -> {
                configurationSection.getKeys(false)
                        .forEach(key -> {
                            Optional<ConfigurationSection> materialDefinition = Optional.ofNullable(materialSection.get().getConfigurationSection(key));
                            materialDefinition.ifPresent(materialDefinitionSection -> {
                                try {
                                    materialList.add(MineableMaterial.fromSection(materialDefinitionSection));
                                } catch (InvalidMineMaterialException e) {
                                    plugin.getLogger().severe(e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                });
            });
        } else {
            configuration.set("inherit", false);
            Optional<ConfigurationSection> materialSection = Optional.ofNullable(plugin.getConfig().getConfigurationSection("materials"));
            materialSection.ifPresent(configurationSection -> configuration.createSection(configurationSection.getName(), configurationSection.getValues(true)));
            save();
        }
    }

    public String getRegionId() {
        return regionId;
    }

    public File getConfigurationFile() {
        return configurationFile;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public List<MineableMaterial> getMaterialList() {
        return materialList;
    }

    public World getWorld() {
        return world;
    }

    public Optional<MineableMaterial> getMaterialProperties(Material material) {
        return materialList.stream().filter(mineableMaterial -> mineableMaterial.getMaterial() == material).findFirst();
    }

    public boolean isInheritingDefaults() {
        return inheritDefaults;
    }

    private void save() {
        try {
            configuration.save(configurationFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save region-configuration to " + configurationFile.getName());
            e.printStackTrace();
        }
    }

    public static boolean configExists(String regionId, String worldName) {
        return new File(REGION_CONFIG_DIR, String.format("%s/%s.yml", worldName, regionId)).exists();
    }

    public static List<RegionConfiguration> getConfigurationsInDirectory(File directory) {
        List<RegionConfiguration> regionConfigurations = new ArrayList<>();
        //Ensure requirements are given
        if (directory.exists() && directory.isDirectory()) {

            //Check files
            for (File worldDirectory : directory.listFiles()) {

                //World directory is a directory
                if (worldDirectory.isDirectory()) {

                    //Try to resolve the world
                    Optional<World> world = Optional.ofNullable(Bukkit.getWorld(worldDirectory.getName()));
                    if (world.isPresent()) {
                        FilenameFilter filter = new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".yml");
                            }
                        };

                        for (File file : worldDirectory.listFiles(filter)) {
                            String regionId = file.getName().replace(".yml","");
                            regionConfigurations.add(new RegionConfiguration(regionId, world.get().getName()));
                        }
                    } else {
                        plugin.getLogger().info("The world " + worldDirectory.getName() + " is unknown. You may remove it's directory from the regions directory of OM.");
                    }

                }

            }
        }
        return regionConfigurations;
    }

}
