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

package dev.th3shadowbroker.ouroboros.mines.thirdparty;

import dev.lone.itemsadder.api.CustomBlock;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.DefaultDropsCheckEvent;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialCheckEvent;
import dev.th3shadowbroker.ouroboros.mines.events.thirdparty.itemsadder.PlaceCustomBlockEvent;
import dev.th3shadowbroker.ouroboros.mines.events.thirdparty.itemsadder.RemoveCustomBlockEvent;
import dev.th3shadowbroker.ouroboros.mines.util.MaterialIdentifier;
import dev.th3shadowbroker.ouroboros.mines.util.MineableMaterial;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class ItemsAdderSupport implements Listener {

    public static final String PLUGIN_NAME = "ItemsAdder";

    public ItemsAdderSupport() {
        Bukkit.getPluginManager().registerEvents(this, OuroborosMines.INSTANCE);
    }

    @EventHandler
    public void onMaterialCheck(MaterialCheckEvent event) {
        var customBlock = Optional.ofNullable(CustomBlock.byAlreadyPlaced(event.getBlock()));
        customBlock.ifPresent(cb -> {
            if (cb.getNamespacedID().equals(event.getMineableMaterial().getMaterialIdentifier().toString())) {
                event.setCustom(true);
            }
        });
    }

    @EventHandler
    public void onDefaultDropsCheck(DefaultDropsCheckEvent event) {
        if (isCustomBlock(event.getMineableMaterial(), event.getBlock())) {
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(event.getBlock());
            event.setDrops(customBlock.getLoot(event.getTool(), true));
        }
    }

    @EventHandler
    public void onRemoveCustomBlock(RemoveCustomBlockEvent event) {
        Optional.ofNullable(CustomBlock.byAlreadyPlaced(event.getBlock())).ifPresent(cb -> {
            cb.playBreakEffect();
            cb.playBreakParticles();
            cb.playBreakSound();
            cb.remove();
        });
    }

    @EventHandler
    public void onPlaceCustomBlock(PlaceCustomBlockEvent event) {
        Optional.ofNullable(CustomBlock.getInstance(event.getMaterialIdentifier().toString()))
                .ifPresent(cb -> {
                    cb.place(event.getLocation());
                });
    }

    private boolean isCustomBlock(MineableMaterial mineableMaterial, Block block) {
        MaterialIdentifier namespacedId = mineableMaterial.getMaterialIdentifier();
        if (!namespacedId.isInDefaultNamespace()) {
            Optional<CustomBlock> customBlock = Optional.ofNullable(CustomBlock.getInstance(namespacedId.toString()));
            return customBlock.isPresent() && Optional.ofNullable(CustomBlock.byAlreadyPlaced(block)).isPresent();
        }

        Optional<String> legacyNamespacedId = Optional.ofNullable((String) mineableMaterial.getProperties().get("ItemsAdder"));
        if (legacyNamespacedId.isPresent()) {
            OuroborosMines.INSTANCE.getLogger().warning(String.format("Material %s uses the legacy ItemsAdder property. This feature will be removed in the future!", mineableMaterial.getMaterialIdentifier()));
            Optional<CustomBlock> customBlock = Optional.ofNullable(CustomBlock.getInstance(legacyNamespacedId.get()));
            return customBlock.isPresent() && Optional.ofNullable(CustomBlock.byAlreadyPlaced(block)).isPresent();
        }
        return false;
    }

}
