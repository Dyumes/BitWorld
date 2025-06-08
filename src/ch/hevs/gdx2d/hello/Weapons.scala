package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable.ArrayBuffer

abstract class Weapon(val name: String, var damage: Int) {
  val range : Float
  var dmg : Int = damage
  var piercePower : Int = 1


  def attack(position: Vector2, direction: Vector2): Projectile
}

class Bow() extends Weapon("Bow", 20){
  override val range : Float = 1000
  dmg = 20
  private val projectilSpeed : Float = 500
  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("BOW ATTACK")
    new Projectile(position.cpy(), direction.nor(), "arrow", projectilSpeed, dmg, piercePower)
  }

  def isEquiped(): Boolean = {
    return false
  }
}

class Spear() extends Weapon("Spear", 30){
  override val range: Float = 1000
  private val projectileSpeed: Float = 400

  override def attack(position: Vector2, direction: Vector2): Projectile = {
    println("SPEAR ATTACK")
    new Projectile(position.cpy(), direction.nor(), "spear", projectileSpeed, dmg, piercePower)
  }
}

class Projectile(
                  var position: Vector2,
                  val direction: Vector2,
                  val form: String, // @TODO FORMS TO DEFINE, AND REPLACE THEM WITH IMAGES
                  val speed: Float,
                  val damage: Int,
                  var pierce: Int)
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
        g.drawFilledCircle(position.x, position.y, 10f, Color.GREEN)
      case "spear" =>
        g.drawFilledRectangle(position.x, position.y, 20f, 5f,0,Color.BLUE)
      case _ =>
        g.drawFilledCircle(position.x, position.y, 10f, Color.WHITE)
    }

  }
}