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

package dev.th3shadowbroker.ouroboros.mines.listeners;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialMinedEvent;
import dev.th3shadowbroker.ouroboros.mines.util.Range;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExperienceListener implements Listener {

    private final OuroborosMines plugin = OuroborosMines.INSTANCE;

    @EventHandler
    public void onMaterialMined(MaterialMinedEvent event) {
        if (!event.isFromDeposit()) {
            event.getMaterial().getExperience().ifNotZero(range -> {
                if (plugin.getConfig().getBoolean("experience.spawnOrbs")) {
                    spawnExperienceInRange(event.getBlock().getLocation(), range);
                } else {
                    giveExperience(event.getPlayer(), range);
                }
            });
        } else {
            event.getMaterial().getDepositExperience().ifNotZero(range -> {
                if (plugin.getConfig().getBoolean("experience.spawnOrbs")) {
                    spawnExperienceInRange(event.getBlock().getLocation(), range);
                } else {
                    giveExperience(event.getPlayer(), range);
                }
            });
        }
    }

    private void giveExperience(Player player, Range range) {
        int amount = !range.isRange() ? range.getMin() : range.getRandomWithin();
        player.giveExp(amount);
    }

    private void spawnExperienceInRange(Location location, Range range) {
        ExperienceOrb orb = ((ExperienceOrb) location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB));
        int amount = !range.isRange() ? range.getMin() : range.getRandomWithin();
        orb.setExperience(amount);
    }

}
