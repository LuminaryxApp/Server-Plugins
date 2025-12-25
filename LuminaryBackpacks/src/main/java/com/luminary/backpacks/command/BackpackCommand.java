package com.luminary.backpacks.command;

import com.luminary.backpacks.LuminaryBackpacks;
import com.luminary.backpacks.data.PlayerBackpackData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BackpackCommand implements CommandExecutor, TabCompleter {

    private final LuminaryBackpacks plugin;
    private static final NumberFormat FORMATTER = NumberFormat.getInstance(Locale.US);

    public BackpackCommand(LuminaryBackpacks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("luminarybackpacks.use")) {
            sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            // Open backpack
            plugin.getBackpackGUI().openBackpack(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> showHelp(player);
            case "upgrade" -> plugin.getBackpackGUI().openUpgradeMenu(player);
            case "settings", "options" -> plugin.getBackpackGUI().openSettingsMenu(player);
            case "sell", "sellall" -> sellAll(player);
            case "autopickup", "pickup" -> toggleAutoPickup(player);
            case "autosell" -> toggleAutoSell(player);
            case "info", "status" -> showInfo(player);
            default -> plugin.getBackpackGUI().openBackpack(player);
        }

        return true;
    }

    private void showHelp(Player player) {
        sendMessage(player, "&6&l━━━━━ Backpack Help ━━━━━");
        sendMessage(player, "&e/bp &7- Open your backpack");
        sendMessage(player, "&e/bp upgrade &7- Upgrade your backpack");
        sendMessage(player, "&e/bp settings &7- Backpack settings");
        sendMessage(player, "&e/bp sell &7- Sell all items");
        sendMessage(player, "&e/bp autopickup &7- Toggle auto-pickup");
        sendMessage(player, "&e/bp autosell &7- Toggle auto-sell");
        sendMessage(player, "&e/bp info &7- View backpack info");
        sendMessage(player, "&6&l━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void sellAll(Player player) {
        double sold = plugin.getBackpackManager().sellAll(player);
        if (sold > 0) {
            sendMessage(player, plugin.getConfigManager().getMessage("sold-items",
                    "{amount}", FORMATTER.format(sold)));
        } else {
            sendMessage(player, plugin.getConfigManager().getMessage("nothing-to-sell"));
        }
    }

    private void toggleAutoPickup(Player player) {
        plugin.getPlayerDataManager().toggleAutoPickup(player.getUniqueId());
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data != null && data.isAutoPickupEnabled()) {
            sendMessage(player, plugin.getConfigManager().getMessage("auto-pickup-enabled"));
        } else {
            sendMessage(player, plugin.getConfigManager().getMessage("auto-pickup-disabled"));
        }
    }

    private void toggleAutoSell(Player player) {
        plugin.getPlayerDataManager().toggleAutoSell(player.getUniqueId());
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data != null && data.isAutoSellEnabled()) {
            sendMessage(player, plugin.getConfigManager().getMessage("auto-sell-enabled"));
        } else {
            sendMessage(player, plugin.getConfigManager().getMessage("auto-sell-disabled"));
        }
    }

    private void showInfo(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            sendMessage(player, "&cCould not load backpack data!");
            return;
        }

        int tier = data.getTier();
        String tierName = plugin.getConfigManager().getTierName(tier);
        String tierColor = plugin.getConfigManager().getTierColor(tier);
        int size = data.getSize();
        int used = data.getUsedSlots();
        double value = plugin.getBackpackManager().getTotalValue(player);
        double fillPercent = plugin.getBackpackManager().getFillPercentage(player);

        sendMessage(player, "&6&l━━━━━ Backpack Info ━━━━━");
        sendMessage(player, "&7Tier: " + tierColor + tierName + " &7(Tier " + tier + ")");
        sendMessage(player, "&7Size: &e" + size + " slots");
        sendMessage(player, "&7Used: &e" + used + "/" + size + " &7(" + String.format("%.1f", fillPercent) + "%)");
        sendMessage(player, "&7Value: &e" + FORMATTER.format(value) + " tokens");
        sendMessage(player, "&7Auto-Pickup: " + (data.isAutoPickupEnabled() ? "&aEnabled" : "&cDisabled"));
        sendMessage(player, "&7Auto-Sell: " + (data.isAutoSellEnabled() ? "&aEnabled" : "&cDisabled"));
        sendMessage(player, "&6&l━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "upgrade", "settings", "sell", "autopickup", "autosell", "info"));
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));

        return completions;
    }
}
