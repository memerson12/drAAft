# drAAft

A Minecraft 1.16.1 mod that makes various gameplay adjustments for a streamlined/ more consistent All Advancement
speedrun. Ideal for competitive events

## Features

### World Generation

- Increased Ancient Debris generation in the Nether
    - Ancient Debris spawns are now 2 attempts with baseline 16, spread 8, one with size 5 and one with size 4.
    - This does not change the optimal y-level for debris mining.
- Igloos always generate with basements (was 50%)
- Thunder happens more often and is standardized per seed
    - Thunder will now occur every 10 - 45 minutes (was 10 - 150 minutes)
    - Rain will occur 1 - 7 minutes before thunder starts
    - This guarantees at least two 3+ minute thunders in 1.5 hours

### Mob Changes

- Animals can now spawn on Podzol blocks (in addition to grass)
    - This is intended for pandas in jungles but has side effects in other places such as mega taigas.
    - Bamboo Jungle and Bamboo Jungle Hills have a 30% chance to attempt a passive mob spawn attempt per chunk (was
      10%).
- Drowned:
    - Chance of spawning with Trident 18.75% (was 6.25%)
    - Chance of spawning with Fishing Rod 11.25% (was 3.75%)
    - Chance of spawning with Nautilus Shell 9% (was 3%).
- Phantoms:
    - Spawn more frequently (15-60 second cooldown, down from 60-120 seconds)
    - Require less time without sleep to spawn (10 minutes, down from 60 minutes)
    - Always spawn at least 2 phantoms on Hard difficulty
    - he local difficulty multiplier of 3.0F has been removed, so the spawn attempt should succeed 3x as often
- Ender Dragon:
    - Fly-away height will always choose the lowest possible block. This means the dragon will not fly until it hits the
      height of the terrain + 20, which is usually around y-80.
    - Dragon instant perch and straight node / diagonal node selection standardized based on the world seed.
- Endermite spawn chance per thrown pearl is now 7% (was 5%).
    - This changes the amount of pearls needed for a 99% chance of getting an Endermite to 64 (was 90).

### Item Changes

- Elytra durability increased to 22,824 (from default)
    - This is to prevent losing to Unbreaking RNG.

## Installation

1. Install Fabric Loader 0.16.13 or later for Minecraft 1.16.1
2. Download the mod JAR file
3. Place the JAR file in your mods folder
4. Launch Minecraft with Fabric loader

## Dependencies

- Minecraft 1.16.1
- Fabric Loader ≥0.16.13

## License

This project is licensed under CC0 1.0 Universal. See the LICENSE file for details.
