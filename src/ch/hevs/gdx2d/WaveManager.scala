/**
 * WaveManager Class
 * -----------------
 * This class manages the spawning and progression of enemy waves in the game.
 *
 * Key Features:
 * - Tracks the current wave number and boss wave state.
 * - Controls the timing for enemy generation using a countdown timer.
 * - Generates different types and numbers of enemies based on the player's level.
 * - Handles special boss wave logic.
 * - Provides an update method to increment the wave number when all enemies are cleared.
 */

package ch.hevs.gdx2d

import ch.hevs.gdx2d.Entity.{Enemy, Player}
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class WaveManager {
  // --- State Variables ---
  private var waveNumber = 0                // Current wave number
  private var waveBoss: Boolean = false     // Is the boss wave active?
  private var counterDt: Float = 2          // Countdown timer for enemy generation

  // --- Main Method: Enemy Generation ---
  /**
   * Generates enemies based on the player's level and the countdown timer.
   * Spawns different enemy types and numbers for each wave.
   * Handles boss wave logic and resets the timer after each spawn.
   */
  def globalEnemiesGeneration(enemies: ArrayBuffer[Enemy], player: Player, dt: Float): ArrayBuffer[Enemy] = {
    val radius = 1100
    val position = player.getPosition

    counterDt -= dt
    println(s"Counter Dt enemies : $counterDt")

    if (counterDt <= 0) {
      player.getLevel() match {
        case 1 =>
          // Spawn 3 goblins
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          counterDt = 2 * Random.nextFloat()
        case 2 =>
          // Spawn 4 goblins
          for (en <- 0 to 3) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          counterDt = 2 * Random.nextFloat()
        case 3 =>
          // Spawn 3 goblins and 2 orcs
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          for (en <- 0 to 1) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val orc: Enemy = new Enemy("orc", x, y, 60, 60, 150, 200, 50, 0)
            enemies += orc
          }
          counterDt = 2 * Random.nextFloat()
        case 4 =>
          // Spawn 3 goblins, 3 orcs, 3 skeletons
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val orc: Enemy = new Enemy("orc", x, y, 60, 60, 150, 200, 50, 0)
            enemies += orc
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val skeleton: Enemy = new Enemy("skeleton", x, y, 50, 60, 200, 20, 15, 0)
            enemies += skeleton
          }
          counterDt = 2 * Random.nextFloat()
        case 5 =>
          // Spawn up to 500 enemies: goblins, orcs, skeletons, wizards
          if (enemies.length <= 500) {
            for (en <- 0 to 2) {
              val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
              val range: Float = Random.nextFloat() * 500 + radius
              val x = position.x + range * Math.cos(angle).toFloat
              val y = position.y + range * Math.sin(angle).toFloat
              val goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
              enemies += goblin
            }
            for (en <- 0 to 2) {
              val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
              val range: Float = Random.nextFloat() * 500 + radius
              val x = position.x + range * Math.cos(angle).toFloat
              val y = position.y + range * Math.sin(angle).toFloat
              val orc: Enemy = new Enemy("orc", x, y, 60, 60, 150, 200, 50, 0)
              enemies += orc
            }
            for (en <- 0 to 2) {
              val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
              val range: Float = Random.nextFloat() * 500 + radius
              val x = position.x + range * Math.cos(angle).toFloat
              val y = position.y + range * Math.sin(angle).toFloat
              val skeleton: Enemy = new Enemy("skeleton", x, y, 50, 60, 200, 20, 15, 0)
              enemies += skeleton
            }
            for (en <- 0 to 1) {
              val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
              val range: Float = Random.nextFloat() * 500 + radius
              val x = position.x + range * Math.cos(angle).toFloat
              val y = position.y + range * Math.sin(angle).toFloat
              val wizard: Enemy = new Enemy("wizard", x, y, 60, 60, 180, 50, 30, 0)
              enemies += wizard
            }
            counterDt = 2 * Random.nextFloat()
          }
        case 6 =>
          // Boss wave logic
          println("BOSS WAVE")
          if (!waveBoss) {
            enemies.clear()
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * 10
            val range: Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            val boss_Mudry: Enemy = new Enemy("boss", x, y, 256, 256, 150, 5000, 5000, 0)
            boss_Mudry.isSpawned = true
            enemies += boss_Mudry
          }
          waveBoss = true
        case _ =>
        // No action for other levels
      }
    }
    enemies
  }

  // --- Update Method ---
  /**
   * Increments the wave number when all enemies are cleared.
   */
  def update(enemies: ArrayBuffer[Enemy]) = {
    if (enemies.isEmpty) {
      waveNumber += 1
    }
  }
}