package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.bitmaps.BitmapImage

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import ch.hevs.gdx2d.desktop.PortableApplication

import java.lang.Math
import scala.util.Random

class WaveManager{
  private var waveNumber = 0
  private var waveBoss : Boolean = false
  // Models for enemies + boss mudry
  //var goblin : Enemy = new Enemy("gobelin", 0, 0, 100, 100, 250, 50, 30, 0, 10)
  //var skeleton_distance : Enemy = new Enemy("skeleton distance", 0, 0, 50, 100, 300, 30, 20, 0)
  //var orc : Enemy = new Enemy("orc", 0, 0, 200, 200, 200, 100, 50, 0)
  //var skeleton: Enemy = new Enemy("skeleton", 0, 0, 50, 50, 350, 20, 15, 0)
  //var mage: Enemy = new Enemy("mage", 0, 0, 100, 150, 250, 50, 30, 0)

  //var boss_Mudry: Enemy = new Enemy("boss", 0, 0, 300, 300, 150, 5000, 80, 0)

  private var counterDt: Float = 2

  def globalEnemiesGeneration(enemies : ArrayBuffer[Enemy], player: Player, dt: Float): ArrayBuffer[Enemy] = {
    val radius = 1100
    val position = player.getPosition

    counterDt -= dt
    println(s"Counter Dt enemies : $counterDt")

    if(counterDt <= 0) {
      player.getLevel() match {
        case 1 =>
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          counterDt = 2 * Random.nextFloat()
        case 2 =>
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          for (en <- 0 to 1) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var skeleton_distance: Enemy = new Enemy("skeleton distance", x, y, 50, 60, 150, 30, 20, en)
            enemies += skeleton_distance
          }
          counterDt = 2 * Random.nextFloat()
        case 3 =>
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var skeleton_distance: Enemy = new Enemy("skeleton distance", x, y, 50, 60, 150, 30, 20, en)
            enemies += skeleton_distance
          }
          for (en <- 0 to 1) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var orc : Enemy = new Enemy("orc", x, y, 60, 60, 150, 200, 50, 0)
            enemies += orc
          }
          counterDt = 2 * Random.nextFloat()
        case 4 =>
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var skeleton_distance: Enemy = new Enemy("skeleton distance", x, y, 50, 60, 150, 30, 20, en)
            enemies += skeleton_distance
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var orc : Enemy = new Enemy("orc", x, y, 60, 60, 150, 200, 50, 0)
            enemies += orc
          }
          for (en <- 0 to 1) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var skeleton: Enemy = new Enemy("skeleton", x, y, 50, 60, 180, 20, 15, 0)
            enemies += skeleton
          }
          counterDt = 2 * Random.nextFloat()
        case 5 =>
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var goblin: Enemy = new Enemy("goblin", x, y, 45, 45, 180, 50, 30, en)
            enemies += goblin
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var skeleton_distance: Enemy = new Enemy("skeleton distance", x, y, 50, 60, 150, 30, 20, en)
            enemies += skeleton_distance
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var orc : Enemy = new Enemy("orc", x, y, 60, 60, 150, 200, 50, 0)
            enemies += orc
          }
          for (en <- 0 to 2) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var skeleton: Enemy = new Enemy("skeleton", x, y, 50, 60, 350, 20, 15, 0)
            enemies += skeleton
          }
          for (en <- 0 to 1) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * en / 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var mage: Enemy = new Enemy("mage", x, y, 60, 60, 180, 50, 30, 0)
            enemies += mage
          }
          counterDt = 2 * Random.nextFloat()
        case 6 =>
          println("BOSS WAVE")
          if (waveBoss == false){
            enemies.clear()
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat + 2 * Math.PI.toFloat * 10
            val range : Float = Random.nextFloat() * 500 + radius
            val x = position.x + range * Math.cos(angle).toFloat
            val y = position.y + range * Math.sin(angle).toFloat
            var boss_Mudry: Enemy = new Enemy("boss", x, y, 300, 300, 150, 5000, 80, 0)
            enemies += boss_Mudry
          }
          waveBoss = true
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



}