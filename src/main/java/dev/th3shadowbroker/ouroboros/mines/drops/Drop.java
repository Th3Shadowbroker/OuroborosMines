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

import dev.th3shadowbroker.ouroboros.mines.util.Range;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class Drop {

    private final ItemStack itemStack;

    private final double dropChance;

    private final Range dropAmount;

    public Drop(ItemStack itemStack, double dropChance, Range dropAmount) {
        this.itemStack = itemStack;
        this.dropChance = dropChance;
        this.dropAmount = dropAmount;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getDropChance() {
        return dropChance;
    }

    public Range getDropAmount() {
        return dropAmount;
    }

    public ItemStack drawDropstack() {
        ItemStack dropStack = itemStack.clone();
        dropStack.setAmount(dropAmount.isRange() ? dropAmount.getRandomWithin() : dropAmount.getMin());
        return dropStack;
    }

    public static Drop fromSection(ConfigurationSection section) {
        ItemStack itemStack = section.getItemStack("item");
        double dropChance = section.getDouble("chance", 1);
        Range dropAmount = Range.fromString(section.getString("amount", "1"));
        return new Drop(itemStack, dropChance, dropAmount);
    }

}
