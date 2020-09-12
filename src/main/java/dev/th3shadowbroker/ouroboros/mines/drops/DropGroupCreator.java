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
import dev.th3shadowbroker.ouroboros.mines.util.Range;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DropGroupCreator {

    private static final String META_AWAITING_RIGHT_CLICK = "OM_AWAITING_RIGHT_CLICK";

    private static final String META_DROP_GROUP_NAME = "OM_DROP_GROUP_NAME";

    public static void awaitCreation(Player player, String dropGroupName) {
        if (!creationPending(player)) {
            player.setMetadata(META_AWAITING_RIGHT_CLICK, new FixedMetadataValue(OuroborosMines.INSTANCE, true));
            player.setMetadata(META_DROP_GROUP_NAME, new FixedMetadataValue(OuroborosMines.INSTANCE, dropGroupName));
        }
    }

    public static void clearCreationStatus(Player player) {
        player.removeMetadata(META_AWAITING_RIGHT_CLICK, OuroborosMines.INSTANCE);
        player.removeMetadata(META_DROP_GROUP_NAME, OuroborosMines.INSTANCE);
    }

    public static boolean creationPending(Player player) {
        return player.hasMetadata(META_AWAITING_RIGHT_CLICK) && player.hasMetadata(META_DROP_GROUP_NAME);
    }

    public static String getDropGroupName(Player player) {
        List<MetadataValue> values = player.getMetadata(META_DROP_GROUP_NAME);
        return values.isEmpty() ? null : values.get(0).asString();
    }

    public static void saveDropGroup(Inventory inventory, String dropGroupName) throws IOException {
        DropManager dropManager = OuroborosMines.INSTANCE.getDropManager();

        ConfigurationSection section = dropManager.getConfiguration().createSection(dropGroupName);
        section.set("multidrop", true);

        ConfigurationSection dropsSection = section.createSection("drops");

        List<Drop> drops = Arrays.stream(inventory.getContents()).filter(itemStack -> itemStack != null).map(itemStack -> new Drop(itemStack.clone(), 1, new Range(1, 1))).collect(Collectors.toList());
        for (int i = 0; i < drops.size(); i++) {
            ConfigurationSection dropSection = dropsSection.createSection(String.valueOf(i));

            Drop drop = drops.get(i);
            drop.getItemStack().setAmount(1);

            dropSection.set("chance", drop.getDropChance());
            dropSection.set("amount", Integer.valueOf(drop.getDropAmount().toString()));
            dropSection.set("item", drop.getItemStack());
        }

        dropManager.getConfiguration().save(dropManager.getFile());
    }

}
