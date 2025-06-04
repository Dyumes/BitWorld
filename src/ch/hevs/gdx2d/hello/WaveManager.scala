package ch.hevs.gdx2d.hello

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import ch.hevs.gdx2d.desktop.PortableApplication

import java.lang.Math
import scala.util.Random

class WaveManager{
  private var waveNumber = 0
  // Models for enemies + boss mudry
  //var goblin : Enemy = new Enemy("gobelin", 0, 0, 100, 100, 250, 50, 30, 0)
  //var skeleton_distance : Enemy = new Enemy("skeleton distance", 0, 0, 50, 100, 300, 30, 20, 0)
  var orc : Enemy = new Enemy("orc", 0, 0, 200, 200, 200, 100, 50, 0)
  var skeleton: Enemy = new Enemy("skeleton", 0, 0, 50, 50, 350, 20, 15, 0)
  var mage: Enemy = new Enemy("mage", 0, 0, 100, 150, 250, 50, 30, 0)

  var boss_Mudry: Enemy = new Enemy("boss", 0, 0, 200, 200, 150, 5000, 80, 0)

  private var counterDt: Float = 2

  def globalEnemiesGeneration(enemies : ArrayBuffer[Enemy], player: Player, dt: Float): ArrayBuffer[Enemy] = {
    val radius = 1100
    val position = player.getPosition
    counterDt -= dt
    println(s"Counter Dt enemies : $counterDt")

    if(counterDt <= 0) {
      player.getLevel() match {
        case 1 =>
          for (en <- 0 to 1) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var goblin: Enemy = new Enemy("goblin", x, y, 100, 100, 250, 50, 30, en)
            enemies += goblin
          }
          counterDt = 2
        case 2 =>
          for (en <- 0 to 7) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var goblin: Enemy = new Enemy("goblin", x, y, 100, 100, 250, 50, 30, en)
            enemies += goblin
          }
          for (en <- 0 to 2) {
            val x = Random.nextFloat() * 2000
            val y = Random.nextFloat() * 2000
            var skeleton_distance: Enemy = new Enemy("skeleton distance", x, y, 50, 100, 300, 30, 20, en)
            enemies += skeleton_distance
          }
          counterDt = 2

        case 3 =>
        case 4 =>
        case 5 =>

        case _ =>
      }

    }
    return enemies
    }

  def update(enemies : ArrayBuffer[Enemy])  = {
    if(enemies.isEmpty) {
      waveNumber += 1
    }
  }

  def generateWave(): Int = {
    val count : Int = 5 * waveNumber
    count
  }
}