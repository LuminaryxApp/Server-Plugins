package com.luminary.backpacks;

import com.luminary.backpacks.backpack.BackpackManager;
import com.luminary.backpacks.command.BackpackCommand;
import com.luminary.backpacks.command.BackpackAdminCommand;
import com.luminary.backpacks.config.ConfigManager;
import com.luminary.backpacks.data.PlayerDataManager;
import com.luminary.backpacks.gui.BackpackGUI;
import com.luminary.backpacks.hook.EconomyHook;
import com.luminary.backpacks.listener.BackpackListener;
import com.luminary.backpacks.listener.InventoryListener;
import com.luminary.backpacks.listener.MiningListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryBackpacks extends JavaPlugin {

    private static LuminaryBackpacks instance;

    private ConfigManager configManager;
    private BackpackManager backpackManager;
    private PlayerDataManager playerDataManager;
    private EconomyHook economyHook;
    private BackpackGUI backpackGUI;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        // Initialize economy hook
        this.economyHook = new EconomyHook(this);
        this.economyHook.hook();

        // Initialize managers
        this.backpackManager = new BackpackManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.backpackGUI = new BackpackGUI(this);

        // Load online players
        Bukkit.getOnlinePlayers().forEach(p -> playerDataManager.loadPlayer(p.getUniqueId()));

        // Register listeners
        getServer().getPluginManager().registerEvents(new BackpackListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);

        // Register commands
        BackpackCommand bpCommand = new BackpackCommand(this);
        getCommand("backpack").setExecutor(bpCommand);
        getCommand("backpack").setTabCompleter(bpCommand);

        BackpackAdminCommand adminCommand = new BackpackAdminCommand(this);
        getCommand("backpackadmin").setExecutor(adminCommand);
        getCommand("backpackadmin").setTabCompleter(adminCommand);

        // Start auto-sell task
        startAutoSellTask();

        getLogger().info("LuminaryBackpacks has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        getLogger().info("LuminaryBackpacks has been disabled!");
    }

    private void startAutoSellTask() {
        if (!configManager.isAutoSellEnabled()) {
            return;
        }

        int interval = configManager.getAutoSellInterval();
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (playerDataManager.isAutoSellEnabled(player.getUniqueId())) {
                    backpackManager.autoSell(player);
                }
            });
        }, interval, interval);
    }

    public void reload() {
        configManager.loadAll();
        getLogger().info("Configuration reloaded!");
    }

    public static LuminaryBackpacks getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BackpackManager getBackpackManager() {
        return backpackManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public BackpackGUI getBackpackGUI() {
        return backpackGUI;
    }
}
