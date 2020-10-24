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
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageMine;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class BeautyQuestsSupport implements Listener {

    public static final String PLUGIN_NAME = "BeautyQuests";

    public BeautyQuestsSupport() {
        Bukkit.getServer().getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        PlayerAccount playerAccount = PlayersManager.getPlayerAccount(event.getPlayer());
        playerAccount.getQuestsDatas().stream().filter(PlayerQuestDatas::hasStarted).forEach(data -> {
			Quest quest = data.getQuest();
			
			Optional<QuestBranch> branch = Optional.ofNullable(quest.getBranchesManager().getBranch(data.getBranch()));
			branch.ifPresent(questBranch -> {
                if (data.isInEndingStages()) {
                    questBranch.getEndingStages().forEach((stage, leadingBranch) -> {
                        handleStage(stage, playerAccount, event.getPlayer(), event.getBlock());
                    });
                } else {
                    handleStage(questBranch.getRegularStage(data.getStage()), playerAccount, event.getPlayer(), event.getBlock());
                }
            });
		});
    }
    
    private void handleStage(AbstractStage stage, PlayerAccount playerAccount, Player player, Block block){
        if (stage instanceof StageMine) {
            StageMine stageMine = (StageMine) stage;
            stageMine.event(playerAccount, player, block, 1);
        }
    }

}
