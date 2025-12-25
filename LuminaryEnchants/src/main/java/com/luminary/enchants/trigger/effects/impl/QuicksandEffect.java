package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Quicksand: Pull in nearby loose blocks (sand/gravel) and mine them automatically.
 */
public class QuicksandEffect extends AbstractEnchantEffect {

    private static final Set<Material> DEFAULT_LOOSE_BLOCKS = EnumSet.of(
            Material.SAND,
            Material.RED_SAND,
            Material.GRAVEL,
            Material.SOUL_SAND,
            Material.SUSPICIOUS_SAND,
            Material.SUSPICIOUS_GRAVEL
    );

    public QuicksandEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "quicksand";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int radius = definition.getParamInt("radius", 3);
        int maxBlocks = Math.min(
                definition.getParamInt("maxBlocks", 10),
                plugin.getConfigManager().getMaxBlocksPerProc()
        );
        List<String> whitelist = definition.getParamStringList("blockWhitelist");

        // Build whitelist set
        Set<Material> allowedBlocks;
        if (whitelist.isEmpty()) {
            allowedBlocks = DEFAULT_LOOSE_BLOCKS;
        } else {
            allowedBlocks = EnumSet.noneOf(Material.class);
            for (String name : whitelist) {
                try {
                    allowedBlocks.add(Material.valueOf(name.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Find and break nearby loose blocks
        List<Block> toBreak = new ArrayList<>();
        for (int x = -radius; x <= radius && toBreak.size() < maxBlocks; x++) {
            for (int y = -radius; y <= radius && toBreak.size() < maxBlocks; y++) {
                for (int z = -radius; z <= radius && toBreak.size() < maxBlocks; z++) {
                    Block nearby = block.getRelative(x, y, z);
                    if (allowedBlocks.contains(nearby.getType())) {
                        toBreak.add(nearby);
                    }
                }
            }
        }

        if (toBreak.isEmpty()) {
            return;
        }

        int broken = 0;
        for (Block b : toBreak) {
            if (canBreak(player, b)) {
                breakBlock(player, b, pickaxe, DropMode.INVENTORY, false);
                broken++;
            }
        }

        if (broken > 0) {
            // Visual effect
            player.getWorld().spawnParticle(Particle.FALLING_DUST,
                    block.getLocation().add(0.5, 0.5, 0.5), 30, radius, radius, radius, 0,
                    Material.SAND.createBlockData());
            player.playSound(block.getLocation(), Sound.BLOCK_SAND_BREAK, 0.7f, 0.8f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&e\u2B07 Quicksand pulled in " + broken + " blocks!"));
        }
    }
}
