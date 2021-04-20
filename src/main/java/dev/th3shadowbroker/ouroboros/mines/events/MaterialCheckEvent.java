package dev.th3shadowbroker.ouroboros.mines.events;

import dev.th3shadowbroker.ouroboros.mines.util.MineableMaterial;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MaterialCheckEvent extends Event {

    private final Block block;

    private final MineableMaterial mineableMaterial;

    private boolean custom;

    private static final HandlerList handlerList = new HandlerList();

    public MaterialCheckEvent(Block block, MineableMaterial mineableMaterial) {
        this.block = block;
        this.mineableMaterial = mineableMaterial;
        this.custom = false;
    }

    public Block getBlock() {
        return block;
    }

    public MineableMaterial getMineableMaterial() {
        return mineableMaterial;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
