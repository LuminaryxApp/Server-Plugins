# LuminaryMiners

AFK miners that generate resources while you're away. Think of them as little robots or golems that mine for you.

## Features

### Passive Income
Players can own miners that automatically generate resources over time. Even when they're offline, the miners keep working. Log back in and collect what they've gathered.

### Miner Types
Different tiers of miners with different speeds and capacities. Higher tier miners produce more and hold more before needing collection.

### Upgrades
Miners can be upgraded to improve:
- Mining speed
- Storage capacity
- Resource types they can mine

## Commands

- `/miners` - open the miners management GUI
- `/mineradmin` - admin commands for giving/managing miners

## How it works

1. Player obtains a miner (shop, crate, admin give, etc.)
2. Player places/activates the miner through the GUI
3. Miner starts generating resources based on its tier
4. Player collects resources when ready
5. Resources go to their balance or backpack

## Config

Customize miner tiers, generation rates, upgrade costs, and what resources each tier can produce.

## Dependencies

**Required:** LuminaryCore

**Optional:** LuminaryEconomy, PlaceholderAPI
