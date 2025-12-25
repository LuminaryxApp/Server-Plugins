package com.luminary.enchants.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a pickaxe enchant procs (activates).
 */
public class PickEnchantProcEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String enchantId;
    private final int level;
    private final ItemStack pickaxe;
    private final Block block;
    private final ProcContext context;
    private boolean cancelled;

    public PickEnchantProcEvent(Player player, String enchantId, int level, ItemStack pickaxe,
                                 @Nullable Block block, ProcContext context) {
        this.player = player;
        this.enchantId = enchantId;
        this.level = level;
        this.pickaxe = pickaxe;
        this.block = block;
        this.context = context;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getEnchantId() {
        return enchantId;
    }

    public int getLevel() {
        return level;
    }

    public ItemStack getPickaxe() {
        return pickaxe;
    }

    @Nullable
    public Block getBlock() {
        return block;
    }

    public ProcContext getContext() {
        return context;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Context for when the proc occurred.
     */
    public enum ProcContext {
        BLOCK_BREAK,
        MINE_TICK,
        INTERACT,
        SECONDARY_ROLL  // Triggered by Second Hand enchant
    }
}
