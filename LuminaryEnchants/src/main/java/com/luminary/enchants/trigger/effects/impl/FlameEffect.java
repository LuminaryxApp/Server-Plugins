package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Flame: Auto-smelts configured blocks (iron/gold/copper ore into ingots).
 */
public class FlameEffect extends AbstractEnchantEffect {

    // Default smelt mappings
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();

    static {
        SMELT_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.RAW_IRON, Material.IRON_INGOT);
        SMELT_MAP.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.COBBLESTONE, Material.STONE);
        SMELT_MAP.put(Material.SAND, Material.GLASS);
        SMELT_MAP.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    }

    public FlameEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "flame";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        double chance = definition.getParamDouble("chance", 1.0);
        List<String> smeltWhitelist = definition.getParamStringList("smeltWhitelist");

        // Check if this block can be smelted
        Material blockType = block.getType();
        Material smeltedTo = SMELT_MAP.get(blockType);

        if (smeltedTo == null) {
            // Check whitelist from config
            if (!smeltWhitelist.isEmpty()) {
                String blockName = blockType.name();
                if (!smeltWhitelist.contains(blockName)) {
                    return;
                }
            }
            return;
        }

        // Chance check (if not 100%)
        if (chance < 1.0 && !WeightedRandom.roll(chance)) {
            return;
        }

        // Give smelted item instead of raw drops
        ItemStack smelted = new ItemStack(smeltedTo, 1);
        addDropsToInventory(player, Collections.singletonList(smelted));

        // Visual effect
        player.getWorld().spawnParticle(Particle.FLAME,
                block.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.02);
        player.playSound(block.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1.2f);

        player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                .createDisplayComponent("&6\uD83D\uDD25 Flame auto-smelted!"));
    }
}
