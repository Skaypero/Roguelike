# Roguelike on Java + libGDX

A simple room-based roguelike using **libGDX**.

## Features

- 40x40 tile rooms.
- Main menu with generation mode selection:
  - `1` — random map generation,
  - `2` — predefined room templates from text files in `src/main/resources/maps`.
- Room content:
  - walls and doors,
  - multiple monsters,
  - multiple chests,
  - no traps.
- Inventory UI and item selection:
  - `Tab` / `Shift+Tab` to select item,
  - selected weapon affects attack,
  - `Q` uses selected consumable.
- Runtime-generated textures (no binary assets committed):
  - 64x64 source textures for tiles/entities,
  - room-specific visual themes.

## Controls

- `W/A/S/D` or arrows — move
- `F` — fight adjacent monster
- `E` — open nearby chest
- `Q` — use consumable
- `Esc` — back to menu

## Run

```bash
mvn exec:java
```

## Tests

```bash
mvn test
```
