# LuminaryEnchants

Custom enchantments for pickaxes. Built specifically for prison mining - these aren't your vanilla enchants.

## Features

### Pickaxe-Only Enchants
These enchants only work on pickaxes and are designed for mining. Things like:
- Explosive - breaks blocks in a radius
- Laser - breaks blocks in a line
- Jackhammer - breaks entire layers
- Speed boosts, auto-smelt, fortune multipliers, etc.

### Enchant Menu
Players open `/enchants` or `/pe` to see available enchants and upgrade them. Each enchant has multiple levels with increasing power and cost.

### Token/Currency Based
Enchants cost tokens (or whatever currency you configure). Higher levels cost more.

### Mine Region Protection
The enchants are smart about where they break blocks. If LuminaryMines is installed, enchant effects won't break blocks outside the mine region. Your ladders and walls stay safe.

## Commands

- `/pickenchants` or `/pe` - open the enchant menu
- `/pe reload` - reload config (admin)
- `/pe apply <enchant> <level> [player]` - force apply an enchant (admin)
- `/pe clear [player]` - clear all enchants from a pickaxe (admin)

## Config

Define your own enchants or modify the defaults:
- Max levels
- Cost per level
- Effect radius/power
- Proc chance (if applicable)

## How it works

When you break a block with an enchanted pickaxe, the enchants have a chance to proc (activate). Different enchants do different things - some break extra blocks, some give bonus drops, some speed up mining.

## Dependencies

**Required:** LuminaryCore

**Optional:** LuminaryMines (for region protection), Vault, WorldGuard
