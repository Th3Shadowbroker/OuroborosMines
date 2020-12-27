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

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DropGroup {

    private final List<Drop> drops;

    private final boolean multidrop;

    private final boolean override;

    public DropGroup(List<Drop> drops, boolean multidrop, boolean override) {
        this.drops = drops;
        this.multidrop = multidrop;
        this.override = override;
    }

    public boolean isValid() {
        return multidrop || drops.stream().mapToDouble(Drop::getDropChance).sum() <= 1;
    }

    public List<Drop> getDrops() {
        return drops;
    }

    public boolean isMultidrop() {
        return multidrop;
    }

    public boolean isOverriding() {
        return override;
    }

    private ItemStack[] drawMultidrop() {
        final List<ItemStack> drops = new ArrayList<>();
        this.drops.forEach(
                drop -> {

                    // Drop chance 100 or higher
                    if (drop.getDropChance() >= 1) {

                        //@FIXME Optimization needed
                        ItemStack drawnStack = drop.drawDropstack();
                        if (drawnStack != null) {
                            drops.add(drawnStack);
                        } else {
                            Server server = OuroborosMines.INSTANCE.getServer();
                            drop.getCommands().forEach(command -> {
                                server.dispatchCommand(server.getConsoleSender(), command);
                            });
                        }

                    // Drop chance below 100
                    } else {
                        Random rnd = new Random();
                        boolean shallDrop = ((double) rnd.nextInt(100) / 100) <= drop.getDropChance();
                        if (shallDrop) {
                            ItemStack drawnStack = drop.drawDropstack();

                            // If-clause implemented in favour of command-drops
                            if (drawnStack != null) {
                                drops.add(drawnStack);
                            } else {
                                Server server = OuroborosMines.INSTANCE.getServer();
                                drop.getCommands().forEach(command -> {
                                    server.dispatchCommand(server.getConsoleSender(), command);
                                });
                            }
                        }
                    }

                }
        );
        return drops.stream().toArray(ItemStack[]::new);
    }

    private ItemStack[] drawSingledrop() {
        ItemStack dropStack = null;
        Random rnd = new Random();

        double drawnChance = ((double) rnd.nextInt(100) / 100);
        double offset = 0;

        for (Drop drop : drops) {
            boolean shallDrop = drawnChance <= drop.getDropChance() + offset;

            if (shallDrop) {
                ItemStack drawnStack = drop.drawDropstack();

                // If-clause implemented in favour of command-drops
                if (drawnStack != null) {
                    dropStack = drawnStack;
                } else {
                    Server server = OuroborosMines.INSTANCE.getServer();
                    drop.getCommands().forEach(command -> {
                        server.dispatchCommand(server.getConsoleSender(), command);
                    });
                }

                break;
            } else {
                offset += drop.getDropChance();
            }
        }

        return dropStack == null ? new ItemStack[0] : new ItemStack[]{ dropStack };
    }

    public ItemStack[] drawDrops() {
        return multidrop ? drawMultidrop() : drawSingledrop();
    }

}
