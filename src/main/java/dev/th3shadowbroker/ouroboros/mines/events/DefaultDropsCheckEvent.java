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

package dev.th3shadowbroker.ouroboros.mines.events;

import dev.th3shadowbroker.ouroboros.mines.util.MineableMaterial;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultDropsCheckEvent extends Event {

    private final ItemStack tool;

    private final MineableMaterial mineableMaterial;

    private final Block block;

    private Collection<ItemStack> drops;

    private static final HandlerList handlers = new HandlerList();

    public DefaultDropsCheckEvent(ItemStack tool, MineableMaterial mineableMaterial, Block block) {
        this.tool = tool;
        this.mineableMaterial = mineableMaterial;
        this.block = block;
    }

    public ItemStack getTool() {
        return tool;
    }

    public MineableMaterial getMineableMaterial() {
        return mineableMaterial;
    }

    public Block getBlock() {
        return block;
    }

    public void setDrops(Collection<ItemStack> drops) {
        this.drops = drops;
    }

    public Collection<ItemStack> getDrops() {
        return drops != null ? drops : Collections.emptyList();
    }

    public boolean hasCustomDefaultDrops() {
        return drops != null && !drops.isEmpty();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
