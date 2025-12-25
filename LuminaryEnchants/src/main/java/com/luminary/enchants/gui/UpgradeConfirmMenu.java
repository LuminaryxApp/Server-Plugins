package com.luminary.enchants.gui;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.api.events.PickEnchantUpgradeEvent;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.util.TextUtil;
import com.luminary.enchants.util.WeightedRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Confirmation menu for upgrading an enchant.
 */
public class UpgradeConfirmMenu extends AbstractMenu {

    private static final int SLOT_INFO = 13;
    private static final int SLOT_CONFIRM = 29;
    private static final int SLOT_CANCEL = 33;

    private final String enchantId;
    private final int levelsToAdd;
    private final EnchantDefinition definition;

    public UpgradeConfirmMenu(LuminaryEnchants plugin, Player player, String enchantId, int levelsToAdd) {
        super(plugin, player, 45, "&5&lConfirm Upgrade");
        this.enchantId = enchantId;
        this.levelsToAdd = levelsToAdd;
        this.definition = plugin.getEnchantRegistry().getEnchant(enchantId);
    }

    @Override
    protected void init() {
        if (definition == null) {
            player.closeInventory();
            return;
        }

        fillBorder(Material.PURPLE_STAINED_GLASS_PANE);

        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        int currentLevel = plugin.getPickaxeDataManager().getEnchantLevel(pickaxe, enchantId);
        int targetLevel = currentLevel + levelsToAdd;
        long cost = definition.calculateUpgradeCost(currentLevel, levelsToAdd);

        boolean economyAvailable = plugin.getHookManager().isTokenEconomyAvailable();
        long balance = economyAvailable ?
                plugin.getHookManager().getTokenEconomy().get(player.getUniqueId()) : 0;
        boolean canAfford = economyAvailable && balance >= cost;

        // Info display
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add("&7Enchant: &f" + definition.getDisplayName());
        infoLore.add("&7From: &e" + currentLevel + " &7\u2192 To: &a" + targetLevel);
        infoLore.add("");
        infoLore.add("&7Cost: " + (canAfford ? "&a" : "&c") + TextUtil.formatNumber(cost) + " tokens");
        infoLore.add("&7Balance: &e" + TextUtil.formatNumber(balance) + " tokens");

        if (definition.isSuccessChanceEnabled()) {
            double successChance = definition.calculateSuccessChance(targetLevel);
            infoLore.add("");
            infoLore.add("&7Success Chance: &e" + TextUtil.formatPercent(successChance));
            infoLore.add("&7On Failure: &c" + definition.getFailMode().name().toLowerCase().replace("_", " "));
        }

        inventory.setItem(SLOT_INFO, createItem(Material.ENCHANTED_BOOK,
                "&d&lUpgrade Preview", infoLore));

        // Confirm button
        if (canAfford) {
            inventory.setItem(SLOT_CONFIRM, createItem(Material.LIME_WOOL,
                    "&a&lCONFIRM",
                    "&7Click to upgrade!",
                    "",
                    "&7Cost: &e" + TextUtil.formatNumber(cost) + " tokens"));
        } else {
            String reason = !economyAvailable ? "Economy unavailable" : "Insufficient tokens";
            inventory.setItem(SLOT_CONFIRM, createItem(Material.GRAY_WOOL,
                    "&7&lCANNOT UPGRADE",
                    "&c" + reason));
        }

        // Cancel button
        inventory.setItem(SLOT_CANCEL, createItem(Material.RED_WOOL,
                "&c&lCANCEL",
                "&7Click to go back"));
    }

    @Override
    public void handleClick(int slot, ClickType clickType) {
        if (slot == SLOT_CANCEL) {
            plugin.getMenuManager().openEnchantDetailMenu(player, enchantId);
            return;
        }

        if (slot == SLOT_CONFIRM) {
            performUpgrade();
        }
    }

    private void performUpgrade() {
        // Validate everything again
        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (!plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
            player.sendMessage(TextUtil.colorize("&cYou must hold a pickaxe!"));
            player.closeInventory();
            return;
        }

        if (!plugin.getHookManager().isTokenEconomyAvailable()) {
            player.sendMessage(TextUtil.colorize("&cToken economy is unavailable!"));
            return;
        }

        int currentLevel = plugin.getPickaxeDataManager().getEnchantLevel(pickaxe, enchantId);
        int targetLevel = currentLevel + levelsToAdd;
        long cost = definition.calculateUpgradeCost(currentLevel, levelsToAdd);
        long balance = plugin.getHookManager().getTokenEconomy().get(player.getUniqueId());

        if (balance < cost) {
            player.sendMessage(TextUtil.colorize("&cInsufficient tokens!"));
            refresh();
            return;
        }

        // Fire event
        PickEnchantUpgradeEvent event = new PickEnchantUpgradeEvent(
                player, enchantId, currentLevel, targetLevel, cost);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            player.sendMessage(TextUtil.colorize("&cUpgrade was cancelled!"));
            return;
        }

        // Withdraw tokens
        if (!plugin.getHookManager().getTokenEconomy().withdraw(player.getUniqueId(), cost,
                "Enchant upgrade: " + enchantId + " to level " + targetLevel)) {
            player.sendMessage(TextUtil.colorize("&cFailed to withdraw tokens!"));
            return;
        }

        // Check success chance if enabled
        if (definition.isSuccessChanceEnabled()) {
            double successChance = definition.calculateSuccessChance(targetLevel);

            if (!WeightedRandom.roll(successChance)) {
                // Upgrade failed!
                handleUpgradeFailure(pickaxe, currentLevel);
                return;
            }
        }

        // Success! Apply the enchant
        ItemStack upgraded = plugin.getPickaxeDataManager().setEnchant(
                pickaxe, enchantId, targetLevel);
        player.getInventory().setItemInMainHand(upgraded);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.sendMessage(TextUtil.colorize("&a&lSUCCESS! &7" + definition.getDisplayName() +
                " upgraded to level &a" + targetLevel + "&7!"));

        // Go back to detail menu
        plugin.getMenuManager().openEnchantDetailMenu(player, enchantId);
    }

    private void handleUpgradeFailure(ItemStack pickaxe, int currentLevel) {
        switch (definition.getFailMode()) {
            case NO_CHANGE -> {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(TextUtil.colorize("&c&lFAILED! &7The upgrade failed. No changes made."));
            }
            case DOWNGRADE -> {
                int newLevel = Math.max(0, currentLevel - 1);
                ItemStack modified = plugin.getPickaxeDataManager().setEnchant(pickaxe, enchantId, newLevel);
                player.getInventory().setItemInMainHand(modified);

                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.5f);
                player.sendMessage(TextUtil.colorize("&c&lFAILED! &7The upgrade failed and you lost a level! " +
                        "Now at level &e" + newLevel + "&7."));
            }
            case REMOVE -> {
                ItemStack modified = plugin.getPickaxeDataManager().removeEnchant(pickaxe, enchantId);
                player.getInventory().setItemInMainHand(modified);

                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 0.5f);
                player.sendMessage(TextUtil.colorize("&c&lFAILED! &7The upgrade failed catastrophically! " +
                        "&cEnchant removed!"));
            }
            default -> {
                player.sendMessage(TextUtil.colorize("&c&lFAILED! &7The upgrade failed."));
            }
        }

        plugin.getMenuManager().openEnchantDetailMenu(player, enchantId);
    }
}
