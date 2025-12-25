package com.luminary.enchants.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player upgrades a pickaxe enchant.
 */
public class PickEnchantUpgradeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String enchantId;
    private final int fromLevel;
    private final int toLevel;
    private final long totalCost;
    private boolean cancelled;

    public PickEnchantUpgradeEvent(Player player, String enchantId, int fromLevel, int toLevel, long totalCost) {
        this.player = player;
        this.enchantId = enchantId;
        this.fromLevel = fromLevel;
        this.toLevel = toLevel;
        this.totalCost = totalCost;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getEnchantId() {
        return enchantId;
    }

    public int getFromLevel() {
        return fromLevel;
    }

    public int getToLevel() {
        return toLevel;
    }

    public int getLevelsGained() {
        return toLevel - fromLevel;
    }

    public long getTotalCost() {
        return totalCost;
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
}
