package ch.hevs.gdx2d.Entity

import ch.hevs.gdx2d.components.bitmaps.Spritesheet
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.weapons_abilities._
import ch.hevs.gdx2d.Entity.Enemy
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.{Interpolation, Vector2}
import com.badlogic.gdx.{Gdx, Input}

import scala.collection.mutable.ArrayBuffer
/**
 *
 * A Player class for controlling the player and the camera following all along
 *
 */

class Player(
              x: Float,
              y: Float,
              width: Float,
              height: Float,
              speed: Int,
              healthPoint: Int,
              damages: Int,
              nbr : Int) extends Entity (x, y, width, height, speed, healthPoint, damages, nbr){

  private val position = new Vector2(x, y)

  private var hp = healthPoint
  private val hpMax = healthPoint
  private var hpToDraw = hp * 100 / hpMax
  private var dmg = damages
  private var xp : Int = 0
  private var lvl: Int = 1
  private var currentZoom : Float= 0.6f
  private var lastPressed = "down"
  private var animationLockTimer = 0f
  private var fastSpeed: Int = speed

  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms
  private val knockbackDistance = 600f

  private var weapon1WasPressed = false
  private var weapon2WasPressed = false
  private var weapon3WasPressed = false
  private var weapon4WasPressed = false


  var projectiles : ArrayBuffer[Projectile] = ArrayBuffer[Projectile]()
  var weapons : ArrayBuffer[Weapon] = ArrayBuffer[Weapon]()


  val orbs = ArrayBuffer[Orb]()

  var debugMode : Boolean = false

  def addOrb(): Unit = {
    orbs += new Orb()
  }

  def updateOrbs(dt: Float, enemies: ArrayBuffer[Enemy]): Unit = {
    orbs.foreach(_.update(dt, getPosition, enemies))
  }

  def drawOrbs(g: GdxGraphics, dt: Float): Unit = {
    orbs.foreach(_.draw(g, dt, getPosition, Vector2.Zero))
  }

  def getClosestEnemy(enemies: ArrayBuffer[Enemy]): Enemy = {
    if (enemies.size == 0){
      println("NO MORE ENEMIES")
      var fictifEnemy = new Enemy("nothing", 0, 0, 0, 0, 0, 0, 0, 0)
      return fictifEnemy
    } else {
      var closestEn: Enemy = enemies(0)
      for (en <- enemies.indices){
        val distance = enemies(en).getPosition.dst(getPosition)
        if (distance < closestEn.getPosition.dst(getPosition) && closestEn.isAlive() == true){
          closestEn = enemies(en)
          println(s"CLOSEST ENEMY : ${enemies(en).nbr}")
        }
      }
      return closestEn
    }
  }

  def getClosestEnemiesForWeapons(enemies: ArrayBuffer[Enemy]): Map[Weapon, Enemy] = {
    val livingEnemies = enemies.filter(_.isAlive()).sortBy(_.getPosition.dst(this.getPosition))
    val weaponTargets = scala.collection.mutable.Map[Weapon, Enemy]()

    val usedEnemies = scala.collection.mutable.Set[Enemy]()

    for (weapon <- weapons) {
      val targetOpt = livingEnemies.find(e => !usedEnemies.contains(e))
      targetOpt.foreach { target =>
        weaponTargets(weapon) = target
        usedEnemies += target
      }
    }

    weaponTargets.toMap
  }

  var isLevelingUp : Boolean = false
  val xpLevel : Array[Int] = Array(5000, 15000, 40000, 75000, 150000)


  // Update the position of the player matching to input
  def update(dt: Float, playerPos: Vector2 = null, enemies : ArrayBuffer[Enemy]): Unit = {
    isLevelingUp = false

    if(xp >= xpLevel(0) && xp <= xpLevel(1)){
      lvl = 2
      if (xp == xpLevel(0)){
        isLevelingUp = true
        xp += 1
      }
    } else if (xp >= xpLevel(1) && xp <= xpLevel(2)){
      lvl = 3
      if (xp == xpLevel(1)){
        isLevelingUp = true
        xp += 1
      }
    } else if (xp >= xpLevel(2) && xp <= xpLevel(3)){
      lvl = 4
      if (xp == xpLevel(2)){
        isLevelingUp = true
        xp += 1
      }
    } else if (xp >= xpLevel(3) && xp <= xpLevel(4)){
      lvl = 5
      if (xp == xpLevel(3)){
        isLevelingUp = true
        xp += 1
      }
    } else if (xp >= xpLevel(4)){
      lvl = 6
      if (xp == xpLevel(4)){
        isLevelingUp = true
        xp += 1
      }
    }

    if (hp <= 0){
      println("GAME OVER")

    } else {
      if (knockbackTimer > 0) {
        knockbackTimer -= dt
        val t = 1f - (knockbackTimer / knockbackDuration)
        val strength = Interpolation.swingOut.apply(1f - t) * knockbackDistance

        position.add(knockbackDir.cpy().scl(strength * dt))
      } else {
        val diagonalFactor = 0.8f // Useful to avoid faster movement on diagonal

        val left  = Gdx.input.isKeyPressed(Input.Keys.A)
        val right = Gdx.input.isKeyPressed(Input.Keys.D)
        val up    = Gdx.input.isKeyPressed(Input.Keys.W)
        val down  = Gdx.input.isKeyPressed(Input.Keys.S)

        //Only for debug
        if(debugMode == true){
          val weapon1 = Gdx.input.isKeyPressed(Input.Keys.NUM_1)
          val weapon2 = Gdx.input.isKeyPressed(Input.Keys.NUM_2)
          val weapon3 = Gdx.input.isKeyPressed(Input.Keys.NUM_3)
          val xp3 = Gdx.input.isKeyPressed(Input.Keys.NUM_8)
          val xp4 = Gdx.input.isKeyPressed(Input.Keys.NUM_9)

          if (xp3 == true){
            xp = 40000
          } else if (xp4 == true){
            xp = 140000
          }

          if (weapon1 && !weapon1WasPressed) {
            weapons += new Bow
          }
          weapon1WasPressed = weapon1

          if (weapon2 && !weapon2WasPressed) {
            weapons += new Spear
          }
          weapon2WasPressed = weapon2

          if (weapon3 && !weapon3WasPressed) {
            addOrb()
          }
          weapon3WasPressed = weapon3
        }




        val horizontal = (if (left)
          {
            lastPressed = "left"
            -1
          } else 0) + (if (right)
          {
            lastPressed = "right"
            1
          } else 0)
        val vertical   = (if (up)
          {
            lastPressed = "up"
            1
          } else 0) + (if (down)
          {
            lastPressed = "down"
            -1
          } else 0)

        val isDiagonal = horizontal != 0 && vertical != 0
        val moveFactor = if (isDiagonal) diagonalFactor else 1f

        position.x += horizontal * speed * dt * moveFactor
        position.y += vertical   * speed * dt * moveFactor

        val targets = getClosestEnemiesForWeapons(enemies)

        for ((weapon, enemy) <- targets) {
          val distance = enemy.getPosition.dst(getPosition)
          if (distance < weapon.range && enemy.isAlive() && weapon.canAttack(dt)) {
            val from = getPosition
            val to = enemy.getPosition
            val direction = new Vector2(to).sub(from).nor()

            val proj = weapon.attack(from, direction)
            projectiles += proj
          }
        }

      }

    }


  }

  private var killCount: Int = 0

  def addKill(): Unit = {
    killCount += 1
  }

  def getKillCount: Int = killCount


  def displayUi(g: GdxGraphics): Unit = {

  }

  def getHp : Int = hp
  def getHpMax : Int = hpMax


  // TODO : Placeholder to check drawing and movement logic, need to be change
  def draw(g: GdxGraphics, dt: Float): Unit = {
    generateFrame(g, dt)


    // Dessin de la barre de vie
    g.setColor(Color.GRAY)
    g.drawFilledRectangle(position.x, position.y + 100, 120, 15, 0)
    g.setColor(Color.GREEN)
    g.drawFilledRectangle(position.x, position.y + 100, hpToDraw, 8, 0)

  }


  override def getPosition: Vector2 = position.cpy()


  // All the logic for the following camera with smoother transition from A to B
  def focusCamera(camera: OrthographicCamera, zoom: Float, dt: Float): Unit = {
    // Variable declarations for centering the camera
    val targetX = position.x
    val targetY = position.y

    val lin_interpo = 5f //Linear interpolation for smoother movement from camera

    // Change the camera position along player's position + zoom
    camera.position.x += (targetX - camera.position.x) * lin_interpo * dt
    camera.position.y += (targetY - camera.position.y) * lin_interpo * dt
    camera.zoom = currentZoom

    camera.update()
  }

  def damage(): Int = {
    return dmg
  }

  def getHit(ennemy: Enemy): Unit = {
    lastPressed = "hit"
    //ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
    hp -= ennemy.damage()
    hpToDraw += (hp * 100 / hpMax) - hpToDraw
    println(s"PLAY")
    println(s"PLAYER GOT HIT : $hp HP left - MAX HP $hpMax - HP TO DRAW $hpToDraw")

    // Direction inverse de l'ennemi vers le joueur
    knockbackDir = new Vector2(position).sub(ennemy.getPosition).nor()
    knockbackTimer = knockbackDuration
  }
  //first weapon
  private var weapon: Weapon = new Spear()
  weapons += weapon

  //other weapons
  private var bow: Weapon = new Bow()
  private var spear: Weapon = new Bow()

  def setWeapon(w: Weapon): Unit = {
    weapon = w
  }



  def weaponEquiped(): Boolean = {
    if (weapon == null){
      return false
    }
    true
  }

  def getWeapon: Weapon = weapon

  var counterDt : Float = 1
  def attack(ennemy: Enemy, dt: Float): Projectile = {
    println("PLAYER ATTACK")
    val from = getPosition
    val to = ennemy.getPosition
    val direction = new Vector2(to).sub(from).nor()
    val newProjectile = weapon.attack(from, direction)
    return newProjectile

  }


  def isAlive(): Boolean = {
    if (hp <= 0){
      return false
    } else {
      return true
    }

  }
  def addXp(enemyXp: Int): Unit = {
    xp += enemyXp
  }
  def getXp(): Int = {
    return xp
  }
  def getXpForNextLevel(): Int = {
    lvl match {
      case 1 =>
        return 5000
      case 2 =>
        return 15000
      case 3 =>
        return 40000
      case 4 =>
        return 75000
      case 5 =>
        return 150000
      case _ =>
        return 0
    }
  }

  def getLevel(): Int = {
    return lvl
  }

  private var animationTimer = 0f
  private val frameDuration = 0.1f // Durée d'affichage de chaque frame (en secondes)

  private var lastAnimation: String = "down"

  private val SPRITE_WIDTH = 128
  private val SPRITE_HEIGHT = 128
  private val FRAME_TIME = 0.15 // Duration of each frame

  private var ss: Spritesheet = null

  private val textureX = 0
  private var textureY = 1

  /**
   * Animation related parameters
   */
  private var dt = 0
  private var currentFrame = 0


  private var newLaunch : Boolean = true

  def generateFrame(g: GdxGraphics, dt: Float): Unit = {
    if (newLaunch == true){
      ss = new Spritesheet("data/images/player/player_walk.png", SPRITE_WIDTH, SPRITE_HEIGHT)
      newLaunch = false
    }
    if (animationLockTimer > 0) animationLockTimer -= dt

    val dirIndex = lastPressed match {
      case "down"  => 0
      case "up"  => 3
      case "left" => 1
      case "right"    => 2
      case _       => 0
    }

    animationTimer += dt
    if (animationTimer >= frameDuration) {
      animationTimer = 0
      currentFrame = (currentFrame + 1) % ss.sprites(dirIndex).length
    }

    val img = ss.sprites(dirIndex)(currentFrame)
    g.draw(img, position.x - SPRITE_WIDTH/2, position.y - SPRITE_HEIGHT/2)
  }


  private var cooldownTimer = 0f
  private var cooldownDuration = 0.2f

  def setAttackSpeed(modifier: Float): Float = {
    cooldownDuration -= modifier
    cooldownDuration
  }

  def getSpeed(): Int = {
    return fastSpeed
  }

  def setSpeed(fast: Int): Unit = {
    fastSpeed += fast
  }

  def setHealth(heal: Int): Unit = {
    hp += heal
  }

}
