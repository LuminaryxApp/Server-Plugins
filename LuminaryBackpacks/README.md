# LuminaryBackpacks

**Virtual backpacks with auto-pickup and auto-sell.** No more full inventories while mining. Blocks go straight to your backpack and can be sold with one click.

Built for prison servers on Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Backpack GUI](images/backpack-gui.png) -->
<!-- ![Auto-Pickup in Action](images/auto-pickup.png) -->
<!-- ![Sell Confirmation](images/sell-confirm.png) -->
<!-- ![Upgrade Menu](images/upgrade-menu.png) -->

*Screenshots coming soon - showing the backpack interface, auto features, and upgrade system.*

---

## Why I Built This

Mining with a full inventory sucks. You're constantly stopping to sell, missing drops, managing slots. Backpacks fix all of that. Mine for as long as you want, sell everything at once when you're done.

---

## Features

### Virtual Storage
Backpacks aren't physical items - they're tied to your account. Can't lose them, can't drop them, always accessible.

### Auto-Pickup
When enabled, mined blocks skip your inventory entirely and go straight to your backpack. No more drops on the ground.

### Auto-Sell
Two options:
- **Manual**: Open backpack, click sell, get paid
- **Automatic**: Sells contents when backpack fills up

### Tiered Capacity
Different backpack tiers hold different amounts:

| Tier | Capacity |
|------|----------|
| Starter | 1,000 blocks |
| Bronze | 5,000 blocks |
| Silver | 25,000 blocks |
| Gold | 100,000 blocks |
| Diamond | 500,000 blocks |

### Block Type Storage
Backpacks track blocks by type. You can see exactly how many of each ore you have:
```
Coal Ore: 1,523
Iron Ore: 847
Diamond Ore: 42
...
```

---

## Backpack Preview

<!-- ![Backpack interface showing stored blocks](images/backpack-preview.png) -->

```
┌─────────────────────────────────────┐
│         Your Backpack               │
│         [====----] 45% Full         │
├─────────────────────────────────────┤
│  Coal Ore      x1,523   ($1,523)    │
│  Iron Ore      x847     ($4,235)    │
│  Gold Ore      x156     ($1,560)    │
│  Diamond Ore   x42      ($4,200)    │
├─────────────────────────────────────┤
│  Total Value: $11,518               │
│                                     │
│  [SELL ALL]        [Auto-Sell: ON]  │
└─────────────────────────────────────┘
```

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/backpack` or `/bp` | Open your backpack | `luminarybackpacks.use` |
| `/bp sell` | Sell all contents | `luminarybackpacks.use` |
| `/bp autopickup` | Toggle auto-pickup | `luminarybackpacks.autopickup` |
| `/bp autosell` | Toggle auto-sell | `luminarybackpacks.autosell` |
| `/bpadmin give <player> <tier>` | Give backpack tier | `luminarybackpacks.admin` |
| `/bpadmin upgrade <player>` | Upgrade player's backpack | `luminarybackpacks.admin` |
| `/bpadmin clear <player>` | Clear backpack contents | `luminarybackpacks.admin` |

---

## Installation

1. Install **LuminaryCore** (required)
2. Install **LuminaryEconomy** (for selling)
3. Drop `LuminaryBackpacks-1.0.0.jar` into plugins
4. Configure tiers and prices in `config.yml`
5. Decide how players unlock higher tiers

---

## Configuration Example

```yaml
tiers:
  starter:
    display-name: "&7Starter Backpack"
    capacity: 1000

  bronze:
    display-name: "&6Bronze Backpack"
    capacity: 5000
    upgrade-cost: 50000

  silver:
    display-name: "&fSilver Backpack"
    capacity: 25000
    upgrade-cost: 250000

prices:
  COBBLESTONE: 1
  COAL_ORE: 5
  IRON_ORE: 15
  GOLD_ORE: 50
  DIAMOND_ORE: 100
  EMERALD_ORE: 150
```

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **HashMap-based storage** for block counts
- **Event-driven auto-pickup**

---

## Project Structure

```
LuminaryBackpacks/
├── src/main/java/com/luminary/backpacks/
│   ├── LuminaryBackpacks.java     # Main plugin class
│   ├── command/                   # Commands
│   ├── config/                    # Config management
│   ├── data/                      # Player backpack data
│   ├── gui/                       # Backpack interface
│   └── listener/                  # Block break -> auto pickup
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## Auto-Pickup Flow

```java
@EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();

    if (!hasAutoPickup(player)) return;

    // Cancel normal drops
    event.setDropItems(false);

    // Add to backpack instead
    backpack.addBlocks(block.getType(), amount);

    // Check auto-sell threshold
    if (hasAutoSell(player) && backpack.isFull()) {
        sellAll(player);
    }
}
```

---

## What I Learned

Intercepting block drops efficiently without causing lag was the main challenge. Also learned about designing upgrade progressions that feel meaningful - the jump from 1k to 500k capacity needs to feel earned.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **LuminaryEconomy** - Sells blocks for currency
- **LuminaryShop** - Uses same price list
- **LuminaryMines** - Perfect combo with private mines
- [View all plugins](#)

---

## License

Portfolio project.
