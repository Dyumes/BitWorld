package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable.ArrayBuffer

abstract class Weapon(val name: String, val damage: Int) {
  val range : Float
  val dmg : Int = damage


  def attack(position : Vector2 = null, direction: Vector2 = null): Projectile = null
}

class Bow() extends Weapon("Bow", 20){
  override val range : Float = 1000
  override val dmg : Int = 20
  private val piercing : Int = 1
  private val projectilSpeed : Float = 2000

  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("BOW ATTACK")
    new Projectile(position.cpy(), direction.nor(), "arrow", projectilSpeed, dmg, piercing)
  }

  def isEquiped(): Boolean = {
    return false
  }
}

class Spear() extends Weapon("Spear", 30){
  override val range : Float = 1000
  override val dmg : Int = 30
  private val piercing : Int = 2
  private val projectilSpeed : Float = 1000

  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("SPEAR ATTACK")
    new Projectile(position.cpy(), direction.nor(), "spear", projectilSpeed, dmg, piercing)
  }

  def isEquiped(): Boolean = {
    return false
  }
}

class Projectile(
                var position: Vector2,
                val direction: Vector2,
                val form: String, // @TODO FORMS TO DEFINE, AND REPLACE THEM WITH IMAGES
                val speed: Float,
                val damage: Int,
                val pierce: Int)
{
  private var pierced : Int = pierce
  var active : Boolean = true

  def onHit(): Unit = {
    pierced -= 1
    if (pierced == 0) return active = false
  }


  def update(dt: Float) : Unit = {
    position.x += direction.x * speed * dt
    position.y += direction.y  * speed * dt
  }

  def draw(g: GdxGraphics): Unit = {
    form match {
      case "arrow" =>
        g.drawFilledCircle(position.x, position.y, 1f, Color.GREEN)
      case "spear" =>
        g.drawFilledCircle(position.x, position.y, 1f, Color.RED)
      case _ =>
        println("OTHER TYPE OF PROJECTILE")
    }

  }
}