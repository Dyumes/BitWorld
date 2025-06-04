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

  private var dmg : Int = damages

  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms
  private val knockbackDistance = 600f

  def damage(): Int = {
    return dmg
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
        val diagonalFactor = 0.8f // Useful to avoid faster movement on diagonal
        val direction = new Vector2(playerPos).sub(position).nor()
        position.mulAdd(direction, speed * dt)
      }
    }

  }

  // TODO : Placeholder to check drawing and movement logic, need to be change
  override def draw(g: GdxGraphics): Unit = {
    g.setColor(Color.GRAY)
    g.drawFilledRectangle(position.x, position.y + 100, 120, 15, 0)
    g.setColor(Color.RED)
    g.drawFilledRectangle(position.x, position.y + 100, hp, 8, 0)
    //draw hitbox corresponding to type
    name match {
      case "goblin" =>
        g.setColor(Color.GREEN) // HitBox
        g.drawFilledRectangle(position.x, position.y, width, height, 0)
      case "skeleton distance" =>
        g.setColor(new Color(200, 200, 200, 0)) // HitBox
        g.drawFilledRectangle(position.x, position.y, width, height, 0)
      case "skeleton" =>
        g.setColor(new Color(150, 150, 150, 0)) // HitBox
        g.drawFilledRectangle(position.x, position.y, width, height, 0)
      case "orc" =>
        g.setColor(Color.FOREST) // HitBox
        g.drawFilledRectangle(position.x, position.y, width, height, 0)
      case "mage" =>
        g.setColor(Color.BLUE) // HitBox
        g.drawFilledRectangle(position.x, position.y, width, height, 0)
      case "boss" =>
        g.setColor(Color.GOLDENROD) // HitBox
        g.drawFilledRectangle(position.x, position.y, width, height, 0)
      case _ =>
    }

    g.setColor(Color.WHITE)
    g.drawString(position.x, position.y, s"$nbr")
  }

  override def getPosition: Vector2 = position.cpy()

  def getHit(player: Player): Unit = {
    hp -= player.damage()
    knockbackDir = new Vector2(position).sub(player.getPosition).nor()
    knockbackTimer = knockbackDuration
  }

  def getHit(projectile: Projectile): Unit = {
    hp -= projectile.damage
    projectile.onHit()
  }

  def takeDamage(dmg : Int): Unit = {
    hp -= dmg
  }

  def pushAway(pushVec: Vector2): Unit = { // Avoid collision between ennemies
    position.add(pushVec)
  }

}
