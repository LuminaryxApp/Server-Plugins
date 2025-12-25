package com.luminary.economy;

import com.luminary.economy.api.EconomyAPI;
import com.luminary.economy.command.*;
import com.luminary.economy.config.ConfigManager;
import com.luminary.economy.currency.CurrencyManager;
import com.luminary.economy.data.DataManager;
import com.luminary.economy.listener.PlayerListener;
import com.luminary.economy.placeholder.EconomyPlaceholders;
import com.luminary.economy.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryEconomy extends JavaPlugin {

    private static LuminaryEconomy instance;

    private ConfigManager configManager;
    private DataManager dataManager;
    private CurrencyManager currencyManager;
    private ScoreboardManager scoreboardManager;
    private EconomyAPI economyAPI;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.loadCurrencies();

        this.dataManager = new DataManager(this);
        this.dataManager.initialize();

        this.scoreboardManager = new ScoreboardManager(this);

        // Initialize API
        this.economyAPI = new EconomyAPI(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register commands
        registerCommands();

        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EconomyPlaceholders(this).register();
            getLogger().info("PlaceholderAPI found - placeholders registered!");
        }

        // Start scoreboard update task
        scoreboardManager.startUpdateTask();

        getLogger().info("LuminaryEconomy has been enabled!");
        getLogger().info("Loaded " + currencyManager.getCurrencyCount() + " currencies.");

        // Check for integrations
        if (getServer().getPluginManager().getPlugin("LuminaryEnchants") != null) {
            getLogger().info("LuminaryEnchants detected - enchant purchases enabled!");
        }
        if (getServer().getPluginManager().getPlugin("LuminaryCrates") != null) {
            getLogger().info("LuminaryCrates detected - crate integration enabled!");
        }
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (dataManager != null) {
            dataManager.saveAll();
            dataManager.shutdown();
        }

        // Stop scoreboard updates
        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }

        getLogger().info("LuminaryEconomy has been disabled!");
    }

    private void registerCommands() {
        EconomyCommand ecoCommand = new EconomyCommand(this);
        getCommand("economy").setExecutor(ecoCommand);
        getCommand("economy").setTabCompleter(ecoCommand);

        BalanceCommand balCommand = new BalanceCommand(this);
        getCommand("tokens").setExecutor(balCommand);
        getCommand("tokens").setTabCompleter(balCommand);
        getCommand("beacons").setExecutor(balCommand);
        getCommand("beacons").setTabCompleter(balCommand);
        getCommand("gems").setExecutor(balCommand);
        getCommand("gems").setTabCompleter(balCommand);

        PayCommand payCommand = new PayCommand(this);
        getCommand("pay").setExecutor(payCommand);
        getCommand("pay").setTabCompleter(payCommand);

        ScoreboardCommand sbCommand = new ScoreboardCommand(this);
        getCommand("scoreboard").setExecutor(sbCommand);
    }

    public void reload() {
        configManager.loadAll();
        currencyManager.loadCurrencies();
        scoreboardManager.reloadConfig();
        getLogger().info("LuminaryEconomy configuration reloaded!");
    }

    public static LuminaryEconomy getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public EconomyAPI getAPI() {
        return economyAPI;
    }
}
