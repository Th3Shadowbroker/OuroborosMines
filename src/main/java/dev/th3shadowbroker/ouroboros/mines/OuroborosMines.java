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

package dev.th3shadowbroker.ouroboros.mines;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import dev.th3shadowbroker.ouroboros.mines.commands.OmCommand;
import dev.th3shadowbroker.ouroboros.mines.exceptions.InvalidMineMaterialException;
import dev.th3shadowbroker.ouroboros.mines.listeners.BlockBreakListener;
import dev.th3shadowbroker.ouroboros.mines.listeners.DepositDiscoveryListener;
import dev.th3shadowbroker.ouroboros.mines.listeners.ExperienceListener;
import dev.th3shadowbroker.ouroboros.mines.thirdparty.JobsRebornSupport;
import dev.th3shadowbroker.ouroboros.mines.thirdparty.PlaceholderAPISupport;
import dev.th3shadowbroker.ouroboros.mines.thirdparty.QuestsSupport;
import dev.th3shadowbroker.ouroboros.mines.util.*;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.th3shadowbroker.ouroboros.update.comparison.Comparator;
import org.th3shadowbroker.ouroboros.update.spiget.SpigetUpdater;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OuroborosMines extends JavaPlugin {

    public static OuroborosMines INSTANCE;

    public static String PREFIX;

    public static StateFlag FLAG;

    private MaterialManager materialManager;

    private EffectManager effectManager;

    private TaskManager taskManager;

    private AnnouncementManager announcementManager;

    private boolean worldGuardFound = false;

    @Override
    public void onLoad() {
        //Check for world-guard and disable if not installed
        worldGuardFound = worldGuardIsInstalled();
        if (!worldGuardFound) { return; }

        //Static stuff
        INSTANCE = this;
        FLAG = new StateFlag("ouroboros-mine", false);

        //Internal stuff
        materialManager = new MaterialManager();
        effectManager = new EffectManager();
        taskManager = new TaskManager();

        //Config
        getLogger().info("Loading configuration...");
        saveDefaultConfig();
        updateConfig();
        PREFIX = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chat.prefix", "&9[OM]") + "&r ");

        //Register flag
        Optional<Plugin> worldGuard = Optional.ofNullable(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"));
        if (worldGuard.isPresent() && worldGuard.get() instanceof WorldGuardPlugin) {
            ((WorldGuardPlugin) worldGuard.get()).getFlagRegistry().register(FLAG);
        } else {
            getLogger().severe("WorldGuard not found. If you are using WorldGuard 7 you should use the most recent version.");
        }
    }

    @Override
    public void onEnable() {
        if (!worldGuardFound) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadMineMaterials();
        loadEffects();

        getServer().getPluginManager().registerEvents( new BlockBreakListener(), this );
        getServer().getPluginManager().registerEvents( new DepositDiscoveryListener(), this );
        getServer().getPluginManager().registerEvents( new ExperienceListener(), this );

        announcementManager = new AnnouncementManager();
        announcementManager.createTasks();

        getCommand("om").setExecutor(new OmCommand());

        checkForSupportedPlugins();

        new MetricsLite(this);
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (!worldGuardFound) { return; }
        getTaskManager().flush();
    }

    private boolean worldGuardIsInstalled() {
        Optional<Plugin> worldGuard = Optional.ofNullable(getServer().getPluginManager().getPlugin("WorldGuard"));

        if (!worldGuard.isPresent()) {
            getLogger().severe(String.format("WorldGuard is not installed. Disabling %s...", getName()));
            return false;
        }

        return true;
    }

    private void loadMineMaterials() {
        ConfigurationSection parentSection = getConfig().getConfigurationSection("materials");
        for (String childSectionKey : parentSection.getKeys(false))
        {
            try
            {
                materialManager.register(MineableMaterial.fromSection(parentSection.getConfigurationSection(childSectionKey)));
            } catch (InvalidMineMaterialException e) {
                getLogger().severe(e.getMessage());
                e.printStackTrace();
            }
        }

        //Load region specific rules
        if (!RegionConfiguration.REGION_CONFIG_DIR.exists()) RegionConfiguration.REGION_CONFIG_DIR.mkdirs();
        materialManager.loadRegionConfigurations();
    }

    private void loadEffects() {
        Optional<ConfigurationSection> parentSection = Optional.ofNullable(getConfig().getConfigurationSection("effects"));
        if (parentSection.isPresent()) {
            for (String childSectionKey : parentSection.get().getKeys(false))
            {
                Optional<ConfigurationSection> childSection = Optional.ofNullable(parentSection.get().getConfigurationSection(childSectionKey));
                if (childSection.isPresent()) {
                    try
                    {
                        effectManager.register(TriggeredEffect.fromSection(childSection.get()));
                    } catch (Exception e) {
                        getLogger().severe("Unable to parse effect of type " + childSectionKey + ": " + e.getMessage());
                    }
                } else {
                    getLogger().severe("Expected " + childSectionKey + " to be a section!");
                }
            }
        } else {
            getLogger().info("No effects defined");
        }
    }

    private void updateConfig() {
         Optional<InputStream> defaultConfigInput = Optional.ofNullable(getResource("config.yml"));
         if (defaultConfigInput.isPresent()) {
             FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration( new InputStreamReader( defaultConfigInput.get() ));

             //Patch messages into existing config
             if (!getConfig().isSet("chat.messages.depositSizes")) {
                getConfig().createSection("chat.messages.depositSizes", defaultConfig.getConfigurationSection("chat.messages.depositSizes").getValues(false));
                saveConfig();
                reloadConfig();
                getLogger().info("Configuration patch for chat.messages.depositSizes applied!");
             }

             //Patch worldNotFound message into config
             if (!getConfig().isSet("chat.messages.worldNotFound")) {
                 getConfig().set("chat.messages.worldNotFound", defaultConfig.getString("chat.messages.worldNotFound"));
                 saveConfig();
                 reloadConfig();
                 getLogger().info("Configuration patch for chat.messages.worldNotFound applied!");
             }

             //Patch experience settings into config
             if (!getConfig().isSet("experience.spawnOrbs")) {
                 getConfig().set("experience.spawnOrbs", true);
                 saveConfig();
                 reloadConfig();
                 getLogger().info("Configuration patch for experience.spawnOrbs applied!");
             }

             //Patch messages that are part of the clan-update
             List<String> pathsToCopy = Arrays.asList(
                     "chat.messages.announcements",
                     "chat.messages.minesClosed",
                     "chat.messages.error",
                     "autoPickup",
                     "openingHours",
                     "timezone",
                     "placeholders"
             );
             pathsToCopy.forEach(path -> {
                 if (!getConfig().isSet(path)) {
                     getLogger().info("Configuration patch for " + path + " applied!");
                     getConfig().set(path, defaultConfig.get(path));

                     saveConfig();
                     reloadConfig();
                 }
             });

         } else {
             getLogger().severe("Unable to load default-configuration to patch existing configuration!");
         }
    }

    private void checkForSupportedPlugins() {
        boolean questsInstalled = getServer().getPluginManager().isPluginEnabled(QuestsSupport.PLUGIN_NAME);
        if (questsInstalled) {
            getLogger().info("Quests support is enabled!");
            new QuestsSupport();
        }

        boolean jobsRebornInstalled = getServer().getPluginManager().isPluginEnabled(JobsRebornSupport.PLUGIN_NAME);
        if (jobsRebornInstalled) {
            getLogger().info("Jobs support is enabled!");
            new JobsRebornSupport();
        }

        boolean placeholderApiInstalled = getServer().getPluginManager().isPluginEnabled(PlaceholderAPISupport.PLUGIN_NAME);
        if (placeholderApiInstalled) {
            getLogger().info("PlaceholderAPI support is enabled!");
            boolean success  = new PlaceholderAPISupport().register();
            if (!success) {
                getLogger().warning("Unable to register placeholders. Skipping!");
            }
        }
    }

    private void checkForUpdates() {
        SpigetUpdater spigetUpdater = new SpigetUpdater(Comparator.SEMANTIC, this,72325);
        spigetUpdater.checkForUpdate();
    }

    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }
    
}
