/*
 * Copyright 2019 Jens Fischer
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

package dev.th3shadowbroker.ouroboros.mines.util;

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;

public class MetaUtils {

    private static final String META_NAME = "OM_RICHNESS";

    public static boolean isRich(Block block) {
        return block.hasMetadata(META_NAME);
    }

    public static int getRichness(Block block) {
        return block.getMetadata(META_NAME).get(0).asInt();
    }

    public static void clear(Block block) {
        block.removeMetadata(META_NAME, OuroborosMines.INSTANCE);
    }

    public static void setRichness(Block block, int value) {
        block.setMetadata(META_NAME, new FixedMetadataValue(OuroborosMines.INSTANCE, value));
    }

    public static void decreaseRichness(Block block) {
        int current = getRichness(block);
        int next = current - 1;

        if (next > 0) {
            setRichness(block, next);
        } else {
            clear(block);
        }
    }

}
