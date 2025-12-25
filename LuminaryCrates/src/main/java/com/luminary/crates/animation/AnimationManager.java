package com.luminary.crates.animation;

import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.crate.Crate;
import com.luminary.crates.crate.CrateReward;
import com.luminary.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages crate opening animations with spinning GUI.
 */
public class AnimationManager {

    private final LuminaryCrates plugin;
    private final Map<UUID, CrateAnimation> activeAnimations = new HashMap<>();

    // Glass pane colors for decoration
    private static final Material[] GLASS_COLORS = {
            Material.RED_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE
    };

    public AnimationManager(LuminaryCrates plugin) {
        this.plugin = plugin;
    }

    /**
     * Start a crate opening animation for a player.
     */
    public void startAnimation(Player player, Crate crate, CrateReward finalReward) {
        if (isAnimating(player)) {
            return;
        }

        CrateAnimation animation = new CrateAnimation(player, crate, finalReward);
        activeAnimations.put(player.getUniqueId(), animation);
        animation.start();
    }

    /**
     * Check if a player is currently in an animation.
     */
    public boolean isAnimating(Player player) {
        return activeAnimations.containsKey(player.getUniqueId());
    }

    /**
     * Cancel an animation for a player.
     */
    public void cancelAnimation(Player player) {
        CrateAnimation animation = activeAnimations.remove(player.getUniqueId());
        if (animation != null) {
            animation.cancel();
        }
    }

    /**
     * Cancel all active animations (used on plugin disable).
     */
    public void cancelAll() {
        for (CrateAnimation animation : activeAnimations.values()) {
            animation.cancel();
        }
        activeAnimations.clear();
    }

    /**
     * Get the animation instance for a player.
     */
    public CrateAnimation getAnimation(Player player) {
        return activeAnimations.get(player.getUniqueId());
    }

    /**
     * Represents an active crate animation.
     */
    public class CrateAnimation {
        private final Player player;
        private final Crate crate;
        private final CrateReward finalReward;
        private final Inventory inventory;
        private final List<CrateReward> spinItems;
        private BukkitTask task;
        private int tick = 0;
        private int spinPosition = 0;
        private boolean finished = false;

        // Animation settings
        private static final int TOTAL_TICKS = 80; // 4 seconds at normal speed
        private static final int SLOW_START = 50; // Start slowing down here
        private static final int GUI_SIZE = 27; // 3 rows
        private static final int CENTER_SLOT = 13; // Center of row 2

        public CrateAnimation(Player player, Crate crate, CrateReward finalReward) {
            this.player = player;
            this.crate = crate;
            this.finalReward = finalReward;
            this.spinItems = generateSpinItems();
            this.inventory = createInventory();
        }

        /**
         * Generate the list of items that will spin, ending with the final reward.
         */
        private List<CrateReward> generateSpinItems() {
            List<CrateReward> items = new ArrayList<>();
            List<CrateReward> rewards = crate.getRewards();
            Random random = new Random();

            // Generate 50 random items, with the final reward at a specific position
            for (int i = 0; i < 50; i++) {
                items.add(rewards.get(random.nextInt(rewards.size())));
            }

            // Place the final reward at position 45 (will land on center)
            items.set(45, finalReward);

            return items;
        }

        /**
         * Create the animation GUI.
         */
        private Inventory createInventory() {
            String title = plugin.getConfigManager().getMessage("animation.title")
                    .replace("{crate}", crate.getDisplayName());
            Inventory inv = Bukkit.createInventory(new CrateAnimationHolder(this), GUI_SIZE,
                    TextUtil.colorize(title));

            // Fill borders with glass
            updateBorder(inv, 0);

            return inv;
        }

