package ch.hevs.gdx2d.Entity

import ch.hevs.gdx2d.components.bitmaps.Spritesheet
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.weapons_abilities._
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
  private val dmg = damages
  private var xp : Int = 0
  private var lvl: Int = 1
  private val currentZoom : Float= 0.6f
  private var lastPressed = "down"
  private var animationLockTimer = 0f
  private var fastSpeed: Int = speed

  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms
  private val knockbackDistance = 600f
  private var knockbackTargetPos: Vector2 = position.cpy()
  private var isKnockbackActive = false

  private var weapon1WasPressed = false
  private var weapon2WasPressed = false
  private var weapon3WasPressed = false

  var projectiles : ArrayBuffer[Projectile] = ArrayBuffer[Projectile]()
  var weapons : ArrayBuffer[Weapon] = ArrayBuffer[Weapon]()

  val orbs = ArrayBuffer[Orb]()

  var debugMode : Boolean = false

  private def addOrb(): Unit = {
    orbs += new Orb()
  }

  def addWeapon(name : String) = {
    name match {
      case "orb" =>
        addOrb()
      case "bow" =>
        weapons += new Bow
      case "spear" =>
        weapons += new Spear
      case _ =>
    }
  }

  def updateOrbs(dt: Float, enemies: ArrayBuffer[Enemy]): Unit = {
    orbs.foreach(_.update(dt, getPosition, enemies))
  }

  def drawOrbs(g: GdxGraphics, dt: Float): Unit = {
    orbs.foreach(_.draw(g, dt, getPosition, Vector2.Zero))
  }

  def getClosestEnemy(enemies: ArrayBuffer[Enemy]): Enemy = {
    if (enemies.isEmpty){
      println("NO MORE ENEMIES")
      val fictifEnemy = new Enemy("nothing", 0, 0, 0, 0, 0, 0, 0, 0)
      fictifEnemy
    } else {
      var closestEn: Enemy = enemies(0)
      for (en <- enemies.indices){
        val distance = enemies(en).getPosition.dst(getPosition)
        if (distance < closestEn.getPosition.dst(getPosition) && closestEn.isAlive()){
          closestEn = enemies(en)
          println(s"CLOSEST ENEMY : ${enemies(en).nbr}")
        }
      }
      closestEn
    }
  }
  def getClosestEnemiesForWeapons(enemies: ArrayBuffer[Enemy]): Map[Weapon, Enemy] = {
    val livingEnemies = enemies.filter(_.isAlive()).sortBy(_.getPosition.dst(this.getPosition))
    val weaponTargets = scala.collection.mutable.Map[Weapon, Enemy]()

    val onlyBossAlive = livingEnemies.size == 1 && livingEnemies.head.name == "boss"

    if (onlyBossAlive) {
      for (weapon <- weapons) {
        weaponTargets(weapon) = livingEnemies.head
      }
    } else {
      val usedEnemies = scala.collection.mutable.Set[Enemy]()
      for (weapon <- weapons) {
        val targetOpt = livingEnemies.find(e => !usedEnemies.contains(e))
        targetOpt.foreach { target =>
          weaponTargets(weapon) = target
          usedEnemies += target
        }
      }
    }

    weaponTargets.toMap
  }

  def nbrBows(): Int = {
    var counter = 0
    for(weapon <- weapons){
      if(weapon.name == "Bow"){
        counter += 1
      }
    }
    counter
  }

  def nbrSpears(): Int = {
    var counter = 0
    for(weapon <- weapons){
      if(weapon.name == "Spear"){
        counter += 1
      }
    }
    counter
  }

  var isLevelingUp : Boolean = false
  private val xpLevel : Array[Int] = Array(2000, 5000, 10000, 20000, 450000)


  // Update the position of the player matching to input
  def update(dt: Float, playerPos: Vector2 = null, enemies : ArrayBuffer[Enemy]): Unit = {
    isLevelingUp = false

    // Déterminer le nouveau niveau
    val newLevel = xp match {
      case xp if xp < xpLevel(0)  => 1
      case xp if xp < xpLevel(1)  => 2
      case xp if xp < xpLevel(2)  => 3
      case xp if xp < xpLevel(3)  => 4
      case xp if xp < xpLevel(4)  => 5
      case _                      => 6
    }

    // Si on a monté de niveau
    if (newLevel > lvl) {
      isLevelingUp = true
    }

    // Mettre à jour le niveau actuel
    lvl = newLevel

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
        if(debugMode){
          val weapon1 = Gdx.input.isKeyPressed(Input.Keys.NUM_1)
          val weapon2 = Gdx.input.isKeyPressed(Input.Keys.NUM_2)
          val weapon3 = Gdx.input.isKeyPressed(Input.Keys.NUM_3)
          val wave3 = Gdx.input.isKeyPressed(Input.Keys.NUM_8)
          val wave5 = Gdx.input.isKeyPressed(Input.Keys.NUM_9)
          val bossWave = Gdx.input.isKeyPressed(Input.Keys.NUM_0)

          if (wave3){
            xp = xpLevel(2)
          } else if (wave5){
            xp = xpLevel(3)
          } else if (bossWave){
            xp = xpLevel(4)
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
  def getHp : Int = hp
  def getHpMax : Int = hpMax


  // TODO : Placeholder to check drawing and movement logic, need to be change
  def draw(g: GdxGraphics, dt: Float): Unit = {
    generateFrame(g, dt)
    if(debugMode){
      g.setColor(Color.GREEN)
      g.drawString(getPosition.x, getPosition.y, s"(${getPosition.x}, ${getPosition.y})")
      g.drawRectangle(getPosition.x, getPosition.y, width, height, 0)
    }
  }


  override def getPosition: Vector2 = position.cpy()


  // All the logic for the following camera with smoother transition from A to B
  def focusCamera(camera: OrthographicCamera, dt: Float): Unit = {
    // Variable declarations for centering the camera
    val targetX = position.x
    val targetY = position.y

    val lin_interpo = 5f //Linear interpolation for smoother movement from camera

    // Change the camera position along player's position + zoom
    camera.position.x += (targetX - camera.position.x) * lin_interpo * dt
    camera.position.y += (targetY - camera.position.y) * lin_interpo * dt
    camera.zoom = currentZoom

    camera.update()
    camera.update()
  }

  def damage(): Int = {
    dmg
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

  def applyProjectileKnockback(direction: Vector2, force: Float = 50f): Unit = {
    if (!isKnockbackActive) {
      knockbackDir = direction.nor()
      knockbackTargetPos = position.cpy().add(knockbackDir.scl(force))
      knockbackTimer = knockbackDuration
      isKnockbackActive = true
    }
  }

  def getHit(projectile: Projectile): Unit = {
    val knockbackDirection = this.getPosition.cpy().sub(projectile.position)
    val knockbackForce = 2f
    this.applyProjectileKnockback(knockbackDirection, knockbackForce)
    if (hp > 0) {
      animationLockTimer = 0.3f
      hp -= 1000
      isKnockbackActive = false

      if (hp <= 0) {
        currentFrame = 0
        animationTimer = 0f
      }
    }
  }
  //first weapon
  private val weapon: Weapon = new Bow()
  weapons += weapon

  //other weapons
  private val bow: Weapon = new Bow()
  private val spear: Weapon = new Bow()

  def getWeapon: Weapon = weapon

  var counterDt : Float = 1
  def attack(ennemy: Enemy): Projectile = {
    println("PLAYER ATTACK")
    val from = getPosition
    val to = ennemy.getPosition
    val direction = new Vector2(to).sub(from).nor()
    val newProjectile = weapon.attack(from, direction)
    newProjectile

  }


  def isAlive(): Boolean = {
    if (hp <= 0){
      false
    } else {
      true
    }

  }
  def addXp(enemyXp: Int): Unit = {
    xp += enemyXp
  }
  def getXp(): Int = {
    xp
  }
  def getXpForNextLevel(): Int = {
    lvl match {
      case 1 =>
        xpLevel(0)
      case 2 =>
        xpLevel(1)
      case 3 =>
        xpLevel(2)
      case 4 =>
        xpLevel(3)
      case 5 =>
        xpLevel(4)
      case _ =>
        xpLevel(4)
    }
  }

  def getLevel(): Int = {
    lvl
  }

  private var animationTimer = 0f
  private val frameDuration = 0.1f // Durée d'affichage de chaque frame (en secondes)

  private val SPRITE_WIDTH = 128
  private val SPRITE_HEIGHT = 128

  private var ss: Spritesheet = null

  private var currentFrame = 0


  private var newLaunch : Boolean = true

  def generateFrame(g: GdxGraphics, dt: Float): Unit = {
    if (newLaunch){
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

  private var cooldownDuration = 0.2f

  def setAttackSpeed(modifier: Float): Float = {
    cooldownDuration -= modifier
    cooldownDuration
  }

  def getSpeed(): Int = {
    fastSpeed
  }

  def setSpeed(fast: Int): Unit = {
    fastSpeed += fast
  }

  def setHealth(heal: Int): Unit = {
    hp += heal
  }

}
