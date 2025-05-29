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

class Player(x: Float, y: Float, width: Float, height: Float) extends Entity (x, y, width, height){
  private val position = new Vector2(x, y)
  private val speed = 400 // pixels/second

  private var hp : Int = 200

  private var knockbackDir = new Vector2(0, 0)
  private var knockbackTimer = 0f
  private val knockbackDuration = 0.2f // 200 ms
  private val knockbackDistance = 600f

  // Update the position of the player matching to input
  override def update(dt: Float, playerPos: Vector2 = null): Unit = {
    if (hp <= 0){
      println("GAME OVER")

    } else {
      if (knockbackTimer > 0) {
        knockbackTimer -= dt
        val t = 1f - (knockbackTimer / knockbackDuration)
        val strength = Interpolation.swingOut.apply(1f - t) * knockbackDistance

        position.add(knockbackDir.cpy().scl(strength * dt))
      } else {
        val diagonalFactor = 0.8f // Useful to avoid faster movement on diagonal

        val left  = Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val right = Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        val up    = Gdx.input.isKeyPressed(Input.Keys.UP)
        val down  = Gdx.input.isKeyPressed(Input.Keys.DOWN)

        val horizontal = (if (left) -1 else 0) + (if (right) 1 else 0)
        val vertical   = (if (up) 1 else 0) + (if (down) -1 else 0)

        val isDiagonal = horizontal != 0 && vertical != 0
        val moveFactor = if (isDiagonal) diagonalFactor else 1f

        position.x += horizontal * speed * dt * moveFactor
        position.y += vertical   * speed * dt * moveFactor
      }

    }


  }

  // TODO : Placeholder to check drawing and movement logic, need to be change
  override def draw(g: GdxGraphics): Unit = {
    g.setColor(Color.GRAY)
    g.drawFilledRectangle(position.x, position.y + 100, 120, 15, 0)
    g.setColor(Color.GREEN)
    g.drawFilledRectangle(position.x, position.y + 100, hp / 2, 8, 0)
    g.setColor(Color.WHITE) // Hitbox
    g.drawFilledRectangle(position.x, position.y, width, height, 0)
  }

  override def getPosition: Vector2 = position.cpy()


  // All the logic for the following camera with smoother transition from A to B
  def focusCamera(camera: OrthographicCamera, zoom: Float, dt: Float): Unit = {
    // Variable declarations for centering the camera
    val targetX = position.x
    val targetY = position.y

    val lin_interpo = 5f //Linear interpolation for smoother movement from camera

    // Change the camera position along player's position + zoom
    camera.position.x += (targetX - camera.position.x) * lin_interpo * dt
    camera.position.y += (targetY - camera.position.y) * lin_interpo * dt
    camera.zoom = zoom

    camera.update()
  }

  def getHit(ennemy: Ennemy): Unit = {
    hp -= ennemy.damage()
    println(s"PLAYER GOT HIT : $hp HP left")

    // Direction inverse de l'ennemi vers le joueur
    knockbackDir = new Vector2(position).sub(ennemy.getPosition).nor()
    knockbackTimer = knockbackDuration
  }

}
