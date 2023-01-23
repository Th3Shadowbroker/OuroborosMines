/*
 * Copyright 2022 Jens Fischer
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

import com.willfp.ecoskills.skills.Skills;
import com.willfp.ecoskills.skills.skills.SkillMining;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialCheckEvent;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialMinedEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class EcoSkillsSupport implements Listener {

    public static final String PLUGIN_NAME = "EcoSkills";

    public EcoSkillsSupport() {
        Bukkit.getServer().getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    @EventHandler
    public void onMaterialCheck(MaterialCheckEvent event) {
        var ecoPlugin = Optional.ofNullable(Bukkit.getServer().getPluginManager().getPlugin("eco"));
        if (ecoPlugin.isEmpty()) return;

        var chunk = event.getBlock().getChunk();
        var data = chunk.getPersistentDataContainer();
        var blockHash = event.getBlock().getLocation().hashCode();
        var namespace = new NamespacedKey(ecoPlugin.get(), Integer.toString(blockHash, 16));

        if (data.has(namespace, PersistentDataType.INTEGER)) {
            data.remove(namespace);
        }
    }

    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        var skill = (SkillMining) Skills.MINING;
        skill.handleLevelling(event.getOriginalEvent());
    }

}