        /**
         * Update the border glass colors (for rainbow effect).
         */
        private void updateBorder(Inventory inv, int colorOffset) {
            // Top row (0-8)
            for (int i = 0; i < 9; i++) {
                if (i != 4) { // Skip indicator position
                    inv.setItem(i, createGlassPane(GLASS_COLORS[(i + colorOffset) % GLASS_COLORS.length]));
                }
            }

            // Bottom row (18-26)
            for (int i = 18; i < 27; i++) {
                if (i != 22) { // Skip indicator position
                    inv.setItem(i, createGlassPane(GLASS_COLORS[(i + colorOffset) % GLASS_COLORS.length]));
                }
            }

            // Selector indicators (pointing to center)
            ItemStack selector = createSelector();
            inv.setItem(4, selector);  // Top center
            inv.setItem(22, selector); // Bottom center
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
         * Create the selector arrow item.
         */
        private ItemStack createSelector() {
            ItemStack selector = new ItemStack(Material.ARROW);
            ItemMeta meta = selector.getItemMeta();
            if (meta != null) {
                meta.displayName(TextUtil.colorize("&e▼ &6Winner &e▼"));
                selector.setItemMeta(meta);
            }
            return selector;
        }

        /**
         * Start the animation.
         */
        public void start() {
            player.openInventory(inventory);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || finished) {
                        cancel();
                        return;
                    }

                    // Calculate spin speed (slows down over time)
                    int delay = calculateDelay();

                    if (tick >= TOTAL_TICKS) {
                        // Animation complete
                        finish();
                        cancel();
                        return;
                    }

                    // Only update on certain ticks based on speed
                    if (tick % delay == 0) {
                        updateSpinDisplay();
                        spinPosition++;

                        // Play tick sound
                        if (tick < SLOW_START) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f);
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 1.2f);
                        }
                    }

                    // Update rainbow border every 4 ticks
                    if (tick % 4 == 0) {
                        updateBorder(inventory, tick / 4);
                    }

                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        /**
         * Calculate the delay between spins (increases as animation progresses).
         */
        private int calculateDelay() {
            if (tick < 20) return 1;      // Fast
            if (tick < 35) return 2;      // Medium-fast
            if (tick < 50) return 3;      // Medium
            if (tick < 65) return 5;      // Medium-slow
            if (tick < 75) return 8;      // Slow
            return 12;                     // Very slow (final)
        }

        /**
         * Update the spinning item display.
         */
        private void updateSpinDisplay() {
            // Display 7 items in the middle row (slots 9-17)
            // Center item (slot 13) is the "selected" one
            for (int i = 0; i < 9; i++) {
                int itemIndex = (spinPosition + i) % spinItems.size();
                CrateReward reward = spinItems.get(itemIndex);

                if (i == 0 || i == 8) {
                    // Side slots - empty or glass
                    inventory.setItem(9 + i, createGlassPane(Material.GRAY_STAINED_GLASS_PANE));
                } else {
                    inventory.setItem(9 + i, reward.createDisplayItem());
                }
            }
        }

        /**
         * Finish the animation and give the reward.
         */
        private void finish() {
            finished = true;
            activeAnimations.remove(player.getUniqueId());

            // Clear the spinning items and show final reward prominently
            for (int i = 9; i < 18; i++) {
                if (i != CENTER_SLOT) {
                    inventory.setItem(i, createGlassPane(Material.LIME_STAINED_GLASS_PANE));
                }
            }
            inventory.setItem(CENTER_SLOT, finalReward.createDisplayItem());

            // Play win sound
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);

            // Give the reward after a short delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    finalReward.give(player);

                    // Send win message
                    String message = plugin.getConfigManager().getMessage("crate.won")
                            .replace("{reward}", finalReward.getDisplayName())
                            .replace("{crate}", crate.getDisplayName());
                    player.sendMessage(TextUtil.colorize(message));

                    // Broadcast if configured
                    if (shouldBroadcast(finalReward)) {
                        broadcastWin(player, crate, finalReward);
                    }

                    // Close inventory after showing result
                    Bukkit.getScheduler().runTaskLater(plugin, (Runnable) player::closeInventory, 40L);
                }
            }, 20L);
        }

        /**
         * Cancel the animation.
         */
        public void cancel() {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
            finished = true;
            if (player.isOnline()) {
                player.closeInventory();
            }
        }

        /**
         * Check if the reward should be broadcasted.
         */
        private boolean shouldBroadcast(CrateReward reward) {
            return reward.getRarity().ordinal() >= plugin.getConfigManager().getBroadcastMinRarity().ordinal();
        }

        /**
         * Broadcast a win to all players.
         */
        private void broadcastWin(Player winner, Crate crate, CrateReward reward) {
            String message = plugin.getConfigManager().getMessage("crate.broadcast")
                    .replace("{player}", winner.getName())
                    .replace("{reward}", reward.getDisplayName())
                    .replace("{crate}", crate.getDisplayName())
                    .replace("{rarity}", reward.getRarity().getColoredName());

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(TextUtil.colorize(message));
            }
        }

        public Player getPlayer() {
            return player;
        }

        public Crate getCrate() {
            return crate;
        }

        public boolean isFinished() {
            return finished;
        }
    }
}
