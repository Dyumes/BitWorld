// Abstract base class for all weapons in the game.

package ch.hevs.gdx2d.weapons_abilities

import ch.hevs.gdx2d.Entity.Enemy
import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.math.Vector2
import scala.collection.mutable.ArrayBuffer


abstract class Weapon(val name: String) {
  def range: Float                     // Maximum attack distance
  var dmg: Int                         // Base damage dealt
  def attackSpeed: Float               // Frequency of attacks
  var piercePower: Int = 1             // How many enemies can be hit before the projectile disappears
  val description: String = ""         // Optional weapon description for UI
  var debug = false                    // Flag for debug logging

  private var cooldownTimer: Float = 0f // Tracks cooldown between attacks

  // Determines if the weapon can attack based on elapsed time
  def canAttack(dt: Float): Boolean = {
    cooldownTimer -= dt
    if (cooldownTimer <= 0) {
      cooldownTimer = 1f / attackSpeed
      true
    } else {
      false
    }
  }

  // Returns a new projectile instance
  def attack(position: Vector2, direction: Vector2): Projectile

  // Optional method for drawing the weapon or attack animation
  def draw(g: GdxGraphics, dt: Float, position: Vector2, direction: Vector2): Unit = {}
}

// Bow weapon: shoots a single fast arrow.
class Bow() extends Weapon("Bow") {
  override val range: Float = 1000
  override var dmg: Int = 20
  override val attackSpeed = 1.5f
  private val piercing: Int = 1
  private val projectilSpeed: Float = 1500
  override val description: String = "A bow shooting one arrow at a time"

  // Keeps track of current facing direction for rendering
  private val currentDirection: Vector2 = new Vector2(1, 0)

  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("BOW ATTACK")
    val offset = 50f
    val startPos = position.cpy().add(direction.cpy().nor().scl(offset))
    new Projectile(startPos, direction.nor(), "arrow", projectilSpeed, dmg, piercing)
  }

  override def draw(g: GdxGraphics, dt: Float, position: Vector2, direction: Vector2): Unit = {
    // Smoothly interpolate toward current direction
    val targetDirection = direction.cpy().nor()
    val interpolationSpeed = 10f
    currentDirection.lerp(targetDirection, dt * interpolationSpeed)

    val drawPos = new Vector2(position).add(currentDirection.cpy().scl(50f))
    val angle = currentDirection.angle()

    g.drawTransformedPicture(drawPos.x, drawPos.y, angle, 1f, ProjectileAssets.bowImage)
  }
}

// Spear weapon: slower than bow, but pierces through two enemies.
class Spear() extends Weapon("Spear") {
  override val range: Float = 1000
  override var dmg: Int = 30
  override val attackSpeed = 0.5f
  private val piercing: Int = 2
  private val projectilSpeed: Float = 300
  override val description: String = "A spear piercing 2 times"

  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("SPEAR ATTACK")
    val offset = 20f
    val startPos = position.cpy().add(direction.cpy().nor().scl(offset))
    new Projectile(startPos, direction.nor(), "spear", projectilSpeed, dmg, piercing)
  }
}

// Orb weapon: does not shoot projectiles, but rotates around the player and damages nearby enemies.
class Orb() extends Weapon("Orb") {
  override val range: Float = 10f              // Not used directly
  override val attackSpeed: Float = 0.5f       // Not used directly
  override var dmg: Int = 50

  private val size: Float = 10f
  private val rotationSpeed: Float = 200f
  private var currentAngle: Float = 0f
  private val distanceFromPlayer: Float = 100f
  override val description: String = "An orbiting orb dealing damage to any near enemies"

  private val image: BitmapImage = new BitmapImage("data/images/weapons/orb.png")

  // Tracks enemies currently in contact with the orb
  private val currentContacts = scala.collection.mutable.Set[Enemy]()

