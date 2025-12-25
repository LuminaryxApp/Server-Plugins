package com.luminary.ranks.listener;

import com.luminary.ranks.LuminaryRanks;
import com.luminary.ranks.data.PlayerRankData;
import com.luminary.ranks.gui.RankMenuHolder;
import com.luminary.ranks.rank.Rank;
import com.luminary.ranks.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final LuminaryRanks plugin;

    public PlayerListener(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data
        PlayerRankData data = plugin.getPlayerDataManager().loadPlayer(
                player.getUniqueId(), player.getName());

        // Send welcome message with rank info
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                Rank rank = plugin.getRankManager().getRank(data.getCurrentRank());
                String rankDisplay = rank != null ? rank.getDisplayName() : "&7None";

                String welcome = plugin.getConfigManager().getRawMessage("join.welcome");
                welcome = welcome.replace("{player}", player.getName())
                                 .replace("{rank}", rankDisplay)
                                 .replace("{prestige}", String.valueOf(data.getPrestigeLevel()))
                                 .replace("{rebirth}", String.valueOf(data.getRebirthLevel()));
                player.sendMessage(TextUtil.colorize(welcome));
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RankMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        if (holder.getType() == RankMenuHolder.MenuType.MAIN) {
            handleMainMenu(player, slot);
        }
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 20 -> { // Rankup
                player.closeInventory();
                player.performCommand("rankup");
            }
            case 22 -> { // Prestige
                player.closeInventory();
                player.performCommand("prestige");
            }
            case 24 -> { // Rebirth
                player.closeInventory();
                player.performCommand("rebirth");
            }
            case 29 -> { // View ranks
                player.closeInventory();
                player.performCommand("ranks list");
            }
            case 31 -> { // View prestiges
                player.closeInventory();
                showPrestigeList(player);
            }
            case 33 -> { // View rebirths
                player.closeInventory();
                showRebirthList(player);
            }
            case 40 -> player.closeInventory();
        }
    }

    private void showPrestigeList(Player player) {
        player.sendMessage(TextUtil.colorize("&d&l━━━━━ Prestige Levels ━━━━━"));
        for (var prestige : plugin.getRankManager().getAllPrestiges()) {
            player.sendMessage(TextUtil.colorize(prestige.getDisplayName() +
                    " &7- Cost: &f" + TextUtil.formatNumber(prestige.getCost()) +
                    " &7- Multiplier: &a" + TextUtil.formatMultiplier(prestige.getMultiplier())));
        }
        player.sendMessage(TextUtil.colorize("&d&l━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void showRebirthList(Player player) {
        player.sendMessage(TextUtil.colorize("&c&l━━━━━ Rebirth Levels ━━━━━"));
        for (var rebirth : plugin.getRankManager().getAllRebirths()) {
            player.sendMessage(TextUtil.colorize(rebirth.getDisplayName() +
                    " &7- Cost: &f" + TextUtil.formatNumber(rebirth.getCost()) +
                    " &7- Permanent Multiplier: &a" + TextUtil.formatMultiplier(rebirth.getPermanentMultiplier())));
        }
        player.sendMessage(TextUtil.colorize("&c&l━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}
