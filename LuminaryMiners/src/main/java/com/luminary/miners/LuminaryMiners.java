package com.luminary.miners;

import com.luminary.miners.command.MinerAdminCommand;
import com.luminary.miners.command.MinerCommand;
import com.luminary.miners.config.ConfigManager;
import com.luminary.miners.hook.EconomyHook;
import com.luminary.miners.listener.PlayerListener;
import com.luminary.miners.miner.MinerManager;
import com.luminary.miners.placeholder.MinerPlaceholders;
import com.luminary.miners.player.PlayerDataManager;
import com.luminary.miners.task.ProductionTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryMiners extends JavaPlugin {

    private static LuminaryMiners instance;

    private ConfigManager configManager;
    private MinerManager minerManager;
    private PlayerDataManager playerDataManager;
    private EconomyHook economyHook;
    private ProductionTask productionTask;

    @Override
    public void onEnable() {
        instance = this;

        // Load configurations
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        // Initialize miner manager
        this.minerManager = new MinerManager(this);
        this.minerManager.loadMiners();

        // Initialize player data
        this.playerDataManager = new PlayerDataManager(this);

        // Hook into economy
        this.economyHook = new EconomyHook(this);
        this.economyHook.hook();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register commands
        MinerCommand minerCommand = new MinerCommand(this);
        getCommand("miners").setExecutor(minerCommand);
        getCommand("miners").setTabCompleter(minerCommand);

        MinerAdminCommand adminCommand = new MinerAdminCommand(this);
        getCommand("mineradmin").setExecutor(adminCommand);
        getCommand("mineradmin").setTabCompleter(adminCommand);

        // Start production task
        this.productionTask = new ProductionTask(this);
        this.productionTask.start();

        // Register PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MinerPlaceholders(this).register();
            getLogger().info("Registered PlaceholderAPI placeholders!");
        }

        getLogger().info("LuminaryMiners has been enabled!");
        getLogger().info("Loaded " + minerManager.getMinerTypeCount() + " miner types.");
    }

    @Override
    public void onDisable() {
        // Stop production task
        if (productionTask != null) {
            productionTask.stop();
        }

        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        getLogger().info("LuminaryMiners has been disabled!");
    }

    public void reload() {
        configManager.loadAll();
        minerManager.loadMiners();
        getLogger().info("LuminaryMiners configuration reloaded!");
    }

    public static LuminaryMiners getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MinerManager getMinerManager() {
        return minerManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public ProductionTask getProductionTask() {
        return productionTask;
    }
}
