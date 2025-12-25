package com.luminary.crates;

import com.luminary.crates.animation.AnimationManager;
import com.luminary.crates.command.CratesCommand;
import com.luminary.crates.config.ConfigManager;
import com.luminary.crates.crate.CrateManager;
import com.luminary.crates.crate.CrateRegistry;
import com.luminary.crates.gui.CrateMenuManager;
import com.luminary.crates.gui.PreviewManager;
import com.luminary.crates.key.KeyManager;
import com.luminary.crates.listener.CrateListener;
import com.luminary.crates.listener.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryCrates extends JavaPlugin {

    private static LuminaryCrates instance;

    private ConfigManager configManager;
    private CrateRegistry crateRegistry;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private AnimationManager animationManager;
    private PreviewManager previewManager;
    private CrateMenuManager crateMenuManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.crateRegistry = new CrateRegistry(this);
        this.crateRegistry.loadCrates();

        this.keyManager = new KeyManager(this);
        this.crateManager = new CrateManager(this);
        this.crateManager.loadLocations();

        this.animationManager = new AnimationManager(this);
        this.previewManager = new PreviewManager(this);
        this.crateMenuManager = new CrateMenuManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        // Register commands
        CratesCommand command = new CratesCommand(this);
        getCommand("crates").setExecutor(command);
        getCommand("crates").setTabCompleter(command);

        getLogger().info("LuminaryCrates has been enabled!");
        getLogger().info("Loaded " + crateRegistry.getCrateCount() + " crate types.");
        getLogger().info("Loaded " + crateManager.getLocationCount() + " crate locations.");

        // Check for LuminaryEnchants integration
        if (getServer().getPluginManager().getPlugin("LuminaryEnchants") != null) {
            getLogger().info("LuminaryEnchants detected - Keyfinder integration active!");
        }
    }

    @Override
    public void onDisable() {
        if (animationManager != null) {
            animationManager.cancelAll();
        }
        if (crateManager != null) {
            crateManager.saveLocations();
        }
        getLogger().info("LuminaryCrates has been disabled!");
    }

    public void reload() {
        configManager.loadAll();
        crateRegistry.loadCrates();
        crateManager.loadLocations();
        getLogger().info("LuminaryCrates configuration reloaded!");
    }

    public static LuminaryCrates getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CrateRegistry getCrateRegistry() {
        return crateRegistry;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public PreviewManager getPreviewManager() {
        return previewManager;
    }

    public CrateMenuManager getCrateMenuManager() {
        return crateMenuManager;
    }
}
