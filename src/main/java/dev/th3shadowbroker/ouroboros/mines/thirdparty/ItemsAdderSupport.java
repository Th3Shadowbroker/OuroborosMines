package dev.th3shadowbroker.ouroboros.mines.thirdparty;

import dev.lone.itemsadder.api.CustomBlock;
import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;
import dev.th3shadowbroker.ouroboros.mines.events.MaterialCheckEvent;
import org.bukkit.Bukkit;
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
        Optional<String> namespacedId = Optional.ofNullable((String) event.getMineableMaterial().getProperties().get("ItemsAdder"));
        if (namespacedId.isPresent()) {

            Optional<CustomBlock> customBlock = Optional.ofNullable(CustomBlock.getInstance(namespacedId.get()));
            if (customBlock.isPresent() && Optional.ofNullable(CustomBlock.byAlreadyPlaced(event.getBlock())).isPresent()) {
                event.setCustom(true);
            }
        }
    }

}
