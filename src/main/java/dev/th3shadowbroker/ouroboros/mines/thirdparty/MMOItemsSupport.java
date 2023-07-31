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

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.DefaultDropsCheckEvent;
import dev.th3shadowbroker.ouroboros.mines.events.thirdparty.mmoitems.DamageMMOItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Map;

public class MMOItemsSupport implements Listener {

    public static final String PLUGIN_NAME = "MMOItems";

    private Map<Integer, DropTable> dropTables;

    public MMOItemsSupport() {
        Bukkit.getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    @EventHandler
    public void onItemDamaged(DamageMMOItem event) {
        var item = new DurabilityItem(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
        if (!item.isValid())
            return;

        var damageEvent = new PlayerItemDamageEvent(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand(), 1);
        event.setApplyDefaultDamage(false);
        Bukkit.getPluginManager().callEvent(damageEvent);
    }

    @EventHandler
    public void onDefaultDropsCheck(DefaultDropsCheckEvent event) {
        event.setOverrideDrops(true);
        MMOItems.plugin.getDropTables().blockDrops(event.getOriginalEvent());
    }

}
