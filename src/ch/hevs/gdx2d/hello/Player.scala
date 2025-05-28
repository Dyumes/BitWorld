package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.Vector2

class Player(x: Float, y: Float, val width: Float, val height: Float) {
  private val position = new Vector2(x, y)
  private val speed = 200f // pixels/seconde

  def update(dt: Float): Unit = {
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  position.x -= speed * dt
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) position.x += speed * dt
    if (Gdx.input.isKeyPressed(Input.Keys.UP))    position.y += speed * dt
    if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  position.y -= speed * dt
  }

  def draw(g: GdxGraphics): Unit = {
    g.setColor(Color.WHITE)
    g.drawFilledRectangle(position.x, position.y, width, height, 0)
  }

  def getPosition: Vector2 = position.cpy()

  def focusCamera(camera: OrthographicCamera, zoom: Float, dt: Float): Unit = {
    val targetX = position.x + width / 2
    val targetY = position.y + width / 2

    val lin_interpo = 5f //Linear interpolation for smoother movement from camera

    camera.position.x += (targetX - camera.position.x) * lin_interpo * dt
    camera.position.y += (targetY - camera.position.y) * lin_interpo * dt
    camera.zoom = zoom

    camera.update()
  }
}
