package com.luminary.enchants.core;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.api.BeaconEffectProvider;
import com.luminary.enchants.api.MineRegionProvider;
import com.luminary.enchants.api.TokenEconomy;
import com.luminary.enchants.core.hooks.DefaultBeaconProvider;
import com.luminary.enchants.core.hooks.DefaultMineRegionProvider;
import com.luminary.enchants.core.hooks.DisabledTokenEconomy;
import com.luminary.enchants.core.hooks.LuminaryEconomyHook;
import com.luminary.enchants.core.hooks.LuminaryMinesHook;
import com.luminary.enchants.core.hooks.VaultTokenEconomy;
import com.luminary.mines.LuminaryMines;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class HookManager {

    private final LuminaryEnchants plugin;
    private TokenEconomy tokenEconomy;
    private BeaconEffectProvider beaconProvider;
    private MineRegionProvider mineRegionProvider;

    public HookManager(LuminaryEnchants plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        setupTokenEconomy();
        setupBeaconProvider();
        setupMineRegionProvider();
    }

    private void setupTokenEconomy() {
        // Priority 1: LuminaryEconomy (our own economy plugin)
        Plugin luminaryEconomy = Bukkit.getPluginManager().getPlugin("LuminaryEconomy");
        if (luminaryEconomy != null && luminaryEconomy.isEnabled()) {
            try {
                String currencyId = plugin.getConfigManager().getMainConfig()
                        .getString("economy.currency", "tokens");
                tokenEconomy = new LuminaryEconomyHook(currencyId);
                plugin.getLogger().info("Hooked into LuminaryEconomy for token economy!");
                return;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into LuminaryEconomy: " + e.getMessage());
            }
        }

        // Priority 2: LuminaryTokens (legacy)
        Plugin luminaryTokens = Bukkit.getPluginManager().getPlugin("LuminaryTokens");
        if (luminaryTokens != null && luminaryTokens.isEnabled()) {
            try {
                RegisteredServiceProvider<TokenEconomy> provider =
                        Bukkit.getServicesManager().getRegistration(TokenEconomy.class);
                if (provider != null) {
                    tokenEconomy = provider.getProvider();
                    plugin.getLogger().info("Hooked into LuminaryTokens for token economy.");
                    return;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into LuminaryTokens: " + e.getMessage());
            }
        }

        // Priority 3: Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp =
                    Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                tokenEconomy = new VaultTokenEconomy(rsp.getProvider());
                plugin.getLogger().info("Hooked into Vault for token economy (fallback).");
                return;
            }
        }

        // No economy available
        tokenEconomy = new DisabledTokenEconomy();
        plugin.getLogger().warning("No token economy found! Enchant upgrades will be disabled.");
    }

    private void setupBeaconProvider() {
        // Try to hook into LuminaryBeacons
        Plugin luminaryBeacons = Bukkit.getPluginManager().getPlugin("LuminaryBeacons");
        if (luminaryBeacons != null && luminaryBeacons.isEnabled()) {
            try {
                RegisteredServiceProvider<BeaconEffectProvider> provider =
                        Bukkit.getServicesManager().getRegistration(BeaconEffectProvider.class);
                if (provider != null) {
                    beaconProvider = provider.getProvider();
                    plugin.getLogger().info("Hooked into LuminaryBeacons for beacon effects.");
                    return;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into LuminaryBeacons: " + e.getMessage());
            }
        }

        // Use default (returns 1.0 multipliers, level 0)
        beaconProvider = new DefaultBeaconProvider();
    }

    private void setupMineRegionProvider() {
        // Try to hook into LuminaryMines
        Plugin luminaryMines = Bukkit.getPluginManager().getPlugin("LuminaryMines");
        if (luminaryMines != null && luminaryMines.isEnabled()) {
            try {
                mineRegionProvider = new LuminaryMinesHook((LuminaryMines) luminaryMines);
                plugin.getLogger().info("Hooked into LuminaryMines for mine region protection!");
                return;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into LuminaryMines: " + e.getMessage());
            }
        }

        // Use default (allows breaking everywhere)
        mineRegionProvider = new DefaultMineRegionProvider();
        plugin.getLogger().info("No mine plugin found - enchants can break blocks anywhere.");
    }

    public TokenEconomy getTokenEconomy() {
        return tokenEconomy;
    }

    public BeaconEffectProvider getBeaconProvider() {
        return beaconProvider;
    }

    public MineRegionProvider getMineRegionProvider() {
        return mineRegionProvider;
    }

    public boolean isTokenEconomyAvailable() {
        return tokenEconomy != null && tokenEconomy.isAvailable();
    }

    public boolean isBeaconProviderAvailable() {
        return beaconProvider != null && beaconProvider.isAvailable();
    }

    public boolean isMineRegionProviderAvailable() {
        return mineRegionProvider != null && mineRegionProvider.isAvailable();
    }

    public String getTokenEconomyType() {
        if (tokenEconomy == null) return "None";
        return tokenEconomy.getProviderName();
    }
}
