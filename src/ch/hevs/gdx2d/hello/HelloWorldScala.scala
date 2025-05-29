package ch.hevs.gdx2d.hello

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.{Interpolation, Vector2}
import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.desktop.PortableApplication
import com.badlogic.gdx.Input.Keys
import ch.hevs.gdx2d.hello.Player

import scala.util.Random
import scala.collection.mutable.ArrayBuffer

/**
 * Hello World demo in Scala
 *
 * @author Pierre-André Mudry (mui)
 * @version 1.0
 */
object HelloWorldScala {

  def main(args: Array[String]): Unit = {
    new HelloWorldScala
  }
}

class HelloWorldScala extends PortableApplication(1920, 1080) {
  private var imgBitmap: BitmapImage = null
  private var background : BitmapImage = null

  private val player : Player = new Player(100, 100, 100, 100)
  private val enemies : ArrayBuffer[Ennemy] = ArrayBuffer[Ennemy]()

  override def onInit(): Unit = {
    setTitle("BitWorld")
    // Load a custom image (or from the lib "res/lib/icon64.png")
    imgBitmap = new BitmapImage("data/images/ISC_logo.png")
    background = new BitmapImage("data/images/placeholder_background.jpg")

    generateEnemies(5)
  }

  def generateEnemies(nbr: Int): Unit = {
    for (ennemy <- 0 until nbr){
      val x = Random.nextFloat() * getWindowWidth
      val y = Random.nextFloat() * getWindowHeight
      val en = new Ennemy(x, y, 100, 100)
      enemies += en
    }
  }
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
    val dt = Gdx.graphics.getDeltaTime
    player.update(dt)

    enemies.filterInPlace(_.getHp() > 0)
    for (en <- enemies.indices){
      if(enemies(en).getHp() == 0){
        enemies.remove(en)
      }
      enemies(en).update(dt, player.getPosition)
      val distance = enemies(en).getPosition.dst(player.getPosition)

      if (distance < 100f){
        player.getHit(enemies(en))
        enemies(en).getHit(player)
      }
    }
    for (i <- enemies.indices){
      for (j <- i + 1 until enemies.length){
        val en1 = enemies(i)
        val en2 = enemies(j)
        val direction = en1.getPosition.sub(en2.getPosition)
        val distance = direction.len()
        val minDistance = 100f

        if (distance < minDistance && distance > 0){
          val push = direction.nor().scl((minDistance-distance) * 0.5f)
          en1.pushAway(push)
          en2.pushAway(push.scl(-1))
        }
      }
    }

    player.focusCamera(g.getCamera, 1, dt)
    // Clears the screen
    g.clear()

    g.drawPicture(0, 0, background)
    // Compute the angle of the image using an elastic interpolation
    val t = computePercentage
    val angle: Float = Interpolation.sine.apply(MIN_ANGLE, MAX_ANGLE, t)

    // Draw everything

    g.drawTransformedPicture(getWindowWidth / 2.0f, getWindowHeight / 2.0f, angle, 0.7f, imgBitmap)
    g.drawStringCentered(getWindowHeight * 0.8f, "Welcome to gdx2d !")
    g.drawFPS()
    g.drawSchoolLogo()

    player.draw(g)
    for (en <- enemies){
      en.draw(g)
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
