package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.{Interpolation, Vector2}

import scala.collection.mutable.ArrayBuffer
/**
 *
 * A Player class for controlling the player and the camera following all along
 *
 */

class Player(x: Float, y: Float, width: Float, height: Float, speed: Int, healthPoint: Int, nbr : Int) extends Entity (x, y, width, height, speed, healthPoint, nbr){
  private val position = new Vector2(x, y)

  private var hp = healthPoint

  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms
  private val knockbackDistance = 600f

  private var cooldownTimer = 0f
  private val cooldownDuration = 0.5f
  var projectiles : ArrayBuffer[Projectile] = ArrayBuffer[Projectile]()

  def getClosestEnemy(enemies: ArrayBuffer[Enemy]): Enemy = {
    if (enemies.size == 0){
      println("NO MORE ENEMIES")
      var fictifEnemy = new Enemy(0, 0, 0, 0, 0, 0, 0)
      return fictifEnemy
    } else {
      var closestEn: Enemy = enemies(0)
      for (en <- enemies.indices){
        val distance = enemies(en).getPosition.dst(getPosition)
        if (distance < closestEn.getPosition.dst(getPosition)){
          closestEn = enemies(en)
          println(s"CLOSEST ENEMY : ${enemies(en).nbr}")
        }
      }
      return closestEn
    }
  }


  // Update the position of the player matching to input
  def update(dt: Float, playerPos: Vector2 = null, enemies : ArrayBuffer[Enemy]): Unit = {
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

        val left  = Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val right = Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        val up    = Gdx.input.isKeyPressed(Input.Keys.UP)
        val down  = Gdx.input.isKeyPressed(Input.Keys.DOWN)

        val horizontal = (if (left) -1 else 0) + (if (right) 1 else 0)
        val vertical   = (if (up) 1 else 0) + (if (down) -1 else 0)

        val isDiagonal = horizontal != 0 && vertical != 0
        val moveFactor = if (isDiagonal) diagonalFactor else 1f

        position.x += horizontal * speed * dt * moveFactor
        position.y += vertical   * speed * dt * moveFactor

        cooldownTimer -= dt
        if (cooldownTimer <= 0) {
          val closest = getClosestEnemy(enemies)
          if (closest != null) {
            val distance = closest.getPosition.dst(getPosition)
            if (distance < weapon.range){
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

  def getHit(ennemy: Enemy): Unit = {
    hp -= ennemy.damage()
    println(s"PLAY")
    println(s"PLAYER GOT HIT : $hp HP left")

    // Direction inverse de l'ennemi vers le joueur
    knockbackDir = new Vector2(position).sub(ennemy.getPosition).nor()
    knockbackTimer = knockbackDuration
  }

  private var weapon: Weapon = new Bow()

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

  def attack(ennemy: Enemy): Projectile = {
    println("PLAYER ATTACK")
    val from : Vector2 = getPosition
    val to : Vector2 = ennemy.getPosition
    val direction : Vector2 = new Vector2(to).sub(from).nor()
    weapon.attack(from, direction)
  }

}
