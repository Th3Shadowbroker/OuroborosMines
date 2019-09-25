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

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import dev.th3shadowbroker.ouroboros.mines.exceptions.InvalidMineMaterialException;
import dev.th3shadowbroker.ouroboros.mines.listeners.BlockBreakListener;
import dev.th3shadowbroker.ouroboros.mines.util.MaterialManager;
import dev.th3shadowbroker.ouroboros.mines.util.MineableMaterial;
import dev.th3shadowbroker.ouroboros.mines.util.TaskManager;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class OuroborosMines extends JavaPlugin {

    public static OuroborosMines INSTANCE;

    public static String PREFIX;

    public static StateFlag FLAG;

    private MaterialManager materialManager;

    private TaskManager taskManager;

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
        taskManager = new TaskManager();

        //Config
        getLogger().info("Loading configuration...");
        saveDefaultConfig();
        PREFIX = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chat.prefix", "&9[OM]") + "&r ");

        //Register flag
        WorldGuard.getInstance().getFlagRegistry().register(FLAG);
    }

    @Override
    public void onEnable() {
        if (!worldGuardFound) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadMineMaterials();
        getServer().getPluginManager().registerEvents( new BlockBreakListener(), this );

        new MetricsLite(this);
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
                e.printStackTrace();
            }
        }
    }

    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }
}
