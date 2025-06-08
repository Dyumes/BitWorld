package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.hello.{Ability, Player, damageBoost}
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.interfaces.DrawableObject
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color

import scala.util.Random

class AbilityMenu(player: Player, val allAbilities: List[Ability]) {
  var visibleAbilities: List[Ability] = Random.shuffle(allAbilities).take(3)
  private var selectedIndex: Int = 0

  def refreshAbilities(): Unit = {
    require(allAbilities.size >= 3, "Must have at least 3 abilities in allAbilities")

    visibleAbilities = Random.shuffle(allAbilities.filterNot(_.activated)).take(3)

    if (visibleAbilities.size < 3) {
      visibleAbilities = List(new damageBoost(player), new healthBoost(player), new speedBoost(player))
    }
  }


  def draw(g: GdxGraphics): Unit = {
    val playerPos = player.getPosition
    val menuX = playerPos.x - 100
    val menuY = playerPos.y + 150
    g.setColor(Color.WHITE)
    g.drawString(menuX, menuY + 50, "Choose an ability:")

    for ((ability, i) <- visibleAbilities.zipWithIndex) {
      val optionY = menuY - i * 30  // Space out options vertically
      val color = if (i == selectedIndex) Color.YELLOW else Color.LIGHT_GRAY
      g.setColor(color)
      g.drawString(menuX + 20, optionY, s"${i + 1}. ${ability.name} - ${ability.description}")
    }

    g.setColor(Color.WHITE) // Reset color
  }
}