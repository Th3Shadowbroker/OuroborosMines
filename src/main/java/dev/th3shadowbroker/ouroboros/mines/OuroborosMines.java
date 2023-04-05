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

import dev.th3shadowbroker.ouroboros.mines.commands.OmCommand;
import dev.th3shadowbroker.ouroboros.mines.drops.DropManager;
import dev.th3shadowbroker.ouroboros.mines.exceptions.InvalidMineMaterialException;
import dev.th3shadowbroker.ouroboros.mines.listeners.*;
import dev.th3shadowbroker.ouroboros.mines.regions.RegionProvider;
import dev.th3shadowbroker.ouroboros.mines.regions.providers.worldguard.WorldGuardProvider;
import dev.th3shadowbroker.ouroboros.mines.thirdparty.*;
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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OuroborosMines extends JavaPlugin {

    public static OuroborosMines INSTANCE;

    public static String PREFIX;

    private MaterialManager materialManager;

    private EffectManager effectManager;

    private TaskManager taskManager;

    private AnnouncementManager announcementManager;

    private DropManager dropManager;

    private RegionProvider regionProvider;

    private boolean regionProviderFound = false;

    @Override
    public void onLoad() {
        //Check for world-guard and disable if not installed
        //worldGuardFound = worldGuardIsInstalled();
        //if (!worldGuardFound) { return; }

        //Static stuff
        INSTANCE = this;

        //Region providers
        Optional<RegionProvider> regionProvider = RegionProvider.getProvider(
            WorldGuardProvider.class
        );

        //Load region provider dynamically
        if (regionProvider.isPresent()) {
            getLogger().info(String.format("Detected %s", regionProvider.get().getProviderName()));
            this.regionProvider = regionProvider.get();
            this.regionProvider.onLoad();
            regionProviderFound = true;
        } else {
            getLogger().severe("No region provider available!");
            return;
        }

        //Internal stuff
        materialManager = new MaterialManager();
        effectManager = new EffectManager();
        taskManager = new TaskManager();

        //Config
        getLogger().info("Loading configuration...");
        saveDefaultConfig();
        reloadConfig();
        updateConfig();
        PREFIX = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chat.prefix", "&9[OM]") + "&r ");

        //Register flag
        //WorldGuard.getInstance().getFlagRegistry().register(FLAG);
    }

    @Override
    public void onEnable() {
        if (!regionProviderFound) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadMineMaterials();
        loadEffects();

        getServer().getPluginManager().registerEvents( new BlockBreakListener(), this );
        getServer().getPluginManager().registerEvents( new DepositDiscoveryListener(), this );
        getServer().getPluginManager().registerEvents( new ExperienceListener(), this );
        getServer().getPluginManager().registerEvents( new TimeSkipListener(), this );
        getServer().getPluginManager().registerEvents( new PlayerInteractListener(), this );

        announcementManager = new AnnouncementManager();
        announcementManager.createTasks();

        dropManager = new DropManager(new File(getDataFolder(), "drops.yml"));

        getCommand("om").setExecutor(new OmCommand());

        checkForSupportedPlugins();

        new MetricsLite(this, 72325);
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (!regionProviderFound) { return; }
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
                     "placeholders",
                     "chat.messages.reloadingDropGroups",
                     "chat.messages.reloadedDropGroups",
                     "chat.messages.awaitingRightClick",
                     "chat.messages.awaitingRightClickCancelled",
                     "chat.messages.dropGroupCreated",
                     "chat.messages.dropGroupExists",
                     "chat.messages.missingDropGroupName",
                     "chat.messages.consoleNotAllowed",
                     "chat.messages.help"
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
        boolean questsInstalled = getServer().getPluginManager().isPluginEnabled(QuestsPikaMugSupport.PLUGIN_NAME) ||
                                  getServer().getPluginManager().isPluginEnabled(QuestsLMBishopSupport.PLUGIN_NAME);
        if (questsInstalled) {
            var pikaMugPlugin = Optional.ofNullable(getServer().getPluginManager().getPlugin(QuestsPikaMugSupport.PLUGIN_NAME));
            pikaMugPlugin.ifPresent(pl -> {
                if (pl.getDescription().getAuthors().contains(QuestsPikaMugSupport.PLUGIN_AUTHOR)) {
                    getLogger().info(String.format("Quests (by %s) support is enabled!", QuestsPikaMugSupport.PLUGIN_AUTHOR));
                    new QuestsPikaMugSupport();
                }
            });

            var lmbishopPlugin = Optional.ofNullable(getServer().getPluginManager().getPlugin(QuestsLMBishopSupport.PLUGIN_NAME));
            lmbishopPlugin.ifPresent(pl -> {
                if (pl.getDescription().getAuthors().contains(QuestsLMBishopSupport.PLUGIN_AUTHOR)) {
                    getLogger().info(String.format("Quests (by %s) support is enabled!", QuestsLMBishopSupport.PLUGIN_AUTHOR));
                    new QuestsLMBishopSupport();
                }
            });
        }

        boolean beautyQuestInstalled = getServer().getPluginManager().isPluginEnabled(BeautyQuestsSupport.PLUGIN_NAME);
        if (beautyQuestInstalled) {
            getLogger().info("BeautyQuests support is enabled!");
            new BeautyQuestsSupport();
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

        boolean townyInstalled = getServer().getPluginManager().isPluginEnabled(TownySupport.PLUGIN_NAME);
        if (townyInstalled) {
            getLogger().info("Towny support is enabled!");
            new TownySupport();
        }

        boolean itemsAdderInstalled = getServer().getPluginManager().isPluginEnabled(ItemsAdderSupport.PLUGIN_NAME);
        if (itemsAdderInstalled) {
            getLogger().info("ItemdAdder support enabled!");
            new ItemsAdderSupport();
        }

        boolean aureliumSkillsInstalled = getServer().getPluginManager().isPluginEnabled(AureliumSkillsSupport.PLUGIN_NAME);
        if (aureliumSkillsInstalled) {
            getLogger().info("AureliumSkills support enabled!");
            new AureliumSkillsSupport();
        }

        boolean ecoSkillsInstalled = getServer().getPluginManager().isPluginEnabled(EcoSkillsSupport.PLUGIN_NAME);
        if (ecoSkillsInstalled) {
            getLogger().info("EcoSkills support enabled!");
            new EcoSkillsSupport();
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

    public DropManager getDropManager() {
        return dropManager;
    }

    public RegionProvider getRegionProvider() {
        return regionProvider;
    }

}
