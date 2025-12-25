package com.luminary.enchants;

import com.luminary.enchants.core.ConfigManager;
import com.luminary.enchants.core.HookManager;
import com.luminary.enchants.core.ServiceRegistry;
import com.luminary.enchants.gui.MenuManager;
import com.luminary.enchants.item.PickaxeDataManager;
import com.luminary.enchants.pickaxe.PickEnchantRegistry;
import com.luminary.enchants.trigger.ProcEngine;
import com.luminary.enchants.trigger.PickaxeListener;
import com.luminary.enchants.command.PickEnchantsCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LuminaryEnchants extends JavaPlugin {

    private static LuminaryEnchants instance;

    private ConfigManager configManager;
    private HookManager hookManager;
    private PickEnchantRegistry enchantRegistry;
    private PickaxeDataManager pickaxeDataManager;
    private ProcEngine procEngine;
    private MenuManager menuManager;
    private ServiceRegistry serviceRegistry;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize core services
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.hookManager = new HookManager(this);
        this.hookManager.initialize();

        this.pickaxeDataManager = new PickaxeDataManager(this);
        this.enchantRegistry = new PickEnchantRegistry(this);
        this.enchantRegistry.loadEnchants();

        this.procEngine = new ProcEngine(this);
        this.menuManager = new MenuManager(this);

        this.serviceRegistry = new ServiceRegistry(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PickaxeListener(this), this);

        // Register commands
        PickEnchantsCommand commandExecutor = new PickEnchantsCommand(this);
        getCommand("pickenchants").setExecutor(commandExecutor);
        getCommand("pickenchants").setTabCompleter(commandExecutor);

        getLogger().info("LuminaryEnchants has been enabled!");
        getLogger().info("Loaded " + enchantRegistry.getEnchantCount() + " pickaxe enchants.");

        if (hookManager.isTokenEconomyAvailable()) {
            getLogger().info("Token economy hook: ACTIVE (" + hookManager.getTokenEconomyType() + ")");
        } else {
            getLogger().warning("Token economy hook: UNAVAILABLE - upgrades will be disabled");
        }

        if (hookManager.isBeaconProviderAvailable()) {
            getLogger().info("Beacon effects hook: ACTIVE");
        } else {
            getLogger().info("Beacon effects hook: Not detected (using defaults)");
        }
    }

    @Override
    public void onDisable() {
        if (menuManager != null) {
            menuManager.closeAll();
        }
        getLogger().info("LuminaryEnchants has been disabled!");
    }

    public void reload() {
        configManager.loadAll();
        enchantRegistry.loadEnchants();
        hookManager.initialize();
        getLogger().info("LuminaryEnchants configuration reloaded!");
    }

    public static LuminaryEnchants getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public PickEnchantRegistry getEnchantRegistry() {
        return enchantRegistry;
    }

    public PickaxeDataManager getPickaxeDataManager() {
        return pickaxeDataManager;
    }

    public ProcEngine getProcEngine() {
        return procEngine;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}
