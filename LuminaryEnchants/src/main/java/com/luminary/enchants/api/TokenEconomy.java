package com.luminary.enchants.api;

import java.util.UUID;

/**
 * Interface for token economy integration.
 * Implementations connect to LuminaryTokens or Vault.
 */
public interface TokenEconomy {

    /**
     * Get the token balance for a player.
     * @param playerId The player's UUID
     * @return The token balance
     */
    long get(UUID playerId);

    /**
     * Withdraw tokens from a player's balance.
     * @param playerId The player's UUID
     * @param amount The amount to withdraw
     * @param reason The reason for the withdrawal
     * @return true if successful, false if insufficient funds
     */
    boolean withdraw(UUID playerId, long amount, String reason);

    /**
     * Deposit tokens to a player's balance.
     * @param playerId The player's UUID
     * @param amount The amount to deposit
     * @param reason The reason for the deposit
     */
    void deposit(UUID playerId, long amount, String reason);

    /**
     * Check if the economy is available.
     * @return true if the economy can be used
     */
    boolean isAvailable();

    /**
     * Get the name of the economy provider.
     * @return Provider name (e.g., "LuminaryTokens", "Vault")
     */
    String getProviderName();
}
