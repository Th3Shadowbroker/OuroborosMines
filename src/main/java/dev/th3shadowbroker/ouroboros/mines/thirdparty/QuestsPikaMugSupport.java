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

package dev.th3shadowbroker.ouroboros.mines.thirdparty;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialMinedEvent;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class QuestsPikaMugSupport implements Listener {

    public static final String PLUGIN_NAME = "Quests";

    public static final String PLUGIN_AUTHOR = "PikaMug";

    public QuestsPikaMugSupport() {
        Bukkit.getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    private void addProgress(Player player, Material material) {
        try
        {
            // Get plugin instance with the crowbar
            Quests quests = (Quests) Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
            Quester quester = quests.getQuester(player.getUniqueId());

            // Check for matching quests
            quester.getCurrentQuests().keySet().forEach(
                    quest -> {
                        // Modify progress manually
                        quester.getCurrentStage(quest).getBlocksToBreak().stream().filter(itemStack -> itemStack.getType() == material).forEach(
                                itemStack -> {
                                    quester.breakBlock(quest, new ItemStack(material));
                                }
                        );
                    });
        } catch (NullPointerException ex) {
            OuroborosMines.INSTANCE.getLogger().warning("Unable to manipulate Quests quest-progress. It seems the plugin isn't installed anymore.");
        }
    }

    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        addProgress(event.getPlayer(), event.getMaterial().getMaterial());
    }

}
