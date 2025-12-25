package com.luminary.enchants.core;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.api.BeaconEffectProvider;
import com.luminary.enchants.api.TokenEconomy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

/**
 * Registers services for other plugins to use.
 */
public class ServiceRegistry {

    private final LuminaryEnchants plugin;

    public ServiceRegistry(LuminaryEnchants plugin) {
        this.plugin = plugin;
        registerServices();
    }

    private void registerServices() {
        // Other plugins can get our instances if needed
        // This is mainly for future extensibility
    }

    /**
     * Register a custom TokenEconomy provider (for LuminaryTokens to call).
     */
    public void registerTokenEconomy(TokenEconomy economy) {
        Bukkit.getServicesManager().register(
                TokenEconomy.class,
                economy,
                plugin,
                ServicePriority.Normal
        );
    }

    /**
     * Register a custom BeaconEffectProvider (for LuminaryBeacons to call).
     */
    public void registerBeaconProvider(BeaconEffectProvider provider) {
        Bukkit.getServicesManager().register(
                BeaconEffectProvider.class,
                provider,
                plugin,
                ServicePriority.Normal
        );
    }
}
