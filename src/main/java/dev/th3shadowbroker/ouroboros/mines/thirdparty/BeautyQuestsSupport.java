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
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageMine;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BeautyQuestsSupport implements Listener {

    public static final String PLUGIN_NAME = "BeautyQuests";

    private final OuroborosMines plugin;

    public BeautyQuestsSupport() {
        this.plugin = OuroborosMines.INSTANCE;
        Bukkit.getServer().getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        PlayerAccount playerAccount = PlayersManager.getPlayerAccount(event.getPlayer());
        List<Quest> playerQuests = QuestsAPI.getQuestsStarteds(playerAccount);

        playerQuests.forEach(quest -> {
            Optional<QuestBranch> questBranch = Optional.ofNullable(quest.getBranchesManager().getPlayerBranch(playerAccount));
            if (!questBranch.isPresent()) return;

            Optional<PlayerQuestDatas> pqd = Optional.ofNullable(playerAccount.getQuestDatas(quest));
            if (!pqd.isPresent()) return;

            Optional<AbstractStage> stage = Optional.ofNullable(questBranch.get().getRegularStage(pqd.get().getStage()));
            if (!stage.isPresent()) return;

            if (stage.get().getType() == StageType.getStageType("MINE")) {
                StageMine stageMine = (StageMine) stage.get();
                List<Material> matchingMaterials = stageMine.getObjects().values().stream().map(bqBlockIntegerEntry -> bqBlockIntegerEntry.getKey().getMaterial().parseMaterial()).collect(Collectors.toList());

                if (matchingMaterials.contains(event.getMaterial().getMaterial())) {
                    try
                    {
                        Method eventMethod = AbstractCountableStage.class.getDeclaredMethod("event", PlayerAccount.class, Player.class, Object.class, Integer.TYPE);
                        eventMethod.setAccessible(true);
                        eventMethod.invoke(stageMine, playerAccount, event.getPlayer(), event.getBlock(), 1);
                    } catch (NoSuchMethodException e) {
                        plugin.getLogger().severe("Unable to find method \"event\" in " + AbstractCountableStage.class.getName());
                    } catch (IllegalAccessException e) {
                        plugin.getLogger().severe("Unable to access method \"event\" in " + AbstractCountableStage.class.getName());
                    } catch (InvocationTargetException e) {
                        plugin.getLogger().severe("Unable to invoke method \"event\" in " + AbstractCountableStage.class.getName());
                    }
                }
            }
        });
    }

}
