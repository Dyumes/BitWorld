package ch.hevs.gdx2d

import ch.hevs.gdx2d.Entity.{Enemy, Player}
import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.weapons_abilities._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}

import scala.collection.mutable.ArrayBuffer

object Main {

  def main(args: Array[String]): Unit = {
    new Main  }
}

class Main extends PortableApplication(1920, 1080) {
  private var imgBitmap: BitmapImage = null
  private var background : BitmapImage = null
  private var backgroundDrew : Boolean = false



  private val player : Player = new Player(100, 100, 60, 60, 200, 200000, 20, 0)
  private var firstLaunch : Boolean = true
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
  private var arAbilities: ArrayBuffer[Ability] = new ArrayBuffer()



  val menu = new AbilityMenu(
    player,
    List(dmgBoost, spdBoost, hpBoost, asBoost, prcBoost, Stinky)
  )

  //For UI
  private var uiBatch: SpriteBatch = _
  private var uiCamera: OrthographicCamera = _
  private var uiFont: BitmapFont = _
  private var uiShapes: ShapeRenderer = _

  private var bossHp: Int = 0
  private var bossHpMax : Int = 0


  override def onInit(): Unit = {
    setTitle("BitWorld")
    // Load a custom image (or from the lib "res/lib/icon64.png")
    background = new BitmapImage("data/images/map.png")
    uiBatch = new SpriteBatch()
    uiCamera = new OrthographicCamera()
    uiCamera.setToOrtho(false, getWindowWidth, getWindowHeight)
    uiFont = new BitmapFont()
    uiShapes = new ShapeRenderer()
  }
  /**
   * This method is called periodically by the engine
   *
   * @param g
   */

  override def onGraphicRender(g: GdxGraphics): Unit = {
    val dt = Gdx.graphics.getDeltaTime
    val debug : Boolean = player.debugMode

    if(Gdx.input.isKeyJustPressed(Keys.TAB)){
      player.debugMode = !player.debugMode
    }

    // Clears the screen
    g.clear()
    g.drawPicture(0, 0, background)



    if (showAbilityMenu) {
      println(menu.visibleAbilities(0).description)
      println(menu.visibleAbilities(1).description)
      println(menu.visibleAbilities(2).description)
      menu.drawAbilityChoices(g)

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

      if (player.isLevelingUp || (Gdx.input.isKeyJustPressed(Keys.SPACE) && debug)) {
        player.isLevelingUp = false
        menu.refreshAbilities()
        showAbilityMenu = true
        return
      }

      Abilties.foreach(_.update())

      Wave.globalEnemiesGeneration(enemies, player, dt)
      println(s"dt : $dt")
      player.getClosestEnemy(enemies)

      player.update(dt, null, enemies)
      if(Stinky.canBeDrawn) {
        g.drawFilledCircle(player.getPosition.x, player.getPosition.y, Stinky.radius, Color.RED)
      }

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

      for (en <- enemies.indices) {
        enemies(en).update(dt, player.getPosition)
        val distanceToPlayer = enemies(en).getPosition.dst(player.getPosition)

        for (projectil <- player.projectiles) {
          val distanceToProjectile = enemies(en).getPosition.dst(projectil.position)
          if (distanceToProjectile < enemies(en).width && projectil.isCollidingWith(enemies(en))) {
            // Calcul de la direction du knockback
            val knockbackDirection = enemies(en).getPosition.cpy().sub(projectil.position)
            enemies(en).applyProjectileKnockback(knockbackDirection)
            enemies(en).getHit(g, enemies, projectil)
            projectil.onHit(enemies(en))
          }
        }

        if (distanceToPlayer < enemies(en).width / 2) {
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

          if (distance < minDistance && distance > 0 && en1.isAlive() && en2.isAlive()) {
            val push = direction.nor().scl((minDistance - distance) * 0.5f)
            en1.pushAway(push)
            en2.pushAway(push.scl(-1))
          }
        }
      }

      player.focusCamera(g.getCamera, 1, dt)

      g.drawFPS()

      player.draw(g, dt)
      for (en <- enemies) {
        en.draw(g)
        if(debug){
          g.setColor(Color.RED)
          g.drawString(en.getPosition.x, en.getPosition.y, s"${en.nbr}")
          g.drawRectangle(en.getPosition.x, en.getPosition.y, en.width, en.height, 0)
        }
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

      val hpPercent = player.getHp.toFloat / player.getHpMax
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
      uiFont.draw(uiBatch, f"${player.getHp}%d", margin + 10, yPos + barHeight - 5)
      uiBatch.end()

      val xpBarY = yPos - 30

      // Fond barre XP
      uiShapes.begin(ShapeRenderer.ShapeType.Filled)
      uiShapes.setColor(0.1f, 0.1f, 0.3f, 1)
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
      uiFont.draw(uiBatch, s"Abilities : ${arAbilities.map(_.name).mkString(", " )}", margin + 10, xpBarY + barHeight - 130)
      uiFont.draw(uiBatch, s"Player // hp : ${player.getHp} - dmg : ${player.damage()} - speed : ${player.getSpeed()}", margin + 10, xpBarY + barHeight - 160)

      uiBatch.end()

      // Après avoir dessiné les barres principales
      uiShapes.begin(ShapeRenderer.ShapeType.Line)
      uiShapes.setColor(0.5f, 0.5f, 0.5f, 1) // Gris métallique
      uiShapes.rect(margin - 1, yPos - 1, barWidth + 2, barHeight + 2) // Contour HP
      uiShapes.rect(margin - 1, xpBarY - 1, barWidth + 2, barHeight - 5 + 2) // Contour XP
      uiShapes.end()

      if(player.debugMode){
        uiBatch.begin()
        uiFont.draw(uiBatch, s"DEBUG/CHEAT MODE", 960, getWindowHeight - 10)
        uiBatch.end()
      }
    }

    if (player.getLevel() >= 6) {
      for(en <- enemies){
        if (en.name == "boss"){
          bossHp = en.getHp()
          bossHpMax = en.getHpMax()
        }
      }
      // Barre de vie du boss
      val bossBarWidth = 600
      val bossBarHeight = 30
      val bossBarX = (getWindowWidth - bossBarWidth) / 2
      val bossBarY = getWindowHeight - 100

      if (bossHpMax > 0) {
        val bossHpPercent = bossHp.toFloat / bossHpMax

        // Fond de la barre du boss
        uiShapes.begin(ShapeRenderer.ShapeType.Filled)
        uiShapes.setColor(Color.DARK_GRAY)
        uiShapes.rect(bossBarX, bossBarY, bossBarWidth, bossBarHeight)

        // Barre rouge dynamique
        uiShapes.setColor(Color.RED)
        uiShapes.rect(bossBarX, bossBarY, bossBarWidth * bossHpPercent, bossBarHeight)
        uiShapes.end()
      }

11
      // Contour
      uiShapes.begin(ShapeRenderer.ShapeType.Line)
      uiShapes.setColor(Color.BLACK)
      uiShapes.rect(bossBarX, bossBarY, bossBarWidth, bossBarHeight)
      uiShapes.end()

      // Texte au centre
      uiBatch.begin()
      uiFont.draw(uiBatch, s"BOSS HP: ${bossHp.toInt} / ${bossHpMax.toInt}", bossBarX + 10, bossBarY + bossBarHeight - 5)
      uiBatch.end()
    }

    /**
     * Compute time percentage for making a looping animation
     *
     * @return the current normalized time
     */

  }
}
