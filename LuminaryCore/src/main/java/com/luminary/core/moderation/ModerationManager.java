package com.luminary.core.moderation;

import com.luminary.core.LuminaryCore;
import com.luminary.core.util.MessageUtil;
import com.luminary.core.util.TimeUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all punishments (bans, mutes, warns).
 */
public class ModerationManager {

    private final LuminaryCore plugin;
    private final File punishmentsFolder;

    // Active punishments cache
    private final Map<UUID, Punishment> activeBans = new ConcurrentHashMap<>();
    private final Map<UUID, Punishment> activeMutes = new ConcurrentHashMap<>();
    private final Map<UUID, List<Punishment>> warnings = new ConcurrentHashMap<>();

    // Frozen players
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();

    public ModerationManager(LuminaryCore plugin) {
        this.plugin = plugin;
        this.punishmentsFolder = new File(plugin.getDataFolder(), "punishments");
        if (!punishmentsFolder.exists()) {
            punishmentsFolder.mkdirs();
        }
        loadPunishments();
    }

    public void reload() {
        activeBans.clear();
        activeMutes.clear();
        warnings.clear();
        loadPunishments();
    }

    private void loadPunishments() {
        File[] files = punishmentsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            UUID playerId = UUID.fromString(file.getName().replace(".yml", ""));

            // Load bans
            ConfigurationSection bansSection = config.getConfigurationSection("bans");
            if (bansSection != null) {
                for (String key : bansSection.getKeys(false)) {
                    ConfigurationSection section = bansSection.getConfigurationSection(key);
                    if (section != null) {
                        Punishment punishment = new Punishment(section);
                        if (punishment.isActive()) {
                            activeBans.put(playerId, punishment);
                        }
                    }
                }
            }

            // Load mutes
            ConfigurationSection mutesSection = config.getConfigurationSection("mutes");
            if (mutesSection != null) {
                for (String key : mutesSection.getKeys(false)) {
                    ConfigurationSection section = mutesSection.getConfigurationSection(key);
                    if (section != null) {
                        Punishment punishment = new Punishment(section);
                        if (punishment.isActive()) {
                            activeMutes.put(playerId, punishment);
                        }
                    }
                }
            }

            // Load warnings
            ConfigurationSection warnsSection = config.getConfigurationSection("warns");
            if (warnsSection != null) {
                List<Punishment> playerWarns = new ArrayList<>();
                for (String key : warnsSection.getKeys(false)) {
                    ConfigurationSection section = warnsSection.getConfigurationSection(key);
                    if (section != null) {
                        playerWarns.add(new Punishment(section));
                    }
                }
                if (!playerWarns.isEmpty()) {
                    warnings.put(playerId, playerWarns);
                }
            }
        }

