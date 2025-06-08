package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.{Interpolation, Vector2}

import scala.collection.mutable.ArrayBuffer
import scala.reflect.internal.util.Position
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
              nbr : Int) extends Entity (x, y, width, height, speed, healthPoint, damages, nbr) {

  private val position = new Vector2(x, y)

  private var hp = healthPoint
  private var dmg = damages
  private var xp: Int = 0
  private var lvl: Int = 1
  private var currentZoom: Float = 1
  private var fastSpeed: Int = speed

  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms
  private val knockbackDistance = 600f

  private var cooldownTimer = 0f
  private var cooldownDuration = 0.2f
  var projectiles: ArrayBuffer[Projectile] = ArrayBuffer[Projectile]()
  var weapons: ArrayBuffer[Weapon] = ArrayBuffer[Weapon]()

  def getClosestEnemy(enemies: ArrayBuffer[Enemy]): Enemy = {
    if (enemies.size == 0) {
      println("NO MORE ENEMIES")
      var fictifEnemy = new Enemy("nothing", 0, 0, 0, 0, 0, 0, 0, 0)
      return fictifEnemy
    } else {
      var closestEn: Enemy = enemies(0)
      for (en <- enemies.indices) {
        val distance = enemies(en).getPosition.dst(getPosition)
        if (distance < closestEn.getPosition.dst(getPosition)) {
          closestEn = enemies(en)
          println(s"CLOSEST ENEMY : ${enemies(en).nbr}")
        }
      }
      return closestEn
    }
  }


  // Update the position of the player matching to input
  def update(dt: Float, playerPos: Vector2 = null, enemies: ArrayBuffer[Enemy]): Unit = {
    if (xp >= 5000 && xp <= 15000) {
      lvl = 2
    } else if (xp >= 15000 && xp <= 40000) {
      lvl = 3
    } else if (xp >= 40000 && xp <= 75000) {
      lvl = 4
    } else if (xp >= 75000 && xp <= 150000) {
      lvl = 5
    } else if (xp >= 150000) {
      lvl = 6
    }
    if (hp <= 0) {
      println("GAME OVER")

    } else {
      if (knockbackTimer > 0) {
        knockbackTimer -= dt
        val t = 1f - (knockbackTimer / knockbackDuration)
        val strength = Interpolation.swingOut.apply(1f - t) * knockbackDistance

        position.add(knockbackDir.cpy().scl(strength * dt))
      } else {
        val diagonalFactor = 0.8f // Useful to avoid faster movement on diagonal

        val left = Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val right = Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        val up = Gdx.input.isKeyPressed(Input.Keys.UP)
        val down = Gdx.input.isKeyPressed(Input.Keys.DOWN)

        val weapon1 = Gdx.input.isKeyPressed(Input.Keys.NUM_1)
        val weapon2 = Gdx.input.isKeyPressed(Input.Keys.NUM_2)
        val xp4 = Gdx.input.isKeyPressed(Input.Keys.NUM_9)



        //TODO : ONLY FOR DEBUGS
        if (weapon1 == true) {
          weapons += bow
          setWeapon(bow)
        } else if (weapon2 == true) {
          weapons += spear
          setWeapon(spear)
        } else if (xp4 == true) {
          xp = 140000
        }


        val horizontal = (if (left) -1 else 0) + (if (right) 1 else 0)
        val vertical = (if (up) 1 else 0) + (if (down) -1 else 0)

        val isDiagonal = horizontal != 0 && vertical != 0
        val moveFactor = if (isDiagonal) diagonalFactor else 1f

        position.x += horizontal * fastSpeed * dt * moveFactor
        position.y += vertical * fastSpeed * dt * moveFactor

        cooldownTimer -= dt
        if (cooldownTimer <= 0) {
          val closest = getClosestEnemy(enemies)
          if (closest != null) {
            val distance = closest.getPosition.dst(getPosition)
            if (distance < weapon.range) {
              val from = getPosition
              val to = closest.getPosition
              val direction = new Vector2(to).sub(from).nor()

              val proj = weapon.attack(from, direction)
              projectiles += proj
              cooldownTimer = cooldownDuration
            }

          }
        }
      }

    }


  }

  // TODO : Placeholder to check drawing and movement logic, need to be change
  override def draw(g: GdxGraphics): Unit = {
    g.setColor(Color.GRAY)
    g.drawFilledRectangle(position.x, position.y + 100, 120, 15, 0)
    g.setColor(Color.GREEN)
    g.drawFilledRectangle(position.x, position.y + 100, hp / 2, 8, 0)
    g.setColor(Color.WHITE) // Hitbox
    g.drawFilledRectangle(position.x, position.y, width, height, 0)
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
    camera.zoom = zoom

    camera.update()
  }

  def damage(): Int = {
    return dmg
  }

  def getHit(ennemy: Enemy): Unit = {
    hp -= ennemy.damage()
    println(s"PLAY")
    println(s"PLAYER GOT HIT : $hp HP left")

    // Direction inverse de l'ennemi vers le joueur
    knockbackDir = new Vector2(position).sub(ennemy.getPosition).nor()
    knockbackTimer = knockbackDuration
  }

  //first weapon
  private var weapon: Weapon = new Bow()

  //other weapons
  private var bow: Weapon = new Bow()
  private var spear: Weapon = new Spear()

  def setWeapon(w: Weapon): Unit = {
    weapon = w
  }


  def weaponEquiped(): Boolean = {
    if (weapon == null) {
      return false
    }
    true
  }

  def getWeapon: Weapon = weapon

  var counterDt: Float = 1

  def attack(ennemy: Enemy, dt: Float): Projectile = {
    println("PLAYER ATTACK")
    val from = getPosition
    val to = ennemy.getPosition
    val direction = new Vector2(to).sub(from).nor()
    val newProjectile = weapon.attack(from, direction)
    return newProjectile
  }


  def isAlive(): Boolean = {
    if (hp <= 0) {
      return false
    } else {
      return true
    }
  }

  def addXp(enemyXp: Int): Unit = {
    xp += enemyXp * 10
  }

  def getXp(): Int = {
    return xp
  }

  def getLevel(): Int = {
    return lvl
  }

  def getZoom(): Float = {
    return currentZoom
  }

  def setAttackSpeed(modifier: Float): Float = {
    cooldownDuration -= modifier
    cooldownDuration
  }

  def setSpeed(fast: Int): Unit = {
    fastSpeed += fast
  }

  def setHealth(heal: Int): Unit = {
    hp += heal
  }
}