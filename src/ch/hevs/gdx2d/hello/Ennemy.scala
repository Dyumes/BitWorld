package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.{Interpolation, Vector2}
/**
 *
 * A Player class for controlling the player and the camera following all along
 *
 */

class Ennemy(x: Float, y: Float, width: Float, height: Float) extends Entity (x, y, width, height){
  private val position = new Vector2(x, y)
  private val speed = 200 // pixels/second

  private var hp : Int = 100
  private val dmg : Int = 10

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

  // Update the position of the player matching to input
  override def update(dt: Float, playerPos : Vector2): Unit = {
    if (hp <= 0){
      println("ENNEMY DEAD")
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
    g.setColor(Color.RED) // HitBox
    g.drawFilledRectangle(position.x, position.y, width, height, 0)
  }

  override def getPosition: Vector2 = position.cpy()

  def getHit(player: Player): Unit = {
    hp -= 10
    knockbackDir = new Vector2(position).sub(player.getPosition).nor()
    knockbackTimer = knockbackDuration
  }

  def pushAway(pushVec: Vector2): Unit = { // Avoid collision between ennemies
    position.add(pushVec)
  }

}
