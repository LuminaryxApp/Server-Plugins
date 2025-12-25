package com.luminary.miners.gui;

import com.luminary.miners.LuminaryMiners;
import com.luminary.miners.miner.MinerType;
import com.luminary.miners.miner.PlayerMiner;
import com.luminary.miners.miner.ResourceType;
import com.luminary.miners.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GUI for managing miners.
 */
public class MinerGUI implements Listener {

    private final LuminaryMiners plugin;
    private final Player player;
    private Inventory currentInventory;
    private GUIType currentType;
    private UUID selectedMinerId;

    public enum GUIType {
        MAIN_MENU,
        MY_MINERS,
        SHOP,
        MINER_DETAILS
    }

    public MinerGUI(LuminaryMiners plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu() {
        currentType = GUIType.MAIN_MENU;
        Inventory inv = Bukkit.createInventory(null, 27, colorize("&6&lMiners Menu"));

        // My Miners
        inv.setItem(11, createItem(Material.CHEST, "&e&lMy Miners",
            "&7View and manage your miners",
            "",
            "&aClick to view!"));

        // Shop
        inv.setItem(13, createItem(Material.GOLD_INGOT, "&6&lMiner Shop",
            "&7Purchase new miners",
            "",
            "&aClick to browse!"));

        // Collect All
        inv.setItem(15, createItem(Material.HOPPER, "&b&lCollect All",
            "&7Collect resources from all miners",
            "",
            "&aClick to collect!"));

        // Fill empty slots
        fillBorder(inv);

        currentInventory = inv;
        player.openInventory(inv);
    }

    public void openMyMiners() {
        currentType = GUIType.MY_MINERS;
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        int size = Math.max(27, ((data.getMinerCount() / 7) + 1) * 9 + 18);
        size = Math.min(size, 54);

        Inventory inv = Bukkit.createInventory(null, size, colorize("&e&lMy Miners"));

        int slot = 10;
        for (PlayerMiner miner : data.getMiners()) {
            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null) continue;

            if (slot >= size - 9) break;

            Material icon = type.getIcon();
            double stored = miner.getStoredResources();
            double maxStorage = type.getStorageCapacity(miner.getTier());
            double productionPerHour = type.getProductionPerHour(miner.getTier());
            ResourceType resource = type.getResourceType();

            List<String> lore = new ArrayList<>();
            lore.add("&7Tier: &f" + miner.getTier() + "/" + type.getMaxTier());
            lore.add("&7Resource: " + resource.getColor() + resource.getDisplayName());
            lore.add("");
            lore.add("&7Production: &f" + formatNumber(productionPerHour) + "/hr");
            lore.add("&7Storage: &f" + formatNumber(stored) + "/" + formatNumber(maxStorage));
            lore.add("&7Status: " + (miner.isActive() ? "&aActive" : "&cInactive"));
            lore.add("");
            lore.add("&eClick to manage!");

            ItemStack item = createItem(icon, type.getDisplayName(), lore.toArray(new String[0]));
            setItemData(item, miner.getMinerId().toString());
            inv.setItem(slot, item);

            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        // Back button
        inv.setItem(size - 5, createItem(Material.ARROW, "&c&lBack", "&7Return to main menu"));

        fillBorder(inv);
        currentInventory = inv;
        player.openInventory(inv);
    }

    public void openShop() {
        currentType = GUIType.SHOP;
        Inventory inv = Bukkit.createInventory(null, 54, colorize("&6&lMiner Shop"));

        int slot = 10;
        for (MinerType type : plugin.getMinerManager().getAllMinerTypes()) {
            if (slot >= 45) break;

            Material icon = type.getIcon();
            ResourceType resource = type.getResourceType();

            List<String> lore = new ArrayList<>();
            lore.add("&7" + type.getDescription());
            lore.add("");
            lore.add("&7Category: &f" + type.getCategory().getDisplayName());
            lore.add("&7Resource: " + resource.getColor() + resource.getDisplayName());
            lore.add("");
            lore.add("&7Production: &f" + formatNumber(type.getBaseProduction()) + "/hr");
            lore.add("&7Storage: &f" + formatNumber(type.getBaseStorage()));
            lore.add("&7Max Tier: &f" + type.getMaxTier());
            lore.add("");
            lore.add("&7Cost: &6" + formatNumber(type.getPurchaseCost()) + " " + type.getPurchaseCurrency());
            lore.add("");

            boolean canAfford = plugin.getEconomyHook().hasBalance(player, type.getPurchaseCurrency(), type.getPurchaseCost());
            if (canAfford) {
                lore.add("&aClick to purchase!");
            } else {
                lore.add("&cNot enough " + type.getPurchaseCurrency() + "!");
            }

            ItemStack item = createItem(icon, type.getDisplayName(), lore.toArray(new String[0]));
            setItemData(item, type.getId());
            inv.setItem(slot, item);

            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        // Back button
        inv.setItem(49, createItem(Material.ARROW, "&c&lBack", "&7Return to main menu"));

        fillBorder(inv);
        currentInventory = inv;
        player.openInventory(inv);
    }

    public void openMinerDetails(UUID minerId) {
        currentType = GUIType.MINER_DETAILS;
        selectedMinerId = minerId;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        PlayerMiner miner = data.getMiner(minerId);
        if (miner == null) {
            player.sendMessage(ChatColor.RED + "Miner not found!");
            return;
        }

        MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Invalid miner type!");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 45, colorize("&e&l" + ChatColor.stripColor(colorize(type.getDisplayName()))));

        // Info item
        double stored = miner.getStoredResources();
        double maxStorage = type.getStorageCapacity(miner.getTier());
        ResourceType resource = type.getResourceType();

        inv.setItem(13, createItem(type.getIcon(), type.getDisplayName(),
            "&7Tier: &f" + miner.getTier() + "/" + type.getMaxTier(),
            "&7Resource: " + resource.getColor() + resource.getDisplayName(),
            "",
            "&7Production: &f" + formatNumber(type.getProductionPerHour(miner.getTier())) + "/hr",
            "&7Storage: &f" + formatNumber(stored) + "/" + formatNumber(maxStorage),
            "&7Status: " + (miner.isActive() ? "&aActive" : "&cInactive")));

        // Collect button
        inv.setItem(29, createItem(Material.HOPPER, "&a&lCollect Resources",
            "&7Stored: &f" + formatNumber(stored),
            "",
            "&aClick to collect!"));

        // Upgrade button
        if (miner.getTier() < type.getMaxTier()) {
            var tierData = type.getTier(miner.getTier() + 1);
            double upgradeCost = tierData != null ? tierData.getUpgradeCost() : 0;
            String currency = tierData != null ? tierData.getUpgradeCurrency() : "tokens";

            boolean canUpgrade = plugin.getEconomyHook().hasBalance(player, currency, upgradeCost);

            inv.setItem(31, createItem(Material.ANVIL, "&6&lUpgrade",
                "&7Next Tier: &f" + (miner.getTier() + 1),
                "&7Cost: &6" + formatNumber(upgradeCost) + " " + currency,
                "",
                canUpgrade ? "&aClick to upgrade!" : "&cNot enough " + currency + "!"));
        } else {
            inv.setItem(31, createItem(Material.BARRIER, "&7&lMax Tier",
                "&7This miner is at max tier!"));
        }

        // Toggle active button
        inv.setItem(33, createItem(miner.isActive() ? Material.LIME_DYE : Material.GRAY_DYE,
            miner.isActive() ? "&a&lActive" : "&7&lInactive",
            "&7Toggle miner on/off",
            "",
            "&eClick to toggle!"));

        // Back button
        inv.setItem(40, createItem(Material.ARROW, "&c&lBack", "&7Return to my miners"));

        fillBorder(inv);
        currentInventory = inv;
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;
        if (!event.getInventory().equals(currentInventory)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (!clicked.hasItemMeta()) return;

        String itemData = getItemData(clicked);

        switch (currentType) {
            case MAIN_MENU:
                handleMainMenuClick(event.getSlot());
                break;

            case MY_MINERS:
                handleMyMinersClick(event.getSlot(), itemData);
                break;

            case SHOP:
                handleShopClick(event.getSlot(), itemData);
                break;

            case MINER_DETAILS:
                handleMinerDetailsClick(event.getSlot());
                break;
        }
    }

    private void handleMainMenuClick(int slot) {
        switch (slot) {
            case 11:
                openMyMiners();
                break;
            case 13:
                openShop();
                break;
            case 15:
                collectAll();
                break;
        }
    }

    private void handleMyMinersClick(int slot, String data) {
        if (slot == currentInventory.getSize() - 5) {
            openMainMenu();
            return;
        }

        if (data != null && !data.isEmpty()) {
            try {
                UUID minerId = UUID.fromString(data);
                openMinerDetails(minerId);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void handleShopClick(int slot, String data) {
        if (slot == 49) {
            openMainMenu();
            return;
        }

        if (data != null && !data.isEmpty()) {
            purchaseMiner(data);
        }
    }

    private void handleMinerDetailsClick(int slot) {
        if (slot == 40) {
            openMyMiners();
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        PlayerMiner miner = data.getMiner(selectedMinerId);
        if (miner == null) return;

        MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
        if (type == null) return;

        switch (slot) {
            case 29: // Collect
                collectFromMiner(miner, type);
                openMinerDetails(selectedMinerId);
                break;

            case 31: // Upgrade
                upgradeMiner(miner, type);
                openMinerDetails(selectedMinerId);
                break;

            case 33: // Toggle
                miner.setActive(!miner.isActive());
                data.markDirty();
                plugin.getPlayerDataManager().savePlayerData(player);
                player.sendMessage(ChatColor.YELLOW + "Miner is now " +
                    (miner.isActive() ? ChatColor.GREEN + "active" : ChatColor.RED + "inactive"));
                openMinerDetails(selectedMinerId);
                break;
        }
    }

    private void collectAll() {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        double tokensCollected = 0;
        double beaconsCollected = 0;
        double gemsCollected = 0;

        for (PlayerMiner miner : data.getMiners()) {
            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null) continue;

            double collected = miner.collectResources();
            if (collected > 0) {
                ResourceType resource = type.getResourceType();
                switch (resource) {
                    case TOKENS:
                        tokensCollected += collected;
                        break;
                    case BEACONS:
                        beaconsCollected += collected;
                        break;
                    case GEMS:
                        gemsCollected += collected;
                        break;
                }
            }
        }

        if (tokensCollected > 0) plugin.getEconomyHook().addBalance(player, "tokens", tokensCollected);
        if (beaconsCollected > 0) plugin.getEconomyHook().addBalance(player, "beacons", beaconsCollected);
        if (gemsCollected > 0) plugin.getEconomyHook().addBalance(player, "gems", gemsCollected);

        if (tokensCollected > 0 || beaconsCollected > 0 || gemsCollected > 0) {
            StringBuilder msg = new StringBuilder(ChatColor.GREEN + "Collected: ");
            if (tokensCollected > 0) msg.append(ChatColor.GOLD).append(formatNumber(tokensCollected)).append(" tokens ");
            if (beaconsCollected > 0) msg.append(ChatColor.AQUA).append(formatNumber(beaconsCollected)).append(" beacons ");
            if (gemsCollected > 0) msg.append(ChatColor.LIGHT_PURPLE).append(formatNumber(gemsCollected)).append(" gems");
            player.sendMessage(msg.toString().trim());
        } else {
            player.sendMessage(ChatColor.YELLOW + "Nothing to collect!");
        }

        plugin.getPlayerDataManager().savePlayerData(player);
        player.closeInventory();
    }

    private void collectFromMiner(PlayerMiner miner, MinerType type) {
        double collected = miner.collectResources();
        if (collected > 0) {
            ResourceType resource = type.getResourceType();
            plugin.getEconomyHook().addBalance(player, resource.getSingular(), collected);
            player.sendMessage(ChatColor.GREEN + "Collected " + resource.format(collected) + "!");
            plugin.getPlayerDataManager().savePlayerData(player);
        } else {
            player.sendMessage(ChatColor.YELLOW + "Nothing to collect yet!");
        }
    }

    private void upgradeMiner(PlayerMiner miner, MinerType type) {
        if (miner.getTier() >= type.getMaxTier()) {
            player.sendMessage(ChatColor.RED + "This miner is already at max tier!");
            return;
        }

        var tierData = type.getTier(miner.getTier() + 1);
        if (tierData == null) return;

        double cost = tierData.getUpgradeCost();
        String currency = tierData.getUpgradeCurrency();

        if (!plugin.getEconomyHook().withdraw(player, currency, cost)) {
            player.sendMessage(ChatColor.RED + "You don't have enough " + currency + "!");
            return;
        }

        miner.upgrade(type.getMaxTier());
        plugin.getPlayerDataManager().getPlayerData(player).markDirty();
        plugin.getPlayerDataManager().savePlayerData(player);
        player.sendMessage(ChatColor.GREEN + "Upgraded to Tier " + miner.getTier() + "!");
    }

    private void purchaseMiner(String typeId) {
        MinerType type = plugin.getMinerManager().getMinerType(typeId);
        if (type == null) return;

        // Check limits
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        int maxMiners = plugin.getConfigManager().getMaxMinersPerPlayer();
        int maxPerType = plugin.getConfigManager().getMaxMinersPerType();

        if (data.getMinerCount() >= maxMiners) {
            player.sendMessage(ChatColor.RED + "You have reached the maximum number of miners (" + maxMiners + ")!");
            return;
        }

        if (data.getMinerCountByType(typeId) >= maxPerType) {
            player.sendMessage(ChatColor.RED + "You can only have " + maxPerType + " of this miner type!");
            return;
        }

        // Check cost
        if (!plugin.getEconomyHook().withdraw(player, type.getPurchaseCurrency(), type.getPurchaseCost())) {
            player.sendMessage(ChatColor.RED + "You don't have enough " + type.getPurchaseCurrency() + "!");
            return;
        }

        // Add miner
        PlayerMiner miner = new PlayerMiner(type.getId());
        data.addMiner(miner);
        plugin.getPlayerDataManager().savePlayerData(player);

        player.sendMessage(ChatColor.GREEN + "Purchased " + colorize(type.getDisplayName()) + ChatColor.GREEN + "!");
        openShop();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(currentInventory)) {
            HandlerList.unregisterAll(this);
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(name));
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(colorize(line));
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setItemData(ItemStack item, String data) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(ChatColor.BLACK + data);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    private String getItemData(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                String last = lore.get(lore.size() - 1);
                if (last.startsWith(ChatColor.BLACK.toString())) {
                    return ChatColor.stripColor(last);
                }
            }
        }
        return null;
    }

    private void fillBorder(Inventory inv) {
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        int size = inv.getSize();

        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
        for (int i = size - 9; i < size; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
        for (int i = 0; i < size; i += 9) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
        for (int i = 8; i < size; i += 9) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String formatNumber(double number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000);
        } else {
            return String.format("%.0f", number);
        }
    }
}
