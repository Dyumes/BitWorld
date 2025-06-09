package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.bitmaps.{BitmapImage, Spritesheet}
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
             nbr: Int) extends Entity (x, y, width, height, speed, healthPoint, damages, nbr) {
  private val position = new Vector2(x, y)

  private var hp = healthPoint
  private var xp = hp
  private var dmg: Int = damages
  private var lastPressed = "down"
  private var animationLockTimer = 0f

  private var isDead = false
  private var deathAnimationDone = false



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
    !isDead
  }

  def getSize(): Vector2 = {
    return new Vector2(width, height)
  }

  def returnSprite(state: String): Unit = {
    if (state == "alive"){
      name match {
        case "goblin" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_walk_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton_distance" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_walk_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "orc" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/orc/orc3_walk_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton" =>
          SPRITE_WIDTH = 128
          SPRITE_HEIGHT = 128
          ss = new Spritesheet("data/images/skeleton/Skeleton_Move.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "wizard" =>
          SPRITE_WIDTH = 128
          SPRITE_HEIGHT = 128
          ss = new Spritesheet("data/images/wizard/wizard.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "boss" =>
          SPRITE_WIDTH = 256
          SPRITE_HEIGHT = 256
          ss = new Spritesheet("data/images/boss/mudry_boss.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case _ =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_walk_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
      }
    } else if (state == "hurt"){
      name match {
        case "goblin" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton_distance" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "orc" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "wizard" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "boss" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case _ =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
      }
    }else if (state == "dead"){
      name match {
        case "goblin" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton_distance" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "orc" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "wizard" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "boss" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case _ =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
      }
    }

  }

  // Update the position of the player matching to input
  def update(dt: Float, playerPos: Vector2, enemies: ArrayBuffer[Enemy] = null): Unit = {
    if (animationLockTimer > 0f) {
      animationLockTimer -= dt
      if (animationLockTimer <= 0f && !isDead) {
        // Restaurer la bonne spritesheet selon le type d'ennemi
        returnSprite("alive")
        currentFrame = 0
        animationTimer = 0f
      }
    }

    if (isAlive() == false) {
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

  override def draw(g: GdxGraphics): Unit = {
    generateFrame(g, Gdx.graphics.getDeltaTime)
    //Hitbox
    g.setColor(Color.RED)
    g.drawString(position.x, position.y, s"$nbr")
    g.drawRectangle(position.x, position.y, width, height, 0)
  }


  override def getPosition: Vector2 = position.cpy()


  def getHit(player: Player): Unit = {
    if (!isDead) {
      returnSprite("hurt")
      animationLockTimer = 0.3f
      hp -= player.damage()
      knockbackDir = new Vector2(position).sub(player.getPosition).nor()
      knockbackTimer = knockbackDuration

      if (hp <= 0) {
        isDead = true
        returnSprite("dead")
        currentFrame = 0
        animationTimer = 0f
      }
    }
  }

  def getHit(orb: Orb, orbPos: Vector2): Unit = {
    if (!isDead) {
      returnSprite("hurt")
      animationLockTimer = 0.3f
      hp -= orb.dmg
      knockbackDir = new Vector2(position).sub(orbPos).nor()
      knockbackTimer = knockbackDuration

      if (hp <= 0) {
        isDead = true
        returnSprite("dead")
        currentFrame = 0
        animationTimer = 0f
      }
    }
  }

  def getHit(g: GdxGraphics, enemies: ArrayBuffer[Enemy], projectile: Projectile): Unit = {
    if (!isDead) {
      returnSprite("hurt")
      animationLockTimer = 0.3f
      hp -= projectile.damage
      projectile.onHit(g, enemies)

      if (hp <= 0) {
        isDead = true
        returnSprite("dead")
        currentFrame = 0
        animationTimer = 0f
      }
    }
  }


  def takeDamage(dmg: Int): Unit = {
    hp -= dmg
  }

  def pushAway(pushVec: Vector2): Unit = { // Avoid collision between ennemies
    position.add(pushVec)
  }

  private var animationTimer = 0f
  private val frameDuration = 0.1f // Durée d'affichage de chaque frame (en secondes)

  private var lastAnimation: String = "down"

  private var SPRITE_WIDTH = 64
  private var SPRITE_HEIGHT = 64
  private val FRAME_TIME = 0.15 // Duration of each frame

  private var ss: Spritesheet = null

  private val textureX = 0
  private var textureY = 1

  /**
   * Animation related parameters
   */
  private var dt = 0
  private var currentFrame = 0
  private val nFrames = 6

  returnSprite("alive")

  def generateFrame(g: GdxGraphics, dt: Float): Unit = {
    val dirIndex = lastPressed match {
      case "down"  => 0
      case "up"    => 1
      case "left"  => 2
      case "right" => 3
      case _       => 0
    }

    if (isDead && !deathAnimationDone) {
      animationTimer += dt
      if (animationTimer >= frameDuration) {
        animationTimer = 0
        currentFrame += 1
        if (currentFrame >= ss.sprites(dirIndex).length) {
          currentFrame = ss.sprites(dirIndex).length - 1
          deathAnimationDone = true
        }
      }
    } else if (!isDead) {
      animationTimer += dt
      if (animationTimer >= frameDuration) {
        animationTimer = 0
        currentFrame = (currentFrame + 1) % ss.sprites(dirIndex).length
      }
    }

    val img = ss.sprites(dirIndex)(currentFrame)
    g.draw(img, position.x - SPRITE_WIDTH / 2, position.y - SPRITE_HEIGHT / 2)

  }

  def isReadyToBeRemoved(): Boolean = {
    isDead && deathAnimationDone
  }




}
