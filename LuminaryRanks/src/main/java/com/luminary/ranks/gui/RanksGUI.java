package com.luminary.ranks.gui;

import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.rank.Prestige;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.rank.Rebirth;
import com.luminary.ranks.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RanksGUI {

    private final LuminaryRanks plugin;

    public RanksGUI(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        RankMenuHolder holder = new RankMenuHolder(RankMenuHolder.MenuType.MAIN);
        Inventory inv = Bukkit.createInventory(holder, 45, TextUtil.colorize("&6&lRank Progression"));
        holder.setInventory(inv);

        PlayerRankData data = plugin.getPlayerDataManager().getOrLoadPlayerData(
                player.getUniqueId(), player.getName());

        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);

        // Current rank info
        Rank currentRank = plugin.getRankManager().getRank(data.getCurrentRank());
        Rank nextRank = plugin.getRankManager().getNextRank(data.getCurrentRank());

        inv.setItem(13, createRankInfoItem(data, currentRank, nextRank, player));

        // Rankup button
        inv.setItem(20, createRankupItem(nextRank, player));

        // Prestige button
        inv.setItem(22, createPrestigeItem(data));

        // Rebirth button
        inv.setItem(24, createRebirthItem(data));

        // View all ranks
        inv.setItem(29, createItem(Material.BOOK, "&e&lView All Ranks",
                "&7See the complete rank list",
                "",
                "&eClick to view!"));

        // View prestiges
        inv.setItem(31, createItem(Material.NETHER_STAR, "&d&lView Prestiges",
                "&7See prestige bonuses",
                "",
                "&eClick to view!"));

        // View rebirths
        inv.setItem(33, createItem(Material.END_CRYSTAL, "&c&lView Rebirths",
                "&7See rebirth bonuses",
                "",
                "&eClick to view!"));

        // Close
        inv.setItem(40, createItem(Material.BARRIER, "&c&lClose", "&7Close this menu"));

        player.openInventory(inv);
    }

    private ItemStack createRankInfoItem(PlayerRankData data, Rank currentRank, Rank nextRank, Player player) {
        double multiplier = plugin.getRankManager().getMultiplier(
                data.getPrestigeLevel(), data.getRebirthLevel());

        List<String> lore = new ArrayList<>();
        lore.add("&7Current Rank: " + (currentRank != null ? currentRank.getDisplayName() : "&7None"));
        lore.add("&7Prestige: &d" + data.getPrestigeLevel() + "&7/&d" + plugin.getRankManager().getMaxPrestige());
        lore.add("&7Rebirth: &c" + data.getRebirthLevel() + "&7/&c" + plugin.getRankManager().getMaxRebirth());
        lore.add("");
        lore.add("&7Multiplier: &a" + TextUtil.formatMultiplier(multiplier));
        lore.add("");
        lore.add("&7Total Rankups: &f" + data.getTotalRankups());
        lore.add("&7Tokens Spent: &f" + TextUtil.formatNumber(data.getTokensSpent()));

        return createItem(Material.DIAMOND, "&b&lYour Progress", lore.toArray(new String[0]));
    }

    private ItemStack createRankupItem(Rank nextRank, Player player) {
        if (nextRank == null) {
            return createItem(Material.BARRIER, "&c&lMax Rank",
                    "&7You've reached the highest rank!",
                    "",
                    "&7Consider prestiging or rebirthing.");
        }

        long cost = nextRank.getCost();
        long balance = plugin.getEconomyHook().getBalance(player);
        boolean canAfford = balance >= cost;

        Material mat = canAfford ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = canAfford ? "&aClick to rank up!" : "&cInsufficient funds!";

        return createItem(mat, "&a&lRank Up",
                "&7Next Rank: " + nextRank.getDisplayName(),
                "&7Cost: &f" + TextUtil.formatNumber(cost),
                "&7Balance: &f" + TextUtil.formatNumber(balance),
                "",
                status);
    }

    private ItemStack createPrestigeItem(PlayerRankData data) {
        boolean isMaxRank = plugin.getRankManager().isMaxRank(data.getCurrentRank());
        boolean isMaxPrestige = plugin.getRankManager().isMaxPrestige(data.getPrestigeLevel());

        if (isMaxPrestige) {
            return createItem(Material.MAGENTA_GLAZED_TERRACOTTA, "&d&lMax Prestige",
                    "&7You've reached max prestige!",
                    "",
                    "&7Consider rebirthing for more bonuses.");
        }

        Prestige next = plugin.getRankManager().getNextPrestige(data.getPrestigeLevel());
        if (next == null) {
            return createItem(Material.BARRIER, "&cError", "&7Could not load prestige data");
        }

        String status = isMaxRank ? "&eClick to prestige!" : "&cMust be rank Z to prestige!";

        return createItem(Material.MAGENTA_DYE, "&d&lPrestige",
                "&7Current: &dP" + data.getPrestigeLevel(),
                "&7Next: " + next.getDisplayName(),
                "&7Cost: &f" + TextUtil.formatNumber(next.getCost()),
                "&7Bonus: &a" + TextUtil.formatMultiplier(next.getMultiplier()) + " multiplier",
                "",
                "&cResets your rank to A!",
                "",
                status);
    }

    private ItemStack createRebirthItem(PlayerRankData data) {
        boolean isMaxPrestige = plugin.getRankManager().isMaxPrestige(data.getPrestigeLevel());
        boolean isMaxRank = plugin.getRankManager().isMaxRank(data.getCurrentRank());
        boolean isMaxRebirth = plugin.getRankManager().isMaxRebirth(data.getRebirthLevel());

        if (isMaxRebirth) {
            return createItem(Material.DRAGON_EGG, "&c&lMax Rebirth",
                    "&7You've reached the ultimate level!",
                    "",
                    "&7You are a legend!");
        }

        Rebirth next = plugin.getRankManager().getNextRebirth(data.getRebirthLevel());
        if (next == null) {
            return createItem(Material.BARRIER, "&cError", "&7Could not load rebirth data");
        }

        String status;
        if (!isMaxPrestige) {
            status = "&cMust be max prestige (P" + plugin.getRankManager().getMaxPrestige() + ")!";
        } else if (!isMaxRank) {
            status = "&cMust be rank Z!";
        } else {
            status = "&eClick to rebirth!";
        }

        return createItem(Material.RED_DYE, "&c&lRebirth",
                "&7Current: &cR" + data.getRebirthLevel(),
                "&7Next: " + next.getDisplayName(),
                "&7Cost: &f" + TextUtil.formatNumber(next.getCost()),
                "&7Bonus: &a" + TextUtil.formatMultiplier(next.getPermanentMultiplier()) + " permanent multiplier",
                "",
                "&4Resets rank AND prestige!",
                "",
                status);
    }

    private void fillBorder(Inventory inv, Material material) {
        ItemStack glass = createItem(material, " ");
        int size = inv.getSize();

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
            inv.setItem(size - 9 + i, glass);
        }
        for (int i = 9; i < size - 9; i += 9) {
            inv.setItem(i, glass);
            inv.setItem(i + 8, glass);
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(TextUtil.colorize(name));

            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(TextUtil.colorize("&7" + line.replace("&7", "")));
                }
                meta.lore(loreList);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
