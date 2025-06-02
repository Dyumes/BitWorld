package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable.ArrayBuffer

abstract class Entity (var x: Float, var y: Float, val width: Float, val height : Float, val nbr: Int) {
  def update(dt: Float, playerPos: Vector2 = null, enemies : ArrayBuffer[Enemy] = null): Unit

  def draw(g: GdxGraphics): Unit = {
    // Default, draw a rectangle
    g.setColor(Color.WHITE)
    g.drawFilledRectangle(x, y, width, height, 0)
  }

  def getPosition : Vector2 = new Vector2(x, y)
}
