package com.luminary.crates.gui;

import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.crate.Crate;
import com.luminary.crates.crate.CrateReward;
import com.luminary.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages crate preview GUIs.
 */
public class PreviewManager {

    private final LuminaryCrates plugin;

    private static final int ROWS = 6;
    private static final int SIZE = ROWS * 9;
    private static final int ITEMS_PER_PAGE = 28; // 4 rows of 7 items

    public PreviewManager(LuminaryCrates plugin) {
        this.plugin = plugin;
    }

    /**
     * Open a crate preview for a player.
     */
    public void openPreview(Player player, Crate crate) {
        openPreview(player, crate, 0);
    }

    /**
     * Open a crate preview at a specific page.
     */
    public void openPreview(Player player, Crate crate, int page) {
        List<CrateReward> rewards = crate.getRewards();
        int totalPages = (int) Math.ceil((double) rewards.size() / ITEMS_PER_PAGE);
        page = Math.max(0, Math.min(page, totalPages - 1));

        PreviewHolder holder = new PreviewHolder(crate, page, totalPages);
        String title = plugin.getConfigManager().getMessage("preview.title")
                .replace("{crate}", crate.getDisplayName())
                .replace("{page}", String.valueOf(page + 1))
                .replace("{pages}", String.valueOf(totalPages));

        Inventory inventory = Bukkit.createInventory(holder, SIZE, TextUtil.colorize(title));
        holder.setInventory(inventory);

        // Fill borders with glass
        fillBorder(inventory, crate);

        // Add reward items
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, rewards.size());

        int slot = 10; // Start at row 2, column 2
        for (int i = startIndex; i < endIndex; i++) {
            // Skip border slots
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            if (slot >= SIZE - 9) break;

            CrateReward reward = rewards.get(i);
            inventory.setItem(slot, reward.createDisplayItem());
            slot++;
        }

        // Add navigation buttons
        addNavigationButtons(inventory, holder);

        player.openInventory(inventory);
    }

    /**
     * Fill the border with glass panes.
     */
    private void fillBorder(Inventory inventory, Crate crate) {
        Material glassMaterial = getGlassForTier(crate.getTier());
        ItemStack glass = createGlassPane(glassMaterial);

        // Top row
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
        }

        // Bottom row
        for (int i = SIZE - 9; i < SIZE; i++) {
            inventory.setItem(i, glass);
        }

        // Left and right columns
        for (int i = 9; i < SIZE - 9; i += 9) {
            inventory.setItem(i, glass);
            inventory.setItem(i + 8, glass);
        }
    }

    /**
     * Get glass pane color based on crate tier.
     */
    private Material getGlassForTier(com.luminary.crates.crate.CrateTier tier) {
        return switch (tier) {
            case COMMON -> Material.WHITE_STAINED_GLASS_PANE;
            case UNCOMMON -> Material.LIME_STAINED_GLASS_PANE;
            case RARE -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case EPIC -> Material.PURPLE_STAINED_GLASS_PANE;
            case LEGENDARY -> Material.ORANGE_STAINED_GLASS_PANE;
            case MYTHIC -> Material.MAGENTA_STAINED_GLASS_PANE;
        };
    }

    /**
     * Create a glass pane item.
     */
    private ItemStack createGlassPane(Material material) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            glass.setItemMeta(meta);
        }
        return glass;
    }

    /**
     * Add navigation buttons to the preview GUI.
     */
    private void addNavigationButtons(Inventory inventory, PreviewHolder holder) {
        // Back button (bottom left area)
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(TextUtil.colorize("&c&lClose"));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.colorize("&7Click to close the preview"));
            backMeta.lore(lore);
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(holder.getBackSlot(), backButton);

        // Previous page button
        if (holder.hasPrevPage()) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.displayName(TextUtil.colorize("&e&lPrevious Page"));
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.colorize("&7Click to go to page " + holder.getPage()));
                prevMeta.lore(lore);
                prevButton.setItemMeta(prevMeta);
            }
            inventory.setItem(holder.getPrevPageSlot(), prevButton);
        }

        // Next page button
        if (holder.hasNextPage()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.displayName(TextUtil.colorize("&e&lNext Page"));
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.colorize("&7Click to go to page " + (holder.getPage() + 2)));
                nextMeta.lore(lore);
                nextButton.setItemMeta(nextMeta);
            }
            inventory.setItem(holder.getNextPageSlot(), nextButton);
        }

        // Info item (center bottom)
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(TextUtil.colorize("&6&lCrate Information"));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.colorize("&7Total Rewards: &e" + holder.getCrate().getRewards().size()));
            lore.add(TextUtil.colorize("&7Tier: " + holder.getCrate().getTier().getColoredName()));
            lore.add(Component.empty());
            lore.add(TextUtil.colorize("&eRight-click the crate with a key to open!"));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(49, infoItem);
    }
}
