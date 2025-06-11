package ch.hevs.gdx2d.weapons_abilities

import ch.hevs.gdx2d.Entity.Enemy
import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable.ArrayBuffer

abstract class Weapon(val name: String, val baseDmg: Int) {
  def range: Float
  var dmg : Int
  def attackSpeed: Float
  var piercePower : Int = 1
  private val enemiesHit = scala.collection.mutable.Set[Enemy]()
  val description : String = ""


  private var cooldownTimer: Float = 0f

  def canAttack(dt: Float): Boolean = {
    cooldownTimer -= dt
    if (cooldownTimer <= 0) {
      cooldownTimer = 1f / attackSpeed
      true
    } else {
      false
    }
  }

  def attack(position: Vector2, direction: Vector2): Projectile
  def draw(g: GdxGraphics, dt: Float, position: Vector2, direction: Vector2): Unit = {}
}


class Bow() extends Weapon("Bow", 20) {
  override val range: Float = 1000
  override var dmg: Int = 20
  override val attackSpeed = 2f
  private val piercing: Int = 1
  private val projectilSpeed: Float = 1500
  override val description: String = "A bow shooting one arrow at a time"

  private var imageLoaded: Boolean = false
  var image: BitmapImage = null

  // Direction actuelle interpolée
  private var currentDirection: Vector2 = new Vector2(1, 0)

  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("BOW ATTACK")
    val offset = 50f
    val startPos = position.cpy().add(direction.cpy().nor().scl(offset))
    new Projectile(startPos, direction.nor(), "arrow", projectilSpeed, dmg, piercing)
  }

  def isEquiped(): Boolean = false

  override def draw(g: GdxGraphics, dt: Float, position: Vector2, direction: Vector2): Unit = {
    if (!imageLoaded) {
      image = new BitmapImage("data/images/weapons/bow.png")
      imageLoaded = true
    }

    // Interpolation vers la direction cible
    val targetDirection = direction.cpy().nor()
    val interpolationSpeed = 10f
    currentDirection.lerp(targetDirection, dt * interpolationSpeed)

    val drawPos = new Vector2(position).add(currentDirection.cpy().scl(50f))
    val angle = currentDirection.angle()

    g.drawTransformedPicture(drawPos.x, drawPos.y, angle, 1f, image)
  }
}

class Spear() extends Weapon("Spear", 30){
  override val range : Float = 1000
  override var dmg : Int = 30
  override val attackSpeed = 1f
  private val piercing : Int = 2
  private val projectilSpeed : Float = 300
  override val description: String = "A spear piercing 2 times"

  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("SPEAR ATTACK")
    val offset = 20f
    val startPos = position.cpy().add(direction.cpy().nor().scl(offset))
    new Projectile(startPos, direction.nor(), "spear", projectilSpeed, dmg, piercing)
  }

  def isEquiped(): Boolean = {
    return false
  }
}

class Orb() extends Weapon("Orb", 50) {
  override val range: Float = 10f //only for weapon class match
  override val attackSpeed: Float = 0.5f //only for weapon class match

  override var dmg: Int = 50
  private val size : Float = 10f
  private val rotationSpeed: Float = 200f
  private var currentAngle: Float = 0f
  private val distanceFromPlayer: Float = 100
  override val description: String = "An orbiting orb dealing damage to any near enemies"

  private var image: BitmapImage = new BitmapImage("data/images/weapons/orb.png")

  private val currentContacts = scala.collection.mutable.Set[Enemy]()

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

  override def attack(position: Vector2, direction: Vector2): Projectile = null
}

class Projectile(
                  var position: Vector2,
                  val direction: Vector2,
                  val form: String,
                  val speed: Float,
                  val damage: Int,
                  val pierce: Int,
                ) {
  private var pierced: Int = pierce
  var active: Boolean = true
  private var currentEnemyHit: Option[Enemy] = None // Ennemi actuellement en collision
  private val enemiesAlreadyHit = scala.collection.mutable.Set[Enemy]() // Ennemis déjà touchés

  def isCollidingWith(enemy: Enemy): Boolean = {
    // Si on a déjà touché cet ennemi, pas de nouvelle collision
    if (enemiesAlreadyHit.contains(enemy)) false
    // Si c'est un nouvel ennemi ou si on a quitté le précédent
    else currentEnemyHit match {
      case None => true
      case Some(e) if e != enemy => true
      case _ => false
    }
  }

  def onHit(enemy: Enemy): Unit = {
    if (!enemiesAlreadyHit.contains(enemy)) {
      currentEnemyHit = Some(enemy)
      enemiesAlreadyHit += enemy
      pierced -= 1
      if (pierced <= 0) active = false
    }
  }

  def update(dt: Float): Unit = {
    position.x += direction.x * speed * dt
    position.y += direction.y * speed * dt

    // Vérifier si on a quitté l'ennemi actuellement touché
    currentEnemyHit.foreach { enemy =>
      if (position.dst(enemy.getPosition) > enemy.width) {
        currentEnemyHit = None
      }
    }
  }

  def draw(g: GdxGraphics): Unit = {
    form match {
      case "arrow" =>
        //g.drawFilledCircle(position.x, position.y, 5f, Color.GREEN)
        g.drawTransformedPicture(position.x, position.y, direction.angle(), 1f, new BitmapImage("data/images/weapons/projectiles/placeholder_arrow.png"))
      case "spear" =>
        g.drawTransformedPicture(position.x, position.y, direction.angle(), 1f, new BitmapImage("data/images/weapons/projectiles/spear.png"))
      case _ =>
        println("OTHER TYPE OF PROJECTILE")
    }

  }
}