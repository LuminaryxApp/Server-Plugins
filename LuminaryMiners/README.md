# LuminaryMiners

**AFK mining bots that work while you're away.** Players deploy miners that passively generate resources over time. Log back in and collect the profits.

Built for prison servers on Paper 1.20+.

---

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Miners Menu](images/miners-menu.png) -->
<!-- ![Miner Working Animation](images/miner-active.png) -->
<!-- ![Collection Interface](images/collect-resources.png) -->

*Screenshots coming soon - showing the miner management GUI, active miners, and resource collection.*

---

## Why I Built This

Not everyone can grind 24/7. Miners give casual players a way to progress while offline. It's also a great monetization option - sell better miners in your store. Plus they're just fun to collect and upgrade.

---

## Features

### Passive Income
Miners generate resources automatically:
- Works while online or offline
- Different tiers produce different amounts
- Collect whenever you want

### Miner Tiers
Multiple tiers with increasing power:
- **Basic Miner** - Slow but free to start
- **Iron Miner** - Moderate production
- **Diamond Miner** - Fast generation
- **Netherite Miner** - Premium tier

### Upgrade System
Improve your miners:
- **Speed** - Faster resource generation
- **Capacity** - Hold more before collection
- **Efficiency** - Better resource types

### Collection System
Resources pile up until you collect them. If a miner hits capacity, it stops until you empty it. Encourages regular logins without being punishing.

---

## How It Works

```
┌─────────────────┐
│  Player owns    │
│  3 miners       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Miners generate │
│ resources/hour  │
│                 │
│ Basic:  100/hr  │
│ Iron:   250/hr  │
│ Diamond: 500/hr │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Player collects │
│ via /miners GUI │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Resources added │
│ to balance      │
└─────────────────┘
```

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/miners` | Open miner management GUI | `luminaryminers.use` |
| `/mineradmin give <player> <tier>` | Give a miner | `luminaryminers.admin` |
| `/mineradmin list <player>` | View player's miners | `luminaryminers.admin` |
| `/mineradmin clear <player>` | Remove all miners | `luminaryminers.admin` |

---

## Installation

1. Install **LuminaryCore** (required)
2. Drop `LuminaryMiners-1.0.0.jar` into plugins
3. Configure miner tiers in `config.yml`
4. Set up generation rates and capacities
5. Decide how players obtain miners (shop, crates, etc.)

---

## Configuration Example

```yaml
miners:
  basic:
    display-name: "&7Basic Miner"
    icon: IRON_PICKAXE
    generation-rate: 100  # per hour
    capacity: 1000

  iron:
    display-name: "&fIron Miner"
    icon: IRON_BLOCK
    generation-rate: 250
    capacity: 2500

  diamond:
    display-name: "&bDiamond Miner"
    icon: DIAMOND_BLOCK
    generation-rate: 500
    capacity: 5000
```

---

## Tech Stack

- **Java 17**
- **Paper API 1.20.1**
- **Persistent data** for offline accumulation

---

## Project Structure

```
LuminaryMiners/
├── src/main/java/com/luminary/miners/
│   ├── LuminaryMiners.java        # Main plugin class
│   ├── command/                   # Commands
│   ├── config/                    # Config management
│   ├── gui/                       # Miner management GUI
│   ├── player/                    # Player miner data
│   └── task/                      # Generation scheduler
└── src/main/resources/
    ├── plugin.yml
    ├── config.yml
    └── miners.yml
```

---

## Offline Calculation

When a player logs in, the plugin calculates how much time passed and credits resources accordingly. This happens instantly on join so there's no waiting.

```java
long offlineTime = now - lastSeen;
long hoursOffline = offlineTime / 3600000;
double generated = miner.getRate() * hoursOffline;
// Cap at capacity
generated = Math.min(generated, miner.getCapacity());
```

---

## What I Learned

The offline calculation logic was interesting - handling edge cases like server restarts, time zone issues, and ensuring nothing gets lost. Also learned about designing progression systems that feel rewarding without being pay-to-win.

---

## Part of the Luminary Suite

- **LuminaryCore** - Required foundation
- **LuminaryEconomy** - Currency integration
- **LuminaryCrates** - Give miners as rewards
- [View all plugins](#)

---

## License

Portfolio project.
