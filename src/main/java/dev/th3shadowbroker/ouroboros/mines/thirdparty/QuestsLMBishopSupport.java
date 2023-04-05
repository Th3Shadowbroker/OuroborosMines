/*
 * Copyright 2023 Jens Fischer
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

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskTypeManager;
import com.leonardobishop.quests.bukkit.tasktype.type.MiningTaskType;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialMinedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestsLMBishopSupport implements Listener {

    public static final String PLUGIN_NAME = "Quests";

    public static final String PLUGIN_AUTHOR = "LMBishop & contributors";

    private final BukkitTaskTypeManager typeManager;

    public QuestsLMBishopSupport() {
        var plugin = (BukkitQuestsPlugin) Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        this.typeManager = (BukkitTaskTypeManager) plugin.getTaskTypeManager();
        Bukkit.getServer().getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        var task = (MiningTaskType) typeManager.getTaskType("blockbreak");
        task.onBlockBreak(event.getOriginalEvent());
    }

}
