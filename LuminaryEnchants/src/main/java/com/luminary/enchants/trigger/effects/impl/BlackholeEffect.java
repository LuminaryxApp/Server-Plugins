package com.luminary.enchants.trigger.effects.impl;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.effects.AbstractEnchantEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Blackhole: Pull drops into inventory automatically within radius.
 */
public class BlackholeEffect extends AbstractEnchantEffect {

    public BlackholeEffect(LuminaryEnchants plugin) {
        super(plugin);
    }

    @Override
    public String getEnchantId() {
        return "blackhole";
    }

    @Override
    public void execute(Player player, ItemStack pickaxe, Block block,
                        EnchantDefinition definition, int level, ProcEngine.ProcContext context) {

        int pickupRadius = definition.getParamInt("pickupRadius", 5);
        String overflowModeStr = definition.getParamString("overflowMode", "DROP");

        // Scale radius with level
        int scaledRadius = pickupRadius + (level / 20);

        Location center = block.getLocation().add(0.5, 0.5, 0.5);

        // Find nearby items
        Collection<Item> nearbyItems = player.getWorld().getNearbyEntitiesByType(
                Item.class, center, scaledRadius);

        if (nearbyItems.isEmpty()) {
            return;
        }

        int collected = 0;
        int voided = 0;

        for (Item item : nearbyItems) {
            ItemStack stack = item.getItemStack();
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(stack);

            if (overflow.isEmpty()) {
                item.remove();
                collected += stack.getAmount();
            } else {
                // Handle overflow based on mode
                OverflowMode mode;
                try {
                    mode = OverflowMode.valueOf(overflowModeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    mode = OverflowMode.DROP;
                }

                switch (mode) {
                    case VOID -> {
                        item.remove();
                        for (ItemStack leftover : overflow.values()) {
                            voided += leftover.getAmount();
                        }
                        collected += stack.getAmount() - overflow.values().stream()
                                .mapToInt(ItemStack::getAmount).sum();
                    }
                    case STORAGE_HOOK -> {
                        // Future: send to virtual storage
                        // For now, treat as DROP
                    }
                    case DROP -> {
                        // Leave the overflow on the ground
                        item.setItemStack(overflow.values().iterator().next());
                        collected += stack.getAmount() - overflow.values().stream()
                                .mapToInt(ItemStack::getAmount).sum();
                    }
                }
            }
        }

        if (collected > 0 || voided > 0) {
            // Visual effect
            player.getWorld().spawnParticle(Particle.PORTAL,
                    center, 50, scaledRadius * 0.5, scaledRadius * 0.5, scaledRadius * 0.5, 0.5);
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL,
                    center, 30, 0.3, 0.3, 0.3, 0.1);
            player.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);

            StringBuilder message = new StringBuilder("&5\u2B24 Blackhole absorbed " + collected + " items");
            if (voided > 0) {
                message.append(" (&cvoided ").append(voided).append("&5)");
            }
            message.append("!");

            player.sendActionBar(plugin.getPickaxeDataManager().getLoreRenderer()
                    .createDisplayComponent(message.toString()));
        }
    }

    private enum OverflowMode {
        DROP,
        VOID,
        STORAGE_HOOK
    }
}
