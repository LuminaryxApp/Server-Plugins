package com.luminary.enchants.core.hooks;

import com.luminary.enchants.api.TokenEconomy;

import java.util.UUID;

/**
 * Disabled token economy - used when no economy plugin is available.
 */
public class DisabledTokenEconomy implements TokenEconomy {

    @Override
    public long get(UUID playerId) {
        return 0;
    }

    @Override
    public boolean withdraw(UUID playerId, long amount, String reason) {
        return false;
    }

    @Override
    public void deposit(UUID playerId, long amount, String reason) {
        // No-op
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getProviderName() {
        return "Disabled";
    }
}
