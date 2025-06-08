package ch.hevs.gdx2d.hello

import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.math.{Interpolation, Vector2}
import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.desktop.PortableApplication
import com.badlogic.gdx.Input.Keys
import ch.hevs.gdx2d.hello.Player
import ch.hevs.gdx2d.hello.WaveManager
import ch.hevs.gdx2d.hello.Ability
import ch.hevs.gdx2d.hello.AbilityMenu
import com.badlogic.gdx.graphics.Color

import java.security.DrbgParameters.Capability
import scala.util.Random
import scala.collection.mutable.ArrayBuffer

object HelloWorldScala {

  def main(args: Array[String]): Unit = {
    new HelloWorldScala
  }
}

class HelloWorldScala extends PortableApplication(1920, 1080) {
  private var imgBitmap: BitmapImage = null
  private var background : BitmapImage = null

  private val player : Player = new Player(100, 100, 100, 100, 300, 200, 20, 0)
  private val enemies : ArrayBuffer[Enemy] = ArrayBuffer[Enemy]()
  private val Wave = new WaveManager

  private val Abilties : ArrayBuffer[Ability] = ArrayBuffer[Ability]()
  val hpBoost : healthBoost = new healthBoost(player)
  val dmgBoost : damageBoost = new damageBoost(player)
  val prcBoost : piercingBoost = new piercingBoost(player)
  val spdBoost : speedBoost = new speedBoost(player)
  val asBoost : attackSpeedBoost = new attackSpeedBoost(player)
  val Stinky : stinky = new stinky(player, enemies)
  Abilties.append(hpBoost)
  Abilties.append(dmgBoost)
  Abilties.append(prcBoost)
  Abilties.append(spdBoost)
  Abilties.append(asBoost)
  Abilties.append(Stinky)
  private var showAbilityMenu = true

  val menu = new AbilityMenu(player, List(
    dmgBoost, spdBoost, hpBoost, asBoost, prcBoost, Stinky
  ))


  override def onInit(): Unit = {
    setTitle("BitWorld")
    // Load a custom image (or from the lib "res/lib/icon64.png")
    imgBitmap = new BitmapImage("data/images/ISC_logo.png")
    background = new BitmapImage("data/images/placeholder_background.png")


  }


  /*

  def generateEnemies(nbr: Int): Unit = {    for (ennemy <- 0 until nbr){
      val x = Random.nextFloat() * getWindowWidth
      val y = Random.nextFloat() * getWindowHeight
      val en = new Enemy(x, y, 100, 100, 300, 100, 10, ennemy)
      enemies += en
    }

  }

   */
  /**
   * Some animation related variables
   */
  private var direction: Int = 1
  private var currentTime: Float = 0
  final private val ANIMATION_LENGTH: Float = 2f // Animation length (in seconds)
  final private val MIN_ANGLE: Float = -20
  final private val MAX_ANGLE: Float = 20


  /**
   * This method is called periodically by the engine
   *
   * @param g
   */

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    player.focusCamera(g.getCamera, 1, Gdx.graphics.getDeltaTime)

    if (showAbilityMenu) {
      println(menu.visibleAbilities(0).description)
      println(menu.visibleAbilities(1).description)
      println(menu.visibleAbilities(2).description)
      menu.draw(g)

      if (Gdx.input.isKeyJustPressed(Keys.NUM_1) ||
        Gdx.input.isKeyJustPressed(Keys.NUM_2) ||
        Gdx.input.isKeyJustPressed(Keys.NUM_3)) {

        val keyToIndex = Map(Keys.NUM_1 -> 0, Keys.NUM_2 -> 1, Keys.NUM_3 -> 2)
        keyToIndex.find { case (key, _) => Gdx.input.isKeyJustPressed(key) } match {
          case Some((_, index)) if index < menu.visibleAbilities.length =>
            val chosenAbility = menu.visibleAbilities(index)
            Abilties += chosenAbility
            chosenAbility.setActivated() // <--- add this line
            showAbilityMenu = false
          case _ =>
        }
      }
    } else {

      if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
        menu.refreshAbilities()
        showAbilityMenu = true
        return
      }

      Abilties.foreach(_.update())

      val dt = Gdx.graphics.getDeltaTime
      Wave.globalEnemiesGeneration(enemies, player, dt)
      println(s"dt : $dt")
      player.getClosestEnemy(enemies)
      player.update(dt, null, enemies)
      if(Stinky.canBeDrawn) {
        g.drawFilledCircle(player.getPosition.x, player.getPosition.y, Stinky.radius, Color.RED)
      }
      player.projectiles.filter(_ != null).foreach(_.update(dt))

      player.projectiles.foreach(_.draw(g))
      player.projectiles.filterInPlace(p => p.active && p.position.dst(player.getPosition) < 2000)

      enemies.filterInPlace(_.getHp() > 0)

      for (en <- enemies.indices) {
        if (enemies(en).getHp() == 0) {
          enemies.remove(en)
        }
        enemies(en).update(dt, player.getPosition)
        val distanceToPlayer = enemies(en).getPosition.dst(player.getPosition)

        for (projectil <- player.projectiles) {
          val distanceToProjectile = enemies(en).getPosition.dst(projectil.position)
          if (distanceToProjectile < enemies(en).width) {
            enemies(en).getHit(projectil)
          }
        }

        if (distanceToPlayer < player.width) {
          player.getHit(enemies(en))
          enemies(en).getHit(player)
          }

      }
      for (i <- enemies.indices) {
        for (j <- i + 1 until enemies.length) {
          val en1 = enemies(i)
          val en2 = enemies(j)
          val direction = en1.getPosition.sub(en2.getPosition)
          val distance = direction.len()
          val minDistance = enemies(i).width
          if (distance < minDistance && distance > 0) {
            val push = direction.nor().scl((minDistance - distance) * 0.5f)
            en1.pushAway(push)
            en2.pushAway(push.scl(-1))
          }
        }
      }

      //g.drawPicture(0, 0, background)
      // Compute the angle of the image using an elastic interpolation
      val t = computePercentage
      val angle: Float = Interpolation.sine.apply(MIN_ANGLE, MAX_ANGLE, t)

      // Draw everything


      g.drawTransformedPicture(getWindowWidth / 2.0f, getWindowHeight / 2.0f, angle, 0.7f, imgBitmap)
      g.drawStringCentered(getWindowHeight * 0.8f, "Welcome to gdx2d !")
      g.drawFPS()

      player.draw(g)
      for (en <- enemies) {
        en.draw(g)
      }

      player.attack(player.getClosestEnemy(enemies), dt).draw(g)
    }
  }

  /**
   * Compute time percentage for making a looping animation
   *
   * @return the current normalized time
   */
  private def computePercentage: Float = {
    if (direction == 1) {
      currentTime += Gdx.graphics.getDeltaTime
      if (currentTime > ANIMATION_LENGTH) {
        currentTime = ANIMATION_LENGTH
        direction *= -1
      }
    }
    else {
      currentTime -= Gdx.graphics.getDeltaTime
      if (currentTime < 0) {
        currentTime = 0
        direction *= -1
      }
    }
    currentTime / ANIMATION_LENGTH
  }
}