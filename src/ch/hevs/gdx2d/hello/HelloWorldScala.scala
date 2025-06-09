package ch.hevs.gdx2d.hello

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.{Interpolation, Matrix4, Vector2}
import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.desktop.PortableApplication
import com.badlogic.gdx.Input.Keys
import ch.hevs.gdx2d.hello.Player
import ch.hevs.gdx2d.hello.WaveManager
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

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
  private var backgroundDrew : Boolean = false



  private val player : Player = new Player(100, 100, 60, 60, 200, 200000, 20, 0)
  private var firstLaunch : Boolean = true
  private val enemies : ArrayBuffer[Enemy] = ArrayBuffer[Enemy]()
  private val Wave = new WaveManager

  //For UI
  private var uiBatch: SpriteBatch = _
  private var uiCamera: OrthographicCamera = _
  private var uiFont: BitmapFont = _
  private var uiShapes: ShapeRenderer = _

  override def onInit(): Unit = {
    setTitle("BitWorld")
    // Load a custom image (or from the lib "res/lib/icon64.png")
    imgBitmap = new BitmapImage("data/images/ISC_logo.png")
    background = new BitmapImage("data/images/map.png")
    uiBatch = new SpriteBatch()
    uiCamera = new OrthographicCamera()
    uiCamera.setToOrtho(false, getWindowWidth, getWindowHeight)
    uiFont = new BitmapFont()
    uiShapes = new ShapeRenderer()


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

    // Clears the screen
    g.clear()
    g.drawPicture(0, 0, background)
    Wave.globalEnemiesGeneration(enemies, player, dt)
    println(s"dt : $dt")
    player.getClosestEnemy(enemies)

    player.update(dt, null, enemies)


    player.projectiles.foreach(_.update(dt))
    player.projectiles.foreach(_.draw(g))

    player.projectiles.filterInPlace(p => p.active && p.position.dst(player.getPosition) < 2000)



    enemies.filterInPlace { enemy =>
      val keep = !enemy.isReadyToBeRemoved()
      if (!keep) {
        player.addKill()
        player.addXp(enemy.getXp())
        println("ENEMY REMOVED")
      }
      keep
    }

    for (en <- enemies.indices){
      enemies(en).update(dt, player.getPosition)
      val distanceToPlayer = enemies(en).getPosition.dst(player.getPosition)

      for (projectil <- player.projectiles){
        val distanceToProjectile = enemies(en).getPosition.dst(projectil.position)
        if (distanceToProjectile < enemies(en).width){
          enemies(en).getHit(g, enemies, projectil)
        }
      }

      if (distanceToPlayer < enemies(en).width / 2){
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
        val minDistance = enemies(i).width

        if (distance < minDistance && distance > 0 && en1.isAlive() && en2.isAlive()){
          val push = direction.nor().scl((minDistance-distance) * 0.5f)
          en1.pushAway(push)
          en2.pushAway(push.scl(-1))
        }
      }
    }

    player.focusCamera(g.getCamera, 1, dt)

    // Compute the angle of the image using an elastic interpolation
    val t = computePercentage
    val angle: Float = Interpolation.sine.apply(MIN_ANGLE, MAX_ANGLE, t)

    // Draw everything

    g.drawTransformedPicture(getWindowWidth / 2.0f, getWindowHeight / 2.0f, angle, 0.7f, imgBitmap)
    g.drawStringCentered(getWindowHeight * 0.8f, "Welcome to gdx2d !")

    g.drawFPS()
    g.drawSchoolLogo()

    player.draw(g, dt)
    for (en <- enemies){
      en.draw(g)
    }

    val targets = player.getClosestEnemiesForWeapons(enemies)

    for ((weapon, target) <- targets) {
      val direction = target.getPosition.cpy().sub(player.getPosition)
      weapon.draw(g, dt, player.getPosition, direction)
    }


    player.attack(player.getClosestEnemy(enemies), dt).draw(g)
    player.updateOrbs(dt, enemies)
    player.drawOrbs(g, dt)

    g.setColor(Color.BLACK)

    uiCamera.update()
    uiBatch.setProjectionMatrix(uiCamera.combined)

    uiBatch.begin()
    val barWidth = 200
    val barHeight = 25
    val margin = 20
    val yPos = getWindowHeight - margin - barHeight



    // Fond de la barre
    uiShapes.begin(ShapeRenderer.ShapeType.Filled)
    uiShapes.setColor(0.2f, 0.2f, 0.2f, 1) // Gris foncé
    uiShapes.rect(margin, yPos, barWidth, barHeight)

    val hpPercent = player.getHp().toFloat / player.getHpMax()
    //change color to hp (red to green)
    uiShapes.setColor(
      1 - hpPercent,
      hpPercent,
      0.2f,
      1
    )
    uiShapes.rect(margin, yPos, barWidth * hpPercent, barHeight)
    uiShapes.end()
    uiBatch.end()

    uiBatch.begin()
    uiFont.draw(uiBatch, f"${player.getHp()}%d", margin + 10, yPos + barHeight - 5)
    uiBatch.end()

    val xpBarY = yPos - 30

    // Fond barre XP
    uiShapes.begin(ShapeRenderer.ShapeType.Filled)
    uiShapes.setColor(0.1f, 0.1f, 0.3f, 1) // Bleu fonéjoiuth9p8 tppui hpi jnkél hé op hup uhuhi phuo ohup  éo hoiu houi gzoz gzozo zo guhip  hiou ooui oo ho iu g o uihuio  ué jk é
    uiShapes.rect(margin, xpBarY, barWidth, barHeight - 5)

    // XP actuelle
    val xpToNextLevel = player.getXpForNextLevel() // À implémenter
    val xpPercent = player.getXp().toFloat / xpToNextLevel
    uiShapes.setColor(0.3f, 0.4f, 1f, 1) // Bleu clair
    uiShapes.rect(margin, xpBarY, barWidth * xpPercent, barHeight - 5)

    uiShapes.setColor(1, 1, 0, 1)
    uiShapes.rect(margin + barWidth * xpPercent - 2, xpBarY - 5, 4, barHeight + 5)
    uiShapes.end()



    uiBatch.begin()
    uiFont.draw(uiBatch, s"Lvl ${player.getLevel()} (${(xpPercent * 100).toInt}%)", margin + 10, xpBarY + barHeight - 10)
    uiFont.draw(uiBatch, s"Kills ${player.getKillCount}", margin + 10, xpBarY + barHeight - 40)
    uiFont.draw(uiBatch, s"${player.weapons.map(_.name).mkString(", ")}\n Orbs : ${player.orbs.length}", margin + 10, xpBarY + barHeight - 70)
    uiFont.draw(uiBatch, s"Nbr enemies : ${enemies.length}", margin + 10, xpBarY + barHeight - 100)
    //uiFont.draw(uiBatch, s"${player.getXp()}", margin + 10, xpBarY + barHeight - 100) #For debug
    uiBatch.end()

    // Après avoir dessiné les barres principales
    uiShapes.begin(ShapeRenderer.ShapeType.Line)
    uiShapes.setColor(0.5f, 0.5f, 0.5f, 1) // Gris métallique
    uiShapes.rect(margin - 1, yPos - 1, barWidth + 2, barHeight + 2) // Contour HP
    uiShapes.rect(margin - 1, xpBarY - 1, barWidth + 2, barHeight - 5 + 2) // Contour XP
    uiShapes.end()


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
