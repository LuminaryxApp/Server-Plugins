# LuminaryCrates

Virtual crate system with spinning animations. No physical crate locations needed - everything runs through a GUI.

## Features

### Virtual Crates
Players open crates through a menu, not by clicking a block. Cleaner setup, no need to place crate locations around your server.

### Spin Animation
When opening a crate, players see a spinning animation of possible rewards before landing on their prize. Builds excitement.

### Key System
Crates require keys to open. Keys are virtual (not physical items), tracked per-player per-crate-type.

### Configurable Rewards
Set up whatever rewards you want:
- Commands (give items, currency, ranks, etc.)
- Custom display names and lore
- Rarity weights
- Guaranteed rewards at certain intervals

## Commands

- `/crates` - open the crates menu
- `/crates open <type>` - open a specific crate
- `/crates keys` - view your keys
- `/crates give <player> <crate> <amount>` - give keys (admin)
- `/crates reload` - reload config (admin)

## Config

Each crate type is defined in config:
- Display name and icon
- List of rewards with weights
- Spin animation settings
- Win messages

## Integration

Works with LuminaryEnchants - you can give enchanted pickaxes or enchant books as rewards. Also integrates with LuminaryRanks for giving rank rewards.

## Dependencies

**Required:** LuminaryCore

**Optional:** Vault, LuminaryEnchants
