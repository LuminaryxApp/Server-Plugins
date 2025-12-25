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
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Ghostrider: Mines through protected filler blocks by skipping them.
 */
public class GhostriderEffect extends AbstractEnchantEffect {

    private static final Set<Material> DEFAULT_SKIP_MATERIALS = EnumSet.of(
            Material.BEDROCK,
            Material.BARRIER,
            Material.STRUCTURE_VOID,
            Material.COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK
    );

    public GhostriderEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "ghostrider";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        List<String> skipMaterialsList = definition.getParamStringList("skipMaterials");
        int extraRange = definition.getParamInt("extraRange", 3);

        // Build skip materials set
        Set<Material> skipMaterials;
        if (skipMaterialsList.isEmpty()) {
            skipMaterials = DEFAULT_SKIP_MATERIALS;
        } else {
            skipMaterials = EnumSet.noneOf(Material.class);
            for (String name : skipMaterialsList) {
                try {
                    skipMaterials.add(Material.valueOf(name.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Scale range with level
        int scaledRange = extraRange + (level / 10);
        int maxBlocks = Math.min(scaledRange, plugin.getConfigManager().getMaxBlocksPerProc());

        // Get facing direction
        Vector direction = getFacingAxis(player);

        // Break blocks in line, skipping protected blocks
        int broken = breakBlocksLine(player, block, pickaxe, direction, scaledRange * 2, maxBlocks,
                DropMode.INVENTORY, skipMaterials);

        if (broken > 0) {
            // Ghost particle effect
            player.getWorld().spawnParticle(Particle.SOUL,
                    block.getLocation().add(0.5, 0.5, 0.5), 20, 0.3, 0.3, 0.3, 0.02);
            player.playSound(block.getLocation(), Sound.ENTITY_VEX_AMBIENT, 0.4f, 0.5f);

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent("&8\uD83D\uDC7B Ghostrider phased through " + broken + " blocks!"));
        }
    }
}
