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

package dev.th3shadowbroker.ouroboros.mines.thirdparty;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.skills.farming.FarmingSource;
import com.archyx.aureliumskills.skills.foraging.ForagingSource;
import com.archyx.aureliumskills.source.BlockSource;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.source.SourceManager;
import com.archyx.aureliumskills.source.SourceRegistry;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialMinedEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.Optional;

public class AureliumSkillsSupport implements Listener {

    public static final String PLUGIN_NAME = "AureliumSkills";

    public AureliumSkillsSupport() {
        Bukkit.getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    /*
    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        AureliumSkills plugin = (AureliumSkills) Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        SourceRegistry sourceRegistry = plugin.getSourceRegistry();
        SourceManager sourceManager = plugin.getSourceManager();

        for (Skill skill : Skills.values()) {
            for (Source source : sourceRegistry.values(skill)) {
                Optional<Method> matchMethod = resolveMatchMethod(source);
                if (matchMethod.isPresent() && matchMethod.get().getReturnType() == Boolean.TYPE) {
                    try {
                        boolean isMatch = (boolean) matchMethod.get().invoke(source, block);
                        if (isMatch) {
                            AureliumAPI.addXp(
                                    player,
                                    skill,
                                    sourceManager.getXp(source)
                            );
                        }
                    } catch (Exception ex) {
                        OuroborosMines.INSTANCE.getLogger().severe("Unable to match mined material with AureliumSkills. Please report this to the author of OM.");
                    }
                }
            }
        }
    }
    */

    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        AureliumSkills plugin = (AureliumSkills) Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        SourceRegistry sourceRegistry = plugin.getSourceRegistry();
        SourceManager sourceManager = plugin.getSourceManager();

        for (Skill skill : Skills.values()) {
            for (Source source : sourceRegistry.values(skill)) {

                // Blocks
                if (source instanceof BlockSource) {
                    BlockSource blockSource = (BlockSource) source;
                    if (blockSource.isMatch(block)) {
                        AureliumAPI.addXp(
                                player,
                                skill,
                                sourceManager.getXp(source)
                        );
                    }

                // Foraging sources
                } else if (source instanceof ForagingSource) {
                    ForagingSource foragingSource = (ForagingSource) source;
                    if (foragingSource.isMatch(block)) {
                        AureliumAPI.addXp(
                                player,
                                skill,
                                sourceManager.getXp(source)
                        );
                    }

                // Farming sources
                } else if (source instanceof FarmingSource) {
                    FarmingSource farmingSource = (FarmingSource) source;
                    if (farmingSource.isMatch(block)) {
                        AureliumAPI.addXp(
                                player,
                                skill,
                                sourceManager.getXp(source)
                        );
                    }
                }
            }
        }
    }

    /*
    private Optional<Method> resolveMatchMethod(Source source) {
        try {
            Method method = source.getClass().getMethod("isMatch", Block.class);
            return Optional.of(method);
        } catch (NoSuchMethodException ex) {
            return Optional.empty();
        }
    }
     */

}
