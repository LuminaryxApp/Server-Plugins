# LuminaryRanks

Prison rank progression system. Players work their way through A-Z ranks, then prestige, then rebirth for permanent bonuses.

## How it works

### Ranks (A-Z)
Players start at rank A and work their way up to Z. Each rank costs more than the last. When you hit Z, you can prestige.

### Prestige
Prestiging resets your rank back to A but gives you a prestige level. Higher prestige = better multipliers and rewards. Keep prestiging until you're ready to rebirth.

### Rebirth
The big reset. Rebirthing resets EVERYTHING - ranks and prestige - but gives permanent bonuses that stick around forever. These bonuses apply to all future progression.

## Commands

- `/rankup` - rank up to the next letter
- `/prestige` - prestige when you hit rank Z
- `/rebirth` - rebirth when you're ready for the permanent bonuses
- `/ranks` - view all ranks and your progress

Admin commands available with `/ranks admin` for setting player ranks, giving prestiges, etc.

## Config

The config lets you customize:
- Cost formula for each rank tier
- Prestige requirements and rewards
- Rebirth requirements and permanent bonuses
- Display names, prefixes, and colors for each rank/prestige/rebirth level

## Integration

- Uses LuminaryEconomy for rank costs (if installed)
- Shows up on the LuminaryEconomy scoreboard
- Can give LuminaryCrates keys as rankup rewards

## Dependencies

**Required:** LuminaryCore

**Optional:** LuminaryEconomy, LuminaryCrates, PlaceholderAPI
