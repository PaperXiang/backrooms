# Backrooms test server configs

This directory stores versioned copies of external plugin configuration used by the local test server at `D:\dev\backrooms\devserver`.

## Sync targets

- `server-configs/CraftEngine/resources/backrooms/` -> `devserver/plugins/CraftEngine/resources/backrooms/`
- `server-configs/TAB/config.yml` -> `devserver/plugins/TAB/config.yml`
- `server-configs/TAB/groups.yml` -> `devserver/plugins/TAB/groups.yml`

## Reload commands after syncing

- BackroomsCore runtime config: `/br reload`
- CraftEngine resource/config/model pack: `/ce reload all`
- TAB display config: `/tab reload`

Jar/code changes still need a server restart. `/br reload` reloads BackroomsCore YAML and runtime registries only.

## Current map-generation status

Level generation is not implemented yet. Current MVP uses existing worlds managed by Paper/Multiverse and configured by `levels/*.yml`.

The next gameplay-critical step is a transition system and a room-generation prototype:

1. Level 0 stairwell/door interaction -> Level 1 random spawn point.
2. Level 1 stairwell interaction -> lobby spawn.
3. Room template marker blocks -> later replacement with CraftEngine blocks.

Random spawn points are implemented now so admin teleports and future transitions can distribute players across a Level instead of always sending everyone to one fixed coordinate.
