package ch.hevs.gdx2d.weapons_abilities

import ch.hevs.gdx2d.Entity.Player
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color

import scala.util.Random

// Represents the in-game ability selection menu.
// Displays three randomly chosen abilities for the player to choose from.
class AbilityMenu(player: Player, val allAbilities: List[Ability]) {

  // The three abilities currently visible to the player, randomly selected.
  var visibleAbilities: List[Ability] = Random.shuffle(allAbilities).take(3)

  // Index of the currently highlighted menu item (always 0 in this version).
  private val selectedIndex: Int = 0

  // Refreshes the visible ability list with three unactivated random abilities.
  // If there are not enough unactivated abilities, fallback defaults are used.
  def refreshAbilities(): Unit = {
    require(allAbilities.size >= 3, "Must have at least 3 abilities in allAbilities")

    // Filter out already activated abilities and pick 3 randomly
    visibleAbilities = Random.shuffle(allAbilities.filterNot(_.activated)).take(3)

    // If less than 3 are available, assign a default set of basic abilities
    if (visibleAbilities.size < 3) {
      visibleAbilities = List(
        new damageBoost(player),
        new healthBoost(player),
        new speedBoost(player)
      )
    }
  }

  // Draws the weapon choice menu (e.g., Bow, Spear, Orb) above the player.
  def drawWeaponChoices(g: GdxGraphics): Unit = {
    val playerPos = player.getPosition
    val menuX = playerPos.x - 100
    val menuY = playerPos.y + 150

    g.setColor(Color.WHITE)
    g.drawString(menuX, menuY + 50, "Choose your weapon:")

    val weapons = List("Bow", "Spear", "Orb")

    for ((name, i) <- weapons.zipWithIndex) {
      val optionY = menuY - i * 30
      val color = if (i == selectedIndex) Color.YELLOW else Color.LIGHT_GRAY

      g.setColor(color)
      g.drawString(menuX + 20, optionY, s"${i + 1}. $name")
    }

    g.setColor(Color.WHITE) // Reset drawing color to default
  }

  // Draws the ability selection menu based on currently visible abilities.
  def drawAbilityChoices(g: GdxGraphics): Unit = {
    val playerPos = player.getPosition
    val menuX = playerPos.x - 100
    val menuY = playerPos.y + 150

    g.setColor(Color.WHITE)
    g.drawString(menuX, menuY + 50, "Choose an ability:")

    for ((ability, i) <- visibleAbilities.zipWithIndex) {
      val optionY = menuY - i * 30
      val color = if (i == selectedIndex) Color.YELLOW else Color.LIGHT_GRAY

      g.setColor(color)
      g.drawString(menuX + 20, optionY, s"${i + 1}. ${ability.name} - ${ability.description}")
    }

    g.setColor(Color.WHITE) // Ensure color state is clean after rendering
  }
}
