# LuminaryCore

The backbone of the Luminary plugin suite. Every other Luminary plugin depends on this one, so make sure it's installed first.

## What it does

LuminaryCore handles all the shared stuff that the other plugins need - config management, utilities, text formatting, and a full moderation system built right in.

### Moderation Commands

All your standard staff tools:
- `/ban`, `/tempban`, `/unban` - permanent and temporary bans
- `/mute`, `/tempmute`, `/unmute` - silence troublemakers
- `/kick` - boot someone off the server
- `/warn` - issue warnings
- `/freeze` - stop a player in their tracks (useful for catching cheaters)
- `/history` - check a player's punishment record

### Staff Features
- `/vanish` - go invisible to regular players
- `/staffchat` - private chat channel for staff
- `/broadcast` - send server-wide announcements
- `/maintenance` - lock the server for maintenance (staff can still join)

## Installation

Just drop the jar in your plugins folder. It loads at startup before other plugins so everything hooks in properly.

## Config

Run the server once and it'll generate the config files. Most settings are pretty self-explanatory. Message formats support color codes and placeholders.

## Permissions

Everything defaults to OP, but you can adjust per-permission if you use a groups plugin. Check plugin.yml for the full list.

## Dependencies

- Paper 1.20+

That's pretty much it. This plugin is meant to stay out of your way while providing the foundation for everything else.
