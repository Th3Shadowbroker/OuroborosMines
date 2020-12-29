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

import dev.th3shadowbroker.ouroboros.mines.drops.types.AbstractDrop;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DropGroup {

    private final List<AbstractDrop> drops;

    private final boolean multidrop;

    private final boolean override;

    public DropGroup(List<AbstractDrop> drops, boolean multidrop, boolean override) {
        this.drops = drops;
        this.multidrop = multidrop;
        this.override = override;
    }

    public boolean isMultidrop() {
        return multidrop;
    }

    public boolean isOverriding() {
        return override;
    }

    public AbstractDrop[] drawMultidrop() {
        List<AbstractDrop> drops = new ArrayList<>();
        this.drops.forEach(drop -> {
            if (drop.getChance() >= 1) {
                drops.add(drop);
            } else {
                Random rnd = new Random();
                boolean shallDrop = ((double) rnd.nextInt(100) / 100) <= drop.getChance();
                if (shallDrop) drops.add(drop);
            }
        });

        return drops.stream().toArray(AbstractDrop[]::new);
    }

    public AbstractDrop drawSingledrop() {
        AbstractDrop drop = null;
        Random rnd = new Random();

        double drawn = ((double) rnd.nextInt(100) / 100);
        double offset = 0;

        for (AbstractDrop pDrop : drops) {
            boolean shallDrop = pDrop.getChance() <= drawn + offset;
            if (shallDrop) {
                drop = pDrop;
                break;
            } else {
                offset += drop.getChance();
            }
        }

        return drop;
    }

    public void drop(Player player, Location blockLocation) {
        if (multidrop) {
            for (AbstractDrop d : drawMultidrop()) {
                d.drop(player, blockLocation);
            }
        } else {
            Optional<AbstractDrop> ad = Optional.ofNullable(drawSingledrop());
            ad.ifPresent(d -> d.drop(player, blockLocation));
        }
    }

}
