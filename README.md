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
<img width="963" alt="wave5_png" src="https://github.com/user-attachments/assets/e6e8b281-2721-4cdc-a91b-b858bc63d10b" />
<img width="959" alt="choose_weapon" src="https://github.com/user-attachments/assets/a40fed4e-1b0f-4882-8129-67629c42c6ef" />
<img width="961" alt="debug_activated" src="https://github.com/user-attachments/assets/0cd64f38-eefb-43af-a530-7e10e658f63c" />
<img width="221" alt="inventory" src="https://github.com/user-attachments/assets/08a60e45-ed60-494f-aa0e-5af8646cb37d" />

<img width="962" alt="choose_ability" src="https://github.com/user-attachments/assets/bbcc7882-963b-44c6-9c2b-8bcfed10492e" />
