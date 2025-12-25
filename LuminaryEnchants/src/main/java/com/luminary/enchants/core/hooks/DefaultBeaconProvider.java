package com.luminary.enchants.core.hooks;

import com.luminary.enchants.api.BeaconEffectProvider;

import java.util.UUID;

/**
 * Default beacon effect provider - returns default values when no beacon plugin is available.
 */
public class DefaultBeaconProvider implements BeaconEffectProvider {

    @Override
    public double multiplier(UUID playerId, String key) {
        return 1.0;
    }

    @Override
    public int level(UUID playerId, String key) {
        return 0;
    }

    @Override
    public boolean has(UUID playerId, String key) {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
