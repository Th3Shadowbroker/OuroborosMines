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

package dev.th3shadowbroker.ouroboros.mines.drops.types;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.util.Range;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemDrop extends AbstractDrop {

    private final ItemStack itemStack;

    private final Range amount;

    public ItemDrop(ItemStack itemStack, double chance, Range amount) {
        super(chance);
        this.itemStack = itemStack;
        this.amount = amount;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Range getAmount() {
        return amount;
    }

    @Override
    public void drop(Player player, Location blockLocation) {
        ItemStack itemStack = this.itemStack.clone();
        itemStack.setAmount(amount.isRange() ? amount.getRandomWithin() : amount.getMin());

        if (OuroborosMines.INSTANCE.getConfig().getBoolean("autoPickup", false)) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(itemStack);
            overflow.values().forEach(i -> blockLocation.getWorld().dropItem(blockLocation, i));
        } else {
            blockLocation.getWorld().dropItem(blockLocation, itemStack);
        }
    }

    public static ItemDrop fromSection(ConfigurationSection section) {
        ItemStack itemStack = section.getItemStack("item");
        double chance = section.getDouble("chance", 1);
        Range amount = Range.fromString(section.getString("amount", "1"));
        return new ItemDrop(itemStack, chance, amount);
    }

}
