# BitWorld

**BitWorld** is a 2D top-down *Vampire Survivors*-like action game developed in Scala as a final project for the Object-Oriented Programming 101.2 course at HEI Valais

The game is built using the [`gdx2d`](https://hevs-isi.github.io/gdx2d/javadoc/) graphics library

---

## Gameplay

You control a player who must survive multiple waves of enemies and vanquish the dangerous endboss. Your character gains experience, levels up, and selects from a variety of abilities to aid in combat. Enemies increase in number and strength over time, culminating in challenging fights.

### Key Features
- **Player Abilities**: Choose from damage boosts, speed enhancements, special abilities like *Stinky* (area-of-effect attack), and more
- **Enemy Waves**: Enemies spawn and scale in difficulty using a dynamic wave manager
- **Weapons & Projectiles**: Automatically fired weapons with targeting and knockback logic
- **Level-Up System**: Gain experience and select abilities from a randomized upgrade menu
- **UI**: Custom health and XP bars with kill and level tracking

---

## Architecture

The main components of the game include:

- `Main`: Entry point of the application, handles game state and rendering
- `Player`: Handles player stats, movement, attacks, and upgrades
- `Enemy`: Represents AI enemies with basic targeting and collision
- `WaveManager`: Controls the progressive spawning of enemies
- `Ability`: Base class for all passive and active abilities
- `Weapon`: Handles projectile generation and direction logic
- `AbilityMenu`: Displays randomized ability choices when leveling up

---

## How to Run

1. Clone this repository
2. Make sure you have [Scala](https://www.scala-lang.org/download/) and [sbt](https://www.scala-sbt.org/download.html) installed
3. Run the Main via your IDE

The game runs in a 1920x1080 window.

---

## Credits

Developed by Dyumes and Skitaarii

---

https://github.com/user-attachments/assets/cf43e5f2-2a99-4a8b-8bf1-cc9bde326caf



## Screenshots

TODO
