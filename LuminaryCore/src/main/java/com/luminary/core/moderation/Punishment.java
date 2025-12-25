package com.luminary.core.moderation;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

/**
 * Represents a punishment (ban, mute, warn).
 */
public class Punishment {

    public enum Type {
        BAN,
        MUTE,
        WARN,
        KICK
    }

    private final String id;
    private final Type type;
    private final UUID targetId;
    private final String targetName;
    private final UUID issuerId;
    private final String issuerName;
    private final String reason;
    private final long issuedAt;
    private final long duration; // -1 for permanent
    private final long expiresAt; // -1 for permanent
    private boolean active;
    private String revokedBy;
    private long revokedAt;
    private String revokeReason;

    public Punishment(String id, Type type, UUID targetId, String targetName,
                      UUID issuerId, String issuerName, String reason, long duration) {
        this.id = id;
        this.type = type;
        this.targetId = targetId;
        this.targetName = targetName;
        this.issuerId = issuerId;
        this.issuerName = issuerName;
        this.reason = reason;
        this.issuedAt = System.currentTimeMillis();
        this.duration = duration;
        this.expiresAt = duration < 0 ? -1 : (issuedAt + duration);
        this.active = true;
        this.revokedBy = null;
        this.revokedAt = 0;
        this.revokeReason = null;
    }

    public Punishment(ConfigurationSection section) {
        this.id = section.getString("id");
        this.type = Type.valueOf(section.getString("type", "BAN"));
        this.targetId = UUID.fromString(section.getString("target-id"));
        this.targetName = section.getString("target-name");

        String issuerIdStr = section.getString("issuer-id");
        this.issuerId = issuerIdStr != null && !issuerIdStr.equals("CONSOLE") ?
            UUID.fromString(issuerIdStr) : null;
        this.issuerName = section.getString("issuer-name", "Console");

        this.reason = section.getString("reason", "No reason provided");
        this.issuedAt = section.getLong("issued-at");
        this.duration = section.getLong("duration", -1);
        this.expiresAt = section.getLong("expires-at", -1);
        this.active = section.getBoolean("active", true);
        this.revokedBy = section.getString("revoked-by");
        this.revokedAt = section.getLong("revoked-at", 0);
        this.revokeReason = section.getString("revoke-reason");
    }

    public void save(ConfigurationSection section) {
        section.set("id", id);
        section.set("type", type.name());
        section.set("target-id", targetId.toString());
        section.set("target-name", targetName);
        section.set("issuer-id", issuerId != null ? issuerId.toString() : "CONSOLE");
        section.set("issuer-name", issuerName);
        section.set("reason", reason);
        section.set("issued-at", issuedAt);
        section.set("duration", duration);
        section.set("expires-at", expiresAt);
        section.set("active", active);
        if (revokedBy != null) {
            section.set("revoked-by", revokedBy);
            section.set("revoked-at", revokedAt);
            section.set("revoke-reason", revokeReason);
        }
    }

    public boolean isExpired() {
        if (!active) return true;
        if (expiresAt < 0) return false; // Permanent
        return System.currentTimeMillis() >= expiresAt;
    }

    public boolean isPermanent() {
        return duration < 0;
    }

    public long getRemainingTime() {
        if (expiresAt < 0) return -1;
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public void revoke(String revokedBy, String reason) {
        this.active = false;
        this.revokedBy = revokedBy;
        this.revokedAt = System.currentTimeMillis();
        this.revokeReason = reason;
    }

    // Getters
    public String getId() { return id; }
    public Type getType() { return type; }
    public UUID getTargetId() { return targetId; }
    public String getTargetName() { return targetName; }
    public UUID getIssuerId() { return issuerId; }
    public String getIssuerName() { return issuerName; }
    public String getReason() { return reason; }
    public long getIssuedAt() { return issuedAt; }
    public long getDuration() { return duration; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active && !isExpired(); }
    public String getRevokedBy() { return revokedBy; }
    public long getRevokedAt() { return revokedAt; }
    public String getRevokeReason() { return revokeReason; }
}
