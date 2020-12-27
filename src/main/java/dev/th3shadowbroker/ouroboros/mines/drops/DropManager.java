/*
 * Copyright 2020 Jens Fischer
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

package dev.th3shadowbroker.ouroboros.mines.drops;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class DropManager {

    private final File file;

    private final FileConfiguration configuration;

    private final Map<String, DropGroup> dropGroups;

    private final Logger log;

    public DropManager(File dropFile) {
        this.dropGroups = new HashMap<>();
        this.file = dropFile;
        this.log = OuroborosMines.INSTANCE.getLogger();
        if (!dropFile.exists()) {
            try {
                boolean dropFileCreated = dropFile.createNewFile();
                if (dropFileCreated) log.info("Drop file created.");
            } catch (IOException ex) {
                log.warning("Failed to create drop file!");
            }
        }
        this.configuration = YamlConfiguration.loadConfiguration(dropFile);
        loadGroups();
    }

    private void loadGroups() {
        log.info(String.format("Loading drops from %s", file.getAbsolutePath()));

        for (String groupName : configuration.getKeys(false)) {
            ConfigurationSection groupSection = configuration.getConfigurationSection(groupName);
            if (groupSection == null) continue;

            try {
                ConfigurationSection dropSection = groupSection.getConfigurationSection("drops");
                boolean multidrop = groupSection.getBoolean("multidrop", false);
                boolean override = groupSection.getBoolean("override", true);

                final List<Drop> drops = new ArrayList<>();
                dropSection.getKeys(false).forEach( key -> {
                    drops.add(Drop.fromSection(dropSection.getConfigurationSection(key)));
                } );

                dropGroups.put(groupName, new DropGroup(drops, multidrop, override));
            } catch (Exception ex) {
                log.warning(String.format("Unable to evaluate the %s section of %s. Please check the wiki. Skipping.", groupName, file.getAbsolutePath()));
            }
        }

        log.info(String.format("Loaded %s drop groups!", dropGroups.size()));
    }

    public void reloadGroups() {
        dropGroups.clear();
        loadGroups();
    }

    public void addGroup(String groupName, DropGroup group) {
        dropGroups.put(groupName, group);
    }

    public Optional<DropGroup> getDropGroup(String groupName) {
        return Optional.ofNullable(dropGroups.getOrDefault(groupName, null));
    }

    public String[] getGroupNames() {
        return dropGroups.keySet().stream().toArray(String[]::new);
    }

    public DropGroup[] getGroups() {
        return dropGroups.values().stream().toArray(DropGroup[]::new);
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }
}