        plugin.getLogger().info("Loaded " + activeBans.size() + " active bans, " +
            activeMutes.size() + " active mutes, " + warnings.size() + " players with warnings.");
    }

    public void saveAll() {
        // Save is handled per-player, but we can force save all
        Set<UUID> allPlayers = new HashSet<>();
        allPlayers.addAll(activeBans.keySet());
        allPlayers.addAll(activeMutes.keySet());
        allPlayers.addAll(warnings.keySet());

        for (UUID playerId : allPlayers) {
            savePlayerPunishments(playerId);
        }
    }

    private void savePlayerPunishments(UUID playerId) {
        File file = new File(punishmentsFolder, playerId.toString() + ".yml");
        YamlConfiguration config = file.exists() ?
            YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

        // Save active ban
        Punishment ban = activeBans.get(playerId);
        if (ban != null) {
            ConfigurationSection section = config.createSection("bans." + ban.getId());
            ban.save(section);
        }

        // Save active mute
        Punishment mute = activeMutes.get(playerId);
        if (mute != null) {
            ConfigurationSection section = config.createSection("mutes." + mute.getId());
            mute.save(section);
        }

        // Save warnings
        List<Punishment> playerWarns = warnings.get(playerId);
        if (playerWarns != null) {
            config.set("warns", null);
            for (Punishment warn : playerWarns) {
                ConfigurationSection section = config.createSection("warns." + warn.getId());
                warn.save(section);
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save punishments for " + playerId + ": " + e.getMessage());
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // ========== BAN METHODS ==========

    public Punishment ban(UUID targetId, String targetName, CommandSender issuer, String reason, long duration) {
        UUID issuerId = issuer instanceof Player ? ((Player) issuer).getUniqueId() : null;
        String issuerName = issuer.getName();

        Punishment punishment = new Punishment(generateId(), Punishment.Type.BAN,
            targetId, targetName, issuerId, issuerName, reason, duration);

        activeBans.put(targetId, punishment);
        savePlayerPunishments(targetId);

        // Kick if online
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            String kickMessage = buildBanMessage(punishment);
            target.kickPlayer(kickMessage);
        }

        // Broadcast
        broadcastPunishment(punishment, "ban");

        return punishment;
    }

    public boolean unban(UUID targetId, String revokedBy, String reason) {
        Punishment ban = activeBans.remove(targetId);
        if (ban != null) {
            ban.revoke(revokedBy, reason);
            savePlayerPunishments(targetId);
            return true;
        }
        return false;
    }

    public boolean isBanned(UUID playerId) {
        Punishment ban = activeBans.get(playerId);
        if (ban != null && ban.isExpired()) {
            activeBans.remove(playerId);
            return false;
        }
        return ban != null;
    }

    public Punishment getBan(UUID playerId) {
        Punishment ban = activeBans.get(playerId);
        if (ban != null && ban.isExpired()) {
            activeBans.remove(playerId);
            return null;
        }
        return ban;
    }

    public String buildBanMessage(Punishment ban) {
        String template = plugin.getConfigManager().getMessage("ban-screen");
        return MessageUtil.colorize(template
            .replace("{reason}", ban.getReason())
            .replace("{duration}", TimeUtil.formatDuration(ban.getDuration()))
            .replace("{remaining}", TimeUtil.formatRemaining(ban.getExpiresAt()))
            .replace("{issuer}", ban.getIssuerName())
            .replace("{id}", ban.getId()));
    }

    // ========== MUTE METHODS ==========

    public Punishment mute(UUID targetId, String targetName, CommandSender issuer, String reason, long duration) {
        UUID issuerId = issuer instanceof Player ? ((Player) issuer).getUniqueId() : null;
        String issuerName = issuer.getName();

        Punishment punishment = new Punishment(generateId(), Punishment.Type.MUTE,
            targetId, targetName, issuerId, issuerName, reason, duration);

        activeMutes.put(targetId, punishment);
        savePlayerPunishments(targetId);

        // Notify if online
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            MessageUtil.send(target, plugin.getConfigManager().getMessage("muted",
                "{reason}", reason,
                "{duration}", TimeUtil.formatDuration(duration)));
        }

        // Broadcast to staff
        broadcastPunishment(punishment, "mute");

        return punishment;
    }

    public boolean unmute(UUID targetId, String revokedBy, String reason) {
        Punishment mute = activeMutes.remove(targetId);
        if (mute != null) {
            mute.revoke(revokedBy, reason);
            savePlayerPunishments(targetId);

            // Notify if online
            Player target = Bukkit.getPlayer(targetId);
            if (target != null) {
                MessageUtil.send(target, plugin.getConfigManager().getMessage("unmuted"));
            }
            return true;
        }
        return false;
    }

    public boolean isMuted(UUID playerId) {
        Punishment mute = activeMutes.get(playerId);
        if (mute != null && mute.isExpired()) {
            activeMutes.remove(playerId);
            return false;
        }
        return mute != null;
    }

    public Punishment getMute(UUID playerId) {
        Punishment mute = activeMutes.get(playerId);
        if (mute != null && mute.isExpired()) {
            activeMutes.remove(playerId);
            return null;
        }
        return mute;
    }

    // ========== WARN METHODS ==========

    public Punishment warn(UUID targetId, String targetName, CommandSender issuer, String reason) {
        UUID issuerId = issuer instanceof Player ? ((Player) issuer).getUniqueId() : null;
        String issuerName = issuer.getName();

        Punishment punishment = new Punishment(generateId(), Punishment.Type.WARN,
            targetId, targetName, issuerId, issuerName, reason, -1);

        warnings.computeIfAbsent(targetId, k -> new ArrayList<>()).add(punishment);
        savePlayerPunishments(targetId);

        // Notify if online
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            MessageUtil.send(target, plugin.getConfigManager().getMessage("warned",
                "{reason}", reason,
                "{issuer}", issuerName));
        }

        // Broadcast to staff
        broadcastPunishment(punishment, "warn");

        // Check for auto-punish on warn threshold
        checkWarnThreshold(targetId, targetName);

        return punishment;
    }

    public List<Punishment> getWarnings(UUID playerId) {
        return warnings.getOrDefault(playerId, new ArrayList<>());
    }

    public int getActiveWarningCount(UUID playerId) {
        List<Punishment> warns = warnings.get(playerId);
        if (warns == null) return 0;
        return (int) warns.stream().filter(Punishment::isActive).count();
    }

    private void checkWarnThreshold(UUID targetId, String targetName) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        int threshold = config.getInt("moderation.warn-threshold", 3);
        String action = config.getString("moderation.warn-threshold-action", "tempban 1d");

        int activeWarns = getActiveWarningCount(targetId);
        if (activeWarns >= threshold) {
            // Parse action
            if (action.startsWith("tempban ")) {
                String duration = action.substring(8);
                long durationMs = TimeUtil.parseTime(duration);
                if (durationMs > 0) {
                    ban(targetId, targetName, Bukkit.getConsoleSender(),
                        "Reached " + threshold + " warnings", durationMs);
                }
            } else if (action.equals("ban")) {
                ban(targetId, targetName, Bukkit.getConsoleSender(),
                    "Reached " + threshold + " warnings", -1);
            }
        }
    }

    // ========== KICK METHODS ==========

    public void kick(Player target, CommandSender issuer, String reason) {
        String issuerName = issuer.getName();

        String kickMessage = MessageUtil.colorize(plugin.getConfigManager().getMessage("kick-screen",
            "{reason}", reason,
            "{issuer}", issuerName));

        target.kickPlayer(kickMessage);

        // Log the kick
        Punishment punishment = new Punishment(generateId(), Punishment.Type.KICK,
            target.getUniqueId(), target.getName(),
            issuer instanceof Player ? ((Player) issuer).getUniqueId() : null,
            issuerName, reason, 0);

        // Broadcast
        broadcastPunishment(punishment, "kick");
    }

    // ========== FREEZE METHODS ==========

    public void freeze(UUID playerId) {
        frozenPlayers.add(playerId);
    }

    public void unfreeze(UUID playerId) {
        frozenPlayers.remove(playerId);
    }

    public boolean isFrozen(UUID playerId) {
        return frozenPlayers.contains(playerId);
    }

    public Set<UUID> getFrozenPlayers() {
        return frozenPlayers;
    }

    // ========== UTILITY METHODS ==========

    private void broadcastPunishment(Punishment punishment, String type) {
        String message = plugin.getConfigManager().getMessage("staff-broadcast-" + type,
            "{player}", punishment.getTargetName(),
            "{issuer}", punishment.getIssuerName(),
            "{reason}", punishment.getReason(),
            "{duration}", TimeUtil.formatDuration(punishment.getDuration()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("luminarycore.staff")) {
                MessageUtil.send(player, message);
            }
        }
        // Also log to console
        plugin.getLogger().info(MessageUtil.stripColor(message));
    }

    public List<Punishment> getHistory(UUID playerId) {
        List<Punishment> history = new ArrayList<>();

        // Add current/past bans
        Punishment ban = activeBans.get(playerId);
        if (ban != null) history.add(ban);

        // Add current/past mutes
        Punishment mute = activeMutes.get(playerId);
        if (mute != null) history.add(mute);

        // Add all warnings
        history.addAll(getWarnings(playerId));

        // Sort by date
        history.sort((a, b) -> Long.compare(b.getIssuedAt(), a.getIssuedAt()));

        return history;
    }
}
