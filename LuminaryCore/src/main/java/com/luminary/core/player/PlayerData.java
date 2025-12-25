package com.luminary.core.player;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

/**
 * Core player data stored by LuminaryCore.
 */
public class PlayerData {

    private final UUID playerId;
    private String lastName;
    private String lastIp;
    private long firstJoin;
    private long lastJoin;
    private long lastQuit;
    private long playTime; // Total play time in milliseconds
    private int loginCount;
    private boolean dirty;

    // Session tracking
    private transient long sessionStart;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.lastName = "";
        this.lastIp = "";
        this.firstJoin = System.currentTimeMillis();
        this.lastJoin = System.currentTimeMillis();
        this.lastQuit = 0;
        this.playTime = 0;
        this.loginCount = 1;
        this.dirty = true;
        this.sessionStart = System.currentTimeMillis();
    }

    public PlayerData(UUID playerId, ConfigurationSection section) {
        this.playerId = playerId;
        this.lastName = section.getString("last-name", "");
        this.lastIp = section.getString("last-ip", "");
        this.firstJoin = section.getLong("first-join", System.currentTimeMillis());
        this.lastJoin = section.getLong("last-join", System.currentTimeMillis());
        this.lastQuit = section.getLong("last-quit", 0);
        this.playTime = section.getLong("play-time", 0);
        this.loginCount = section.getInt("login-count", 1);
        this.dirty = false;
        this.sessionStart = System.currentTimeMillis();
    }

    public void save(ConfigurationSection section) {
        section.set("last-name", lastName);
        section.set("last-ip", lastIp);
        section.set("first-join", firstJoin);
        section.set("last-join", lastJoin);
        section.set("last-quit", lastQuit);
        section.set("play-time", playTime);
        section.set("login-count", loginCount);
        dirty = false;
    }

    // Session management
    public void startSession(String name, String ip) {
        this.lastName = name;
        this.lastIp = ip;
        this.lastJoin = System.currentTimeMillis();
        this.sessionStart = System.currentTimeMillis();
        this.loginCount++;
        this.dirty = true;
    }

    public void endSession() {
        this.lastQuit = System.currentTimeMillis();
        this.playTime += (System.currentTimeMillis() - sessionStart);
        this.dirty = true;
    }

    // Getters
    public UUID getPlayerId() {
        return playerId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLastIp() {
        return lastIp;
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public long getLastJoin() {
        return lastJoin;
    }

    public long getLastQuit() {
        return lastQuit;
    }

    public long getPlayTime() {
        return playTime;
    }

    public long getTotalPlayTime() {
        // Include current session
        return playTime + (System.currentTimeMillis() - sessionStart);
    }

    public int getLoginCount() {
        return loginCount;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }
}
