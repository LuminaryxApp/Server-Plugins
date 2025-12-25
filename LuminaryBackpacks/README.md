# LuminaryBackpacks

Virtual backpacks that auto-collect mined blocks. No more full inventories while mining.

## Features

### Virtual Storage
Backpacks aren't items - they're virtual storage tied to your account. Open them with `/backpack` and see everything you've collected.

### Auto-Pickup
When you break blocks, they go straight to your backpack instead of dropping on the ground. No more inventory management while mining.

### Auto-Sell
Hit a button and sell everything in your backpack instantly. Or set it to auto-sell when the backpack fills up. Works with LuminaryEconomy for currency payouts.

### Tiers
Different backpack tiers with different capacities. Start small, upgrade to hold more.

## Commands

- `/backpack` or `/bp` - open your backpack
- `/backpack <tier>` - open a specific tier (if you own multiple)
- `/bpadmin give <player> <tier>` - give a backpack (admin)
- `/bpadmin upgrade <player>` - upgrade a player's backpack (admin)

## How it works

1. Mine blocks like normal
2. Blocks go into your backpack automatically (if auto-pickup is enabled)
3. When you're ready to sell, open the backpack and hit sell
4. Currency gets added to your LuminaryEconomy balance

## Config

- Define backpack tiers and their capacities
- Set which blocks can be stored
- Configure sell prices per block type
- Enable/disable auto-features by default

## Dependencies

**Required:** LuminaryCore

**Optional:** LuminaryEconomy, PlaceholderAPI
