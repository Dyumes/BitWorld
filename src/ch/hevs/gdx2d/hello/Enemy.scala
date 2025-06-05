package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.bitmaps.BitmapImage
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
// TODO Nbr is temporary, need to be change when real enemies are set
class Enemy(var name: String,
             x: Float,
             y: Float,
             width: Float,
             height: Float,
             speed: Int,
             healthPoint: Int,
             damages: Int,
             nbr: Int) extends Entity (x, y, width, height, speed, healthPoint, damages, nbr){
  private val position = new Vector2(x, y)

  private var hp = healthPoint
  private var xp = hp
  private var dmg : Int = damages
  private var lastPressed = "down"
  private var animationLockTimer = 0f

  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms
  private val knockbackDistance = 600f

  def damage(): Int = {
    return dmg
  }

  def getXp(): Int = {
    return xp
  }
  def getHp(): Int = {
    return hp
  }

  def isAlive(): Boolean = {
    if (hp <= 0){
      false
    } else {
      true
    }
  }

  def getSize() : Vector2 = {
    return new Vector2(width, height)
  }

  // Update the position of the player matching to input
  def update(dt: Float, playerPos : Vector2, enemies : ArrayBuffer[Enemy] = null): Unit = {
    if (isAlive() == false){
      println("ENEMY DEAD")
    } else {
      if (knockbackTimer > 0) {
        knockbackTimer -= dt
        val t = 1f - (knockbackTimer / knockbackDuration)
        val strength = Interpolation.swingOut.apply(1f - t) * knockbackDistance
        position.add(knockbackDir.cpy().scl(strength * dt))
      } else {
        if (animationLockTimer <= 0) {
          val direction = new Vector2(playerPos).sub(position).nor()

          if (Math.abs(direction.x) > Math.abs(direction.y)) {
            lastPressed = if (direction.x > 0) "right" else "left"
          } else {
            lastPressed = if (direction.y > 0) "up" else "down"
          }
        }

        position.mulAdd(new Vector2(playerPos).sub(position).nor(), speed * dt)
      }

    }

  }

  // TODO : Placeholder to check drawing and movement logic, need to be change
  override def draw(g: GdxGraphics): Unit = {
    generateFrame(g, Gdx.graphics.getDeltaTime)
  }


  override def getPosition: Vector2 = position.cpy()



  def getHit(player: Player): Unit = {
    lastPressed = "hit"
    animationLockTimer = 0.3f // 300ms d'animation hit
    hp -= player.damage()
    knockbackDir = new Vector2(position).sub(player.getPosition).nor()
    knockbackTimer = knockbackDuration
  }

  def getHit(projectile: Projectile): Unit = {
    lastPressed = "hit"
    animationLockTimer = 0.3f // 300ms d'animation hit
    hp -= projectile.damage
    projectile.onHit()
  }

  def takeDamage(dmg : Int): Unit = {
    hp -= dmg
  }

  def pushAway(pushVec: Vector2): Unit = { // Avoid collision between ennemies
    position.add(pushVec)
  }

  private var currentFrame = 0
  private var animationTimer = 0f
  private val frameDuration = 0.1f // Durée d'affichage de chaque frame (en secondes)

  private var lastAnimation: String = "down"

  def generateFrame(g: GdxGraphics, dt: Float): Unit = {
    if (animationLockTimer > 0) {
      animationLockTimer -= dt
    }
    val (walkDownFrames, walkUpFrames, walkLeftFrames, walkRightFrames, hitFrames) = name match {
      case "goblin" => (
        if (isAlive() == true){
          (
            EnemyAnimations.goblinWalkDown,
            EnemyAnimations.goblinWalkUp,
            EnemyAnimations.goblinWalkLeft,
            EnemyAnimations.goblinWalkRight,
            EnemyAnimations.goblinHit
          )
        } else {
          (
          EnemyAnimations.goblinDeathDown,
          EnemyAnimations.goblinDeathUp,
          EnemyAnimations.goblinDeathLeft,
          EnemyAnimations.goblinDeathRight,
          Array[BitmapImage]()
          )
        }
      )
      case _ => (
        EnemyAnimations.goblinWalkDown,
        EnemyAnimations.goblinWalkUp,
        EnemyAnimations.goblinWalkLeft,
        EnemyAnimations.goblinWalkRight,
        EnemyAnimations.goblinHit
      )
    }

    val activeFrames = lastPressed match {
      case "down"  => walkDownFrames
      case "up"    => walkUpFrames
      case "left"  => walkLeftFrames
      case "right" => walkRightFrames
      case "hit"   => hitFrames
      case _       => walkDownFrames
    }

    // Si on change d'animation, reset currentFrame à 0
    if (lastAnimation != lastPressed) {
      currentFrame = 0
      animationTimer = 0
      lastAnimation = lastPressed
    }

    if (activeFrames.nonEmpty) {
      animationTimer += dt
      if (animationTimer >= frameDuration) {
        animationTimer = 0
        currentFrame = (currentFrame + 1) % activeFrames.length
      }

      g.drawTransformedPicture(position.x, position.y, 0, 0.5f, 0, 1, activeFrames(currentFrame))
    }


  }


}
