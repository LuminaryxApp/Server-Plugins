package com.luminary.ranks.rank;

import com.luminary.ranks.LuminaryRanks;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages all ranks, prestiges, and rebirths.
 */
public class RankManager {

    private final LuminaryRanks plugin;

    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final List<Rank> orderedRanks = new ArrayList<>();
    private final Map<Integer, Prestige> prestiges = new LinkedHashMap<>();
    private final Map<Integer, Rebirth> rebirths = new LinkedHashMap<>();

    private int maxPrestige = 10;
    private int maxRebirth = 5;

    public RankManager(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    public void loadRanks() {
        ranks.clear();
        orderedRanks.clear();
        prestiges.clear();
        rebirths.clear();

        FileConfiguration config = plugin.getConfigManager().getRanksConfig();

        // Load ranks (A-Z)
        ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
        if (ranksSection != null) {
            int order = 0;
            for (String rankId : ranksSection.getKeys(false)) {
                ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankId);
                if (rankSection != null) {
                    Rank rank = new Rank(rankId.toUpperCase(), rankSection, order++);
                    ranks.put(rank.getId(), rank);
                    orderedRanks.add(rank);
                }
            }
        } else {
            // Generate default A-Z ranks
            generateDefaultRanks();
        }

        // Load prestiges
        ConfigurationSection prestigeSection = config.getConfigurationSection("prestiges");
        if (prestigeSection != null) {
            maxPrestige = prestigeSection.getInt("max-level", 10);
            ConfigurationSection levelsSection = prestigeSection.getConfigurationSection("levels");
            if (levelsSection != null) {
                for (String levelStr : levelsSection.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        ConfigurationSection section = levelsSection.getConfigurationSection(levelStr);
                        if (section != null) {
                            prestiges.put(level, new Prestige(level, section));
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Generate missing prestiges with defaults
        for (int i = 1; i <= maxPrestige; i++) {
            if (!prestiges.containsKey(i)) {
                prestiges.put(i, createDefaultPrestige(i));
            }
        }

        // Load rebirths
        ConfigurationSection rebirthSection = config.getConfigurationSection("rebirths");
        if (rebirthSection != null) {
            maxRebirth = rebirthSection.getInt("max-level", 5);
            ConfigurationSection levelsSection = rebirthSection.getConfigurationSection("levels");
            if (levelsSection != null) {
                for (String levelStr : levelsSection.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        ConfigurationSection section = levelsSection.getConfigurationSection(levelStr);
                        if (section != null) {
                            rebirths.put(level, new Rebirth(level, section));
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Generate missing rebirths with defaults
        for (int i = 1; i <= maxRebirth; i++) {
            if (!rebirths.containsKey(i)) {
                rebirths.put(i, createDefaultRebirth(i));
            }
        }

        plugin.getLogger().info("Loaded " + ranks.size() + " ranks");
    }

    private void generateDefaultRanks() {
        String[] colors = {"&7", "&8", "&f", "&a", "&2", "&b", "&3", "&9", "&1", "&d", "&5", "&e", "&6", "&c"};

        for (char c = 'A'; c <= 'Z'; c++) {
            int index = c - 'A';
            String colorCode = colors[index % colors.length];
            long cost = (index + 1) * 1000L;

            String id = String.valueOf(c);
            String displayName = colorCode + "Rank " + c;
            String prefix = colorCode + "[" + c + "]";

            Rank rank = new Rank(id, displayName, prefix, cost, index);
            ranks.put(id, rank);
            orderedRanks.add(rank);
        }
    }

    private Prestige createDefaultPrestige(int level) {
        String color = getPrestigeColor(level);
        return new Prestige(
                level,
                color + "Prestige " + level,
                color + "[P" + level + "]",
                100000L * level * level,
                1.0 + (level * 0.1)
        );
    }

    private Rebirth createDefaultRebirth(int level) {
        String color = getRebirthColor(level);
        return new Rebirth(
                level,
                color + "&lRebirth " + level,
                color + "[R" + level + "]",
                1000000L * level * level,
                -1, // Requires max prestige
                1.0 + (level * 0.25)
        );
    }

    private String getPrestigeColor(int level) {
        String[] colors = {"&d", "&5", "&9", "&3", "&b", "&a", "&2", "&e", "&6", "&c"};
        return colors[(level - 1) % colors.length];
    }

    private String getRebirthColor(int level) {
        String[] colors = {"&c", "&4", "&6", "&e", "&f"};
        return colors[(level - 1) % colors.length];
    }

    // Rank methods
    public Rank getRank(String id) {
        return ranks.get(id.toUpperCase());
    }

    public Rank getFirstRank() {
        return orderedRanks.isEmpty() ? null : orderedRanks.get(0);
    }

    public Rank getNextRank(String currentRankId) {
        Rank current = getRank(currentRankId);
        if (current == null) return getFirstRank();

        int nextOrder = current.getOrder() + 1;
        if (nextOrder < orderedRanks.size()) {
            return orderedRanks.get(nextOrder);
        }
        return null; // At max rank
    }

    public Rank getLastRank() {
        return orderedRanks.isEmpty() ? null : orderedRanks.get(orderedRanks.size() - 1);
    }

    public boolean isMaxRank(String rankId) {
        Rank last = getLastRank();
        return last != null && last.getId().equals(rankId.toUpperCase());
    }

    public List<Rank> getAllRanks() {
        return Collections.unmodifiableList(orderedRanks);
    }

    public int getRankCount() {
        return orderedRanks.size();
    }

    // Prestige methods
    public Prestige getPrestige(int level) {
        return prestiges.get(level);
    }

    public Prestige getNextPrestige(int currentLevel) {
        int nextLevel = currentLevel + 1;
        if (nextLevel <= maxPrestige) {
            return prestiges.get(nextLevel);
        }
        return null;
    }

    public boolean isMaxPrestige(int level) {
        return level >= maxPrestige;
    }

    public int getMaxPrestige() {
        return maxPrestige;
    }

    public List<Prestige> getAllPrestiges() {
        return new ArrayList<>(prestiges.values());
    }

    // Rebirth methods
    public Rebirth getRebirth(int level) {
        return rebirths.get(level);
    }

    public Rebirth getNextRebirth(int currentLevel) {
        int nextLevel = currentLevel + 1;
        if (nextLevel <= maxRebirth) {
            return rebirths.get(nextLevel);
        }
        return null;
    }

    public boolean isMaxRebirth(int level) {
        return level >= maxRebirth;
    }

    public int getMaxRebirth() {
        return maxRebirth;
    }

    public List<Rebirth> getAllRebirths() {
        return new ArrayList<>(rebirths.values());
    }

    // Multiplier calculation
    public double getMultiplier(int prestigeLevel, int rebirthLevel) {
        double multiplier = 1.0;

        // Add prestige multiplier
        Prestige prestige = getPrestige(prestigeLevel);
        if (prestige != null) {
            multiplier *= prestige.getMultiplier();
        }

        // Add rebirth multiplier (permanent)
        Rebirth rebirth = getRebirth(rebirthLevel);
        if (rebirth != null) {
            multiplier *= rebirth.getPermanentMultiplier();
        }

        return multiplier;
    }
}
