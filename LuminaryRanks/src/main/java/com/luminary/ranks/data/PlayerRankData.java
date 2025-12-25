package com.luminary.ranks.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

/**
 * Stores a player's rank progression data.
 */
public class PlayerRankData {

    private final UUID playerId;
    private String playerName;

    private String currentRank;
    private int prestigeLevel;
    private int rebirthLevel;

    // Stats
    private int totalRankups;
    private int totalPrestiges;
    private int totalRebirths;
    private long tokensSpent;

    private boolean dirty = false;

    public PlayerRankData(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentRank = "A";
        this.prestigeLevel = 0;
        this.rebirthLevel = 0;
        this.totalRankups = 0;
        this.totalPrestiges = 0;
        this.totalRebirths = 0;
        this.tokensSpent = 0;
    }

    public PlayerRankData(UUID playerId, ConfigurationSection section) {
        this.playerId = playerId;
        this.playerName = section.getString("name", "Unknown");
        this.currentRank = section.getString("rank", "A");
        this.prestigeLevel = section.getInt("prestige", 0);
        this.rebirthLevel = section.getInt("rebirth", 0);
        this.totalRankups = section.getInt("stats.total-rankups", 0);
        this.totalPrestiges = section.getInt("stats.total-prestiges", 0);
        this.totalRebirths = section.getInt("stats.total-rebirths", 0);
        this.tokensSpent = section.getLong("stats.tokens-spent", 0);
    }

    public void save(ConfigurationSection section) {
        section.set("name", playerName);
        section.set("rank", currentRank);
        section.set("prestige", prestigeLevel);
        section.set("rebirth", rebirthLevel);
        section.set("stats.total-rankups", totalRankups);
        section.set("stats.total-prestiges", totalPrestiges);
        section.set("stats.total-rebirths", totalRebirths);
        section.set("stats.tokens-spent", tokensSpent);
        dirty = false;
    }

    // Rank methods
    public void rankUp(String newRank, long cost) {
        this.currentRank = newRank;
        this.totalRankups++;
        this.tokensSpent += cost;
        this.dirty = true;
    }

    // Prestige - resets rank to A
    public void prestige(long cost) {
        this.prestigeLevel++;
        this.currentRank = "A";
        this.totalPrestiges++;
        this.tokensSpent += cost;
        this.dirty = true;
    }

    // Rebirth - resets rank and prestige
    public void rebirth(long cost) {
        this.rebirthLevel++;
        this.currentRank = "A";
        this.prestigeLevel = 0;
        this.totalRebirths++;
        this.tokensSpent += cost;
        this.dirty = true;
    }

    // Getters
    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        this.dirty = true;
    }

    public String getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(String rank) {
        this.currentRank = rank;
        this.dirty = true;
    }

    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    public void setPrestigeLevel(int level) {
        this.prestigeLevel = level;
        this.dirty = true;
    }

    public int getRebirthLevel() {
        return rebirthLevel;
    }

    public void setRebirthLevel(int level) {
        this.rebirthLevel = level;
        this.dirty = true;
    }

    public int getTotalRankups() {
        return totalRankups;
    }

    public int getTotalPrestiges() {
        return totalPrestiges;
    }

    public int getTotalRebirths() {
        return totalRebirths;
    }

    public long getTokensSpent() {
        return tokensSpent;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
