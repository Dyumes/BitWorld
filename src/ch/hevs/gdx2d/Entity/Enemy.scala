package ch.hevs.gdx2d.Entity

import ch.hevs.gdx2d.components.bitmaps.Spritesheet
import ch.hevs.gdx2d.weapons_abilities._
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.{Interpolation, Vector2}

import scala.collection.mutable.ArrayBuffer
/**
 *
 * A Player class for controlling the player and the camera following all along
 *
 */
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
  private val maxHp = healthPoint
  private val xp = hp
  private val dmg: Int = damages
  private var lastPressed = "down"
  private var animationLockTimer = 0f

  private var isDead = false
  private var deathAnimationDone = false

  // For Mudry Boss
  var enemyProjectiles : ArrayBuffer[Projectile] = ArrayBuffer[Projectile]()
  private var isAttacking = false
  private var attackLockTimer = 0f
  private val attackLockDuration = 0.5f
  // Speciall attack mudry
  private var sprayDuration = 3f           // durée totale du spray
  private var sprayTimer = 0f              // timer courant pour le spray
  private val sprayInterval = 0.1f         // délai entre chaque projectile dans la rafale
  private var sprayIntervalTimer = 0f      // timer pour cadence de tir
  private val sprayStartAngle = -60f
  private val sprayEndAngle = 60f
  private var sprayActive = false
  private val sprayCooldown: Float = 6f     // cooldown entre chaque rafale// indique si le spray est en cours



  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms

  private var knockbackTargetPos: Vector2 = position.cpy()
  private var isKnockbackActive = false

  def damage(): Int = {
    return dmg
  }

  def getXp: Int = {
    return xp
  }

  def getHp: Int = {
    return hp
  }

  def getHpMax: Int = {
    return maxHp
  }

  def isAlive(): Boolean = {
    !isDead
  }

  private def returnSprite(state: String): Unit = {
    if (state == "alive"){
      name match {
        case "goblin" =>
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
        case "orc" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/orc/orc3_hurt_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton" =>
          SPRITE_WIDTH = 128
          SPRITE_HEIGHT = 128
          ss = new Spritesheet("data/images/skeleton/skeleton_hurt.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "wizard" =>
          SPRITE_WIDTH = 128
          SPRITE_HEIGHT = 128
          ss = new Spritesheet("data/images/wizard/wizard_hurt.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "boss" =>
          SPRITE_WIDTH = 256
          SPRITE_HEIGHT = 256
          ss = new Spritesheet("data/images/boss/mudry_boss_hurt.png", SPRITE_WIDTH, SPRITE_HEIGHT)
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
        case "orc" =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/orc/orc3_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "skeleton" =>
          SPRITE_WIDTH = 128
          SPRITE_HEIGHT = 128
          ss = new Spritesheet("data/images/skeleton/skeleton_death.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "wizard" =>
          SPRITE_WIDTH = 128
          SPRITE_HEIGHT = 128
          ss = new Spritesheet("data/images/wizard/wizard_death.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case "boss" =>
          SPRITE_WIDTH = 256
          SPRITE_HEIGHT = 256
          ss = new Spritesheet("data/images/boss/mudry_boss_hurt.png", SPRITE_WIDTH, SPRITE_HEIGHT)
        case _ =>
          SPRITE_WIDTH = 64
          SPRITE_HEIGHT = 64
          ss = new Spritesheet("data/images/goblin/orc1_death_full.png", SPRITE_WIDTH, SPRITE_HEIGHT)
      }
    }

  }

  // Update the position of the player matching to input
  // Ajoute cette variable au début de ta classe Enemy (avec les autres variables)
  private var sprayCooldownTimer = 0f // Timer cooldown entre sprays, distinct de sprayTimer

  // Dans ta méthode update:
  def update(dt: Float, playerPos: Vector2, enemies: ArrayBuffer[Enemy] = null): Unit = {
    if (animationLockTimer > 0f) {
      animationLockTimer -= dt
      if (animationLockTimer <= 0f && !isDead) {
        returnSprite("alive")
        currentFrame = 0
        animationTimer = 0f
      }
    }

    if (!isAlive()) {
      println("ENEMY DEAD")
    } else {
      if (name == "boss") {
        if (sprayActive) {
          // Spray en cours
          sprayIntervalTimer -= dt
          if (sprayIntervalTimer <= 0f) {
            sprayIntervalTimer = sprayInterval

            val elapsed = sprayDuration - sprayTimer
            val t = elapsed / sprayDuration
            val angle = sprayStartAngle + t * (sprayEndAngle - sprayStartAngle)

            val baseDir = playerPos.cpy().sub(getPosition).nor()
            val dir = baseDir.cpy().setAngle(baseDir.angle() + angle).nor()

            val projectile = new Projectile(getPosition.cpy(), dir, "scala", 300f, 30, 1)
            enemyProjectiles += projectile
          }

          sprayTimer -= dt
          if (sprayTimer <= 0f) {
            sprayActive = false
            sprayCooldownTimer = sprayCooldown  // lance le cooldown entre sprays
          }

        } else if (sprayCooldownTimer > 0f) {
          // Cooldown entre sprays
          sprayCooldownTimer -= dt

        } else if (canRangedAttack(dt)) {
          // Attaque classique (hors spray)
          isAttacking = true
          val baseDir = playerPos.cpy().sub(getPosition).nor()
          val angles = Seq(-15f, 0f, 15f)

          angles.foreach { angle =>
            val dir = baseDir.cpy().setAngle(baseDir.angle() + angle).nor()
            val projectile = new Projectile(getPosition.cpy(), dir, "scala", 300f, 1000, 1)
            enemyProjectiles += projectile
          }
        }

        // Déclenchement du spray uniquement si pas actif ET cooldown terminé
        if (!sprayActive && sprayCooldownTimer <= 0f) {
          sprayActive = true
          sprayDuration = 3f       // durée du spray
          sprayIntervalTimer = 0f  // timer interne pour cadence de tir
          sprayTimer = sprayDuration
        }
      }

      if (isKnockbackActive) {
        val progress = 1f - (knockbackTimer / knockbackDuration)
        val alpha = Interpolation.smooth.apply(progress)
        position.set(
          knockbackTargetPos.x * alpha + position.x * (1 - alpha),
          knockbackTargetPos.y * alpha + position.y * (1 - alpha)
        )

        if (position.dst2(knockbackTargetPos) < 5f) {
          isKnockbackActive = false
        }

        knockbackTimer -= dt
        if (knockbackTimer <= 0f) {
          isKnockbackActive = false
        }
      } else {
        if (animationLockTimer <= 0) {
          if(isAttacking){
            attackLockTimer += dt
            if(attackLockTimer >= attackLockDuration){
              isAttacking = false
            }
          } else {
            val direction = new Vector2(playerPos).sub(position).nor()

            if (Math.abs(direction.x) > Math.abs(direction.y)) {
              lastPressed = if (direction.x > 0) "right" else "left"
            } else {
              lastPressed = if (direction.y > 0) "up" else "down"
            }
          }
        }

        position.mulAdd(new Vector2(playerPos).sub(position).nor(), speed * dt)
      }
    }
  }


  override def draw(g: GdxGraphics): Unit = {
    generateFrame(g, Gdx.graphics.getDeltaTime)
  }

  override def getPosition: Vector2 = position.cpy()


  def getHit(player: Player): Unit = {
    if (!isDead) {
      returnSprite("hurt")
      animationLockTimer = 0.3f
      hp -= player.damage()

      val dir = new Vector2(position).sub(player.getPosition).nor()
      knockbackDir = dir
      knockbackTargetPos = position.cpy().add(knockbackDir.scl(50f))
      knockbackTimer = knockbackDuration
      isKnockbackActive = true

      if (hp <= 0) {
        isDead = true
        returnSprite("dead")
        currentFrame = 0
        animationTimer = 0f
      }
    }
  }


  def getHit(Stinky : Zone) : Unit = {
    if (!isDead) {
      returnSprite("hurt")
      animationLockTimer = 0.3f
      hp -= Stinky.damage

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

  def getHit(projectile: Projectile): Unit = {
    val knockbackDirection = this.getPosition.cpy().sub(projectile.position)
    val knockbackForce = projectile match {
      case _: Bow => 50f  // Knockback léger pour les flèches
      case _: Spear => 80f // Knockback plus fort pour les lances
      case _ => 40f       // Valeur par défaut
    }
    this.applyProjectileKnockback(knockbackDirection, knockbackForce)
    if (!isDead) {
      returnSprite("hurt")
      animationLockTimer = 0.3f
      hp -= projectile.damage

      if (hp <= 0) {
        isDead = true
        returnSprite("dead")
        currentFrame = 0
        animationTimer = 0f
      }
    }
  }

  def applyProjectileKnockback(direction: Vector2, force: Float = 50f): Unit = {
    if (!isKnockbackActive) {
      knockbackDir = direction.nor()
      knockbackTargetPos = position.cpy().add(knockbackDir.scl(force))
      knockbackTimer = knockbackDuration
      isKnockbackActive = true
    }
  }

  private var rangedCooldown: Float = 0f
  private val rangedAttackSpeed: Float = 0.5f  // une attaque toutes les 2 secondes
  def canRangedAttack(dt: Float): Boolean = {
    rangedCooldown -= dt
    if (rangedCooldown <= 0f) {
      rangedCooldown = 1f / rangedAttackSpeed
      true
    } else false
  }


  def pushAway(pushVec: Vector2): Unit = { // Avoid collision between ennemies
    position.add(pushVec)
  }

  private var animationTimer = 0f
  private val frameDuration = 0.1f // Durée d'affichage de chaque frame (en secondes)

  private var SPRITE_WIDTH = 64
  private var SPRITE_HEIGHT = 64

  private var ss: Spritesheet = null

  private var currentFrame = 0

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
