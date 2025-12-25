package com.luminary.enchants.trigger.effects;

import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Interface for enchant effect implementations.
 */
public interface EnchantEffect {

    /**
     * Get the enchant ID this effect handles.
     */
    String getEnchantId();

    /**
     * Execute the enchant effect.
     *
     * @param player The player who triggered the enchant
     * @param pickaxe The pickaxe item
     * @param block The block that was broken (may be null for some triggers)
     * @param definition The enchant definition from config
     * @param level The current level of the enchant
     * @param context The trigger context
     */
    void execute(Player player, ItemStack pickaxe, Block block,
                 EnchantDefinition definition, int level, ProcEngine.ProcContext context);
}