  // Updates the orb's position and checks collisions with enemies
  def update(dt: Float, playerPos: Vector2, enemies: ArrayBuffer[Enemy]): Unit = {
    currentAngle = (currentAngle + rotationSpeed * dt) % 360
    val orbPos = calculateOrbPosition(playerPos)

    enemies.foreach { enemy =>
      if (!enemy.isAlive()) {
        currentContacts -= enemy
      } else {
        val dist = orbPos.dst(enemy.getPosition)
        val inRange = dist <= size

        if (inRange && !currentContacts.contains(enemy)) {
          enemy.getHit(this, orbPos)
          println(s"Orb hit: ${enemy.getPosition}")
          currentContacts += enemy
        } else if (!inRange && currentContacts.contains(enemy)) {
          currentContacts -= enemy
        }
      }
    }
  }

  // Computes the orb's position based on angle and player position
  private def calculateOrbPosition(playerPos: Vector2): Vector2 = {
    new Vector2(
      playerPos.x + math.cos(math.toRadians(currentAngle)).toFloat * distanceFromPlayer,
      playerPos.y + math.sin(math.toRadians(currentAngle)).toFloat * distanceFromPlayer
    )
  }

  override def draw(g: GdxGraphics, dt: Float, position: Vector2, direction: Vector2): Unit = {
    val orbPos = calculateOrbPosition(position)
    g.drawTransformedPicture(orbPos.x, orbPos.y, currentAngle, 1f, image)
  }

  // The orb does not generate projectiles
  override def attack(position: Vector2, direction: Vector2): Projectile = null
}

// Generic projectile class used by most weapons (except the Orb).
class Projectile(
                  var position: Vector2,
                  val direction: Vector2,
                  val form: String,     // Used to determine asset and type
                  val speed: Float,
                  val damage: Int,
                  val pierce: Int
                ) {
  private var pierced: Int = pierce
  var active: Boolean = true
  private var currentEnemyHit: Option[Enemy] = None
  private val enemiesAlreadyHit = scala.collection.mutable.Set[Enemy]()

  // Determines if this projectile can collide with the given enemy
  def isCollidingWith(enemy: Enemy): Boolean = {
    if (enemiesAlreadyHit.contains(enemy)) false
    else currentEnemyHit match {
      case None => true
      case Some(e) if e != enemy => true
      case _ => false
    }
  }

  // Handles logic when hitting an enemy (damage, pierce count, etc.)
  def onHit(enemy: Enemy): Unit = {
    if (!enemiesAlreadyHit.contains(enemy)) {
      currentEnemyHit = Some(enemy)
      enemiesAlreadyHit += enemy
      pierced -= 1
      if (pierced <= 0) active = false
    }
  }

  // Updates projectile movement and state
  def update(dt: Float): Unit = {
    position.x += direction.x * speed * dt
    position.y += direction.y * speed * dt

    currentEnemyHit.foreach { enemy =>
      if (position.dst(enemy.getPosition) > enemy.width) {
        currentEnemyHit = None
      }
    }
  }

  // Renders the projectile using the appropriate image and rotation
  def draw(g: GdxGraphics): Unit = {
    val image = form match {
      case "arrow" => ProjectileAssets.arrowImage
      case "spear" => ProjectileAssets.spearImage
      case "scala" => ProjectileAssets.scalaImage
      case _ =>
        println("OTHER TYPE OF PROJECTILE")
        return
    }

    g.drawTransformedPicture(position.x, position.y, direction.angle(), 1f, image)
  }

  // Returns width based on projectile type
  def width: Float = form match {
    case "scala" => ProjectileAssets.scalaWidth.toFloat
    case "arrow" => ProjectileAssets.arrowWidth.toFloat
    case "spear" => ProjectileAssets.spearWidth.toFloat
    case _ => 10f
  }

  // Returns height based on projectile type
  def height: Float = form match {
    case "scala" => ProjectileAssets.scalaHeight.toFloat
    case "arrow" => ProjectileAssets.arrowHeight.toFloat
    case "spear" => ProjectileAssets.spearHeight.toFloat
    case _ => 10f
  }
}
