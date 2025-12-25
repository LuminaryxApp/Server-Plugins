package com.luminary.economy.placeholder;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.currency.Currency;
import com.luminary.economy.data.PlayerData;
import com.luminary.economy.util.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for LuminaryEconomy.
 *
 * Placeholders:
 * - %luminaryeconomy_tokens% - Tokens balance (formatted)
 * - %luminaryeconomy_tokens_raw% - Tokens balance (raw number)
 * - %luminaryeconomy_tokens_formatted% - Tokens balance (compact: 1.5K, 2.3M)
 * - %luminaryeconomy_beacons% - Beacons balance
 * - %luminaryeconomy_gems% - Gems balance
 * - %luminaryeconomy_<currency>% - Any currency balance
 * - %luminaryeconomy_<currency>_raw% - Any currency raw balance
 * - %luminaryeconomy_<currency>_formatted% - Any currency compact format
 */
public class EconomyPlaceholders extends PlaceholderExpansion {

    private final LuminaryEconomy plugin;

    public EconomyPlaceholders(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "luminaryeconomy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Luminary";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            // Try to load data for offline player
            data = plugin.getDataManager().getOrLoadPlayerData(player.getUniqueId(),
                    player.getName() != null ? player.getName() : "Unknown");
        }

        if (data == null) {
            return "0";
        }

        String[] parts = params.toLowerCase().split("_");
        String currencyId = parts[0];
        String modifier = parts.length > 1 ? parts[1] : "";

        Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
        if (currency == null) {
            // Try common aliases
            currency = switch (currencyId) {
                case "token" -> plugin.getCurrencyManager().getCurrency("tokens");
                case "beacon" -> plugin.getCurrencyManager().getCurrency("beacons");
                case "gem" -> plugin.getCurrencyManager().getCurrency("gems");
                default -> null;
            };
        }

        if (currency == null) {
            return "0";
        }

        double balance = data.getBalance(currency);

        return switch (modifier) {
            case "raw" -> String.valueOf((long) balance);
            case "formatted", "compact" -> TextUtil.formatCompact(balance);
            default -> currency.format(balance);
        };
    }
}
