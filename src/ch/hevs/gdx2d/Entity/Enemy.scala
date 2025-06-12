/**
 * Enemy Class
 * -----------
 * This class represents an enemy entity in the game. It manages all the logic related to the enemy's
 * behavior, including movement, health, attacks (including special attacks like sprays for bosses),
 * knockback effects, animation states, and interactions with the player and projectiles.
 *
 * Key Features:
 * - Handles different enemy types (goblin, orc, skeleton, wizard, boss) with specific sprites.
 * - Manages health, damage, and experience points.
 * - Supports ranged and special attacks (spray attack for bosses).
 * - Implements knockback and death animations.
 * - Controls animation frames and sprite rendering.
 * - Handles collision and hit logic from various sources (player, projectiles, zones).
 * - Manages enemy removal after death animation is complete.
 */

package ch.hevs.gdx2d.Entity

import ch.hevs.gdx2d.components.bitmaps.Spritesheet
import ch.hevs.gdx2d.weapons_abilities._
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.{Interpolation, Vector2}

import scala.collection.mutable.ArrayBuffer

class Enemy(var name: String,
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            speed: Int,
            healthPoint: Int,
            damages: Int,
            nbr: Int) extends Entity (x, y, width, height, speed, healthPoint, damages, nbr) {

  // --- Enemy State Variables ---

  // Position
  private val position = new Vector2(x, y)

  // Health and damage management
  private var hp = healthPoint
  private val maxHp = healthPoint
  private val xp = hp
  private val dmg: Int = damages

  // Animation and state management
  private var lastPressed = "down"           // Last movement direction
  private var animationLockTimer = 0f        // Timer to lock animation state
  private var isDead = false                 // Is the enemy dead?
  private var deathAnimationDone = false     // Has the death animation finished?
  private var animationTimer = 0f            // Animation timer
  private val frameDuration = 0.1f           // Duration of each frame (in seconds)
  private var SPRITE_WIDTH = 64
  private var SPRITE_HEIGHT = 64
  private var ss: Spritesheet = null
  private var currentFrame = 0

  // Projectiles and attack logic (for bosses)
  var enemyProjectiles : ArrayBuffer[Projectile] = ArrayBuffer[Projectile]()
  private var isAttacking = false            // Is the enemy currently attacking?
  private var attackLockTimer = 0f           // Timer to lock attack state
  private val attackLockDuration = 0.5f      // Duration to lock attack state

  // Special spray attack (for boss)
  private var sprayDuration = 3f             // Total duration of the spray attack
  private var sprayTimer = 0f                // Current timer for the spray
  private val sprayInterval = 0.1f           // Delay between each projectile in the spray
  private var sprayIntervalTimer = 0f        // Timer for projectile firing rate in spray
  private val sprayStartAngle = -60f         // Start angle for spray
  private val sprayEndAngle = 60f            // End angle for spray
  private var sprayActive = false            // Is the spray attack active?
  private val sprayCooldown: Float = 6f      // Cooldown between sprays
  private var sprayCooldownTimer = 0f        // Timer for spray cooldown

  // Knockback logic
  private var knockbackDir = new Vector2(0, 0)           // Direction of knockback
  private var knockbackTimer = 0f                        // Timer for knockback duration
  private val knockbackDuration = 0.2f                   // Duration of knockback (200 ms)
  private var knockbackTargetPos: Vector2 = position.cpy() // Target position for knockback
  private var isKnockbackActive = false                  // Is knockback currently active?

  // Spawn state
  var isSpawned : Boolean = false

  // Cooldown management for ranged attacks
  private var rangedCooldown: Float = 0f
  private val rangedAttackSpeed: Float = 0.5f  // One attack every 2 seconds

  // --- Initialization ---
  returnSprite("alive")

  // --- Public API: Getters and State Checks ---

  // Returns the damage value of the enemy
  def damage(): Int = {
    return dmg
  }

  // Returns the experience points given by the enemy
  def getXp: Int = {
    return xp
  }

  // Returns the current health points
  def getHp: Int = {
    return hp
  }

  // Returns the maximum health points
  def getHpMax: Int = {
    return maxHp
  }

  // Returns true if the enemy is alive
  def isAlive(): Boolean = {
    !isDead
  }

  // Returns a copy of the enemy's position
  override def getPosition: Vector2 = position.cpy()

  // Returns true if the enemy is dead and the death animation is finished (ready to be removed)
  def isReadyToBeRemoved(): Boolean = {
    isDead && deathAnimationDone
  }

  // --- Main Update and Draw Methods ---

  // Main update method called every frame, handles all logic (movement, attacks, knockback, etc.)
  def update(dt: Float, playerPos: Vector2, enemies: ArrayBuffer[Enemy] = null): Unit = {
    // Handle animation lock (e.g., after being hit)
    if (animationLockTimer > 0f) {
      animationLockTimer -= dt
      if (animationLockTimer <= 0f && !isDead) {
        returnSprite("alive")
        currentFrame = 0
        animationTimer = 0f
      }
    }

    // If dead, print message (could be used for debugging)
    if (!isAlive()) {
      println("ENEMY DEAD")
    } else {
      // Special logic for boss enemy
      if (name == "boss") {
        if (sprayActive) {
          // Spray attack is active
          sprayIntervalTimer -= dt
          if (sprayIntervalTimer <= 0f) {
            sprayIntervalTimer = sprayInterval

            // Calculate spray angle and direction
            val elapsed = sprayDuration - sprayTimer
            val t = elapsed / sprayDuration
            val angle = sprayStartAngle + t * (sprayEndAngle - sprayStartAngle)

            val baseDir = playerPos.cpy().sub(getPosition).nor()
            val dir = baseDir.cpy().setAngle(baseDir.angle() + angle).nor()

            // Create and add a new projectile for the spray
            val projectile = new Projectile(getPosition.cpy(), dir, "scala", 300f, 30, 1)
            enemyProjectiles += projectile
          }

          // Update spray timer and deactivate when finished
          sprayTimer -= dt
          println(s"SprayTimer : $sprayTimer")
          if (sprayTimer <= 0f) {
            sprayActive = false
            sprayCooldownTimer = sprayCooldown  // Start cooldown between sprays
          }

        } else if (canRangedAttack(dt) && sprayCooldownTimer > 0f) {
          // Regular ranged attack (not spray)
          println("RANGE ATTACK")
          isAttacking = true
          val baseDir = playerPos.cpy().sub(getPosition).nor()
          val angles = Seq(-30f, -15f, 0f, 15f, 30f)

          // Fire projectiles in a spread pattern
          angles.foreach { angle =>
            val dir = baseDir.cpy().setAngle(baseDir.angle() + angle).nor()
            val projectile = new Projectile(getPosition.cpy(), dir, "scala", 300f, 10000, 1)
            enemyProjectiles += projectile
          }
        }
        sprayCooldownTimer -= dt

        // Activate spray attack if not active and cooldown is finished
        if (!sprayActive && sprayCooldownTimer <= 0f) {
          sprayActive = true
          sprayDuration = 3f       // Duration of the spray
          sprayIntervalTimer = 0f  // Internal timer for firing rate
          sprayTimer = sprayDuration
        }
      }

      // Handle knockback logic
      if (isKnockbackActive) {
        val progress = 1f - (knockbackTimer / knockbackDuration)
        val alpha = Interpolation.smooth.apply(progress)
        position.set(
          knockbackTargetPos.x * alpha + position.x * (1 - alpha),
          knockbackTargetPos.y * alpha + position.y * (1 - alpha)
        )

        // End knockback if close enough to target position
        if (position.dst2(knockbackTargetPos) < 5f) {
          isKnockbackActive = false
        }

        knockbackTimer -= dt
        if (knockbackTimer <= 0f) {
          isKnockbackActive = false
        }
      } else {
        // Handle attack lock and movement
        if (animationLockTimer <= 0) {
          if(isAttacking){
            attackLockTimer += dt
            if(attackLockTimer >= attackLockDuration){
              isAttacking = false
            }
          } else {
            // Determine direction to player for movement and animation
            val direction = new Vector2(playerPos).sub(position).nor()

            if (Math.abs(direction.x) > Math.abs(direction.y)) {
              lastPressed = if (direction.x > 0) "right" else "left"
            } else {
              lastPressed = if (direction.y > 0) "up" else "down"
            }
          }
        }

        // Move enemy towards the player
        position.mulAdd(new Vector2(playerPos).sub(position).nor(), speed * dt)
      }
    }
  }

  // Draws the current animation frame of the enemy
  override def draw(g: GdxGraphics): Unit = {
    generateFrame(g, Gdx.graphics.getDeltaTime)
  }

  // --- Animation and Sprite Management ---

  // Sets the sprite and dimensions based on the enemy's state and type
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

  // Generates and draws the current animation frame
  def generateFrame(g: GdxGraphics, dt: Float): Unit = {
    val dirIndex = lastPressed match {
      case "down"  => 0
      case "up"    => 1
      case "left"  => 2
      case "right" => 3
      case _       => 0
    }

    // Handle death animation
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
      // Handle normal animation
      animationTimer += dt
      if (animationTimer >= frameDuration) {
        animationTimer = 0
        currentFrame = (currentFrame + 1) % ss.sprites(dirIndex).length
      }
    }

    // Draw the current frame at the enemy's position
    val img = ss.sprites(dirIndex)(currentFrame)
    g.draw(img, position.x - SPRITE_WIDTH / 2, position.y - SPRITE_HEIGHT / 2)
  }

  // --- Enemy Actions and Interactions ---

  // Handles being hit by the player (melee)
  def getHit(player: Player): Unit = {
    if (!isDead) {
      returnSprite("hurt")
      animationLockTimer = 0.3f
      hp -= player.damage()

      // Apply knockback from player
      val dir = new Vector2(position).sub(player.getPosition).nor()
      knockbackDir = dir
      knockbackTargetPos = position.cpy().add(knockbackDir.scl(50f))
      knockbackTimer = knockbackDuration
      isKnockbackActive = true

      // If health drops to zero, trigger death
      if (hp <= 0) {
        isDead = true
        returnSprite("dead")
        currentFrame = 0
        animationTimer = 0f
      }
    }
  }

  // Handles being hit by a damaging zone (e.g., poison, fire)
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

  // Handles being hit by an orb (projectile)
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

  // Handles being hit by a projectile (arrow, spear, etc.)
  def getHit(projectile: Projectile): Unit = {
    val knockbackDirection = this.getPosition.cpy().sub(projectile.position)
    val knockbackForce = projectile match {
      case _: Bow => 50f  // Light knockback for arrows
      case _: Spear => 80f // Stronger knockback for spears
      case _ => 40f       // Default knockback
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

  // Applies knockback to the enemy in a given direction and force
  def applyProjectileKnockback(direction: Vector2, force: Float = 50f): Unit = {
    if (!isKnockbackActive) {
      knockbackDir = direction.nor()
      knockbackTargetPos = position.cpy().add(knockbackDir.scl(force))
      knockbackTimer = knockbackDuration
      isKnockbackActive = true
    }
  }

  // Pushes the enemy away to avoid collision with other enemies
  def pushAway(pushVec: Vector2): Unit = {
    position.add(pushVec)
  }

  // --- Attack Utilities ---

  // Cooldown management for ranged attacks
  def canRangedAttack(dt: Float): Boolean = {
    rangedCooldown -= dt
    if (rangedCooldown <= 0f) {
      rangedCooldown = 2f
      true
    } else false
  }
}