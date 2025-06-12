/**
 * Abilities System
 * ---------------
 * This file defines the Ability system for the game. Each ability is a class that can be attached to a player,
 * providing passive or active effects. Abilities can modify player stats, trigger special effects, or interact
 * with enemies. Each ability has a name, a description, an activation state, and an update method called every frame.
 *
 * Key Features:
 * - Abstract Ability base class with activation logic.
 * - Several concrete abilities: health boost, damage boost, piercing boost, speed boost, attack speed boost, range boost.
 * - Special abilities: Zone (damage area), Vampirism, Random Lightning, Crazy Axe.
 * - Each ability implements its own update logic.
 */

package ch.hevs.gdx2d.weapons_abilities

import ch.hevs.gdx2d.Entity.{Enemy, Player}
import scala.collection.mutable.ArrayBuffer

// --- Base Ability Class ---

abstract class Ability(val name: String, var unlocked: Boolean) {
  // actionType: true -> Active, false -> Passive
  val description: String = ""
  def update(dt: Float): Unit = {}
  var activated = unlocked
  def setActivated(): Unit = {
    activated = true
  }
}

// --- Stat Boost Abilities ---

class healthBoost(player: Player) extends Ability("Boost of heal", false) {
  override val description: String = "Increase health point"
  override def update(dt: Float): Unit = {
    if (activated) {
      player.setHealth((player.healthPoint * 0.1).toInt)
      activated = false
    }
  }
}

class damageBoost(player: Player) extends Ability("Boost of damage", false) {
  override val description: String = "Increase physical damage"
  override def update(dt: Float): Unit = {
    if (activated) {
      player.getWeapon.dmg += 40
      activated = false
    }
  }
}

class piercingBoost(player: Player) extends Ability("Boost of piercing", false) {
  override val description: String = "Increase piercing power of every weapon"
  override def update(dt: Float): Unit = {
    if (activated) {
      player.getWeapon.piercePower += 1
      activated = false
    }
  }
}

class speedBoost(player: Player) extends Ability("Boost of movement", false) {
  override val description: String = "Increase movement speed"
  override def update(dt: Float): Unit = {
    if (activated) {
      player.setSpeed(80)
      activated = false
    }
  }
}

class attackSpeedBoost(player: Player) extends Ability("Boost of attack speed", false) {
  override val description: String = "Increase attack speed of every weapon"
  override def update(dt: Float): Unit = {
    if (activated) {
      player.setAttackSpeed(0.05f)
      activated = false
    }
  }
}

class rangeBoost(player: Player) extends Ability("Boost of range", false) {
  override val description: String = "Increase player range"
  override def update(dt: Float): Unit = {
    if (activated) {
      // Implement range increase logic here
    }
  }
}

// --- Special/Active Abilities ---

class Zone(player: Player, enemies: ArrayBuffer[Enemy]) extends Ability("Damage zone", false) {
  override val description: String = "Create a damage zone all around the player, dealing constant damage"
  val damage: Int = 1
  val radius = 200f
  var canBeDrawn = false
  private val counterMax = 0.1f
  private var counter = 0f

  override def update(dt: Float): Unit = {
    counter += dt
    canBeDrawn = activated
    if (canBeDrawn && counter >= counterMax) {
      enemies.foreach { enemy =>
        if (enemy.getPosition.dst(player.getPosition) < radius) {
          enemy.getHit(this)
        }
      }
      counter = 0
    }
  }
}

class vampirisim(player: Player) extends Ability("Vampirisme", false) {
  override val description: String = "Donne un chance de vampirisme à chaque coup"
  override def update(dt: Float): Unit = {
    if (activated) {
      // Implement vampirism logic here
    }
  }
}

class randomLightning(player: Player) extends Ability("Éclair magique", false) {
  override val description: String = "Donne une chance de frapper un ennemi aléatoire avec un éclair"
  override def update(dt: Float): Unit = {
    if (activated) {
      // Implement random lightning logic here
    }
  }
}

class crazyAxe(player: Player) extends Ability("Hache boomerang", false) {
  override val description: String = "Lance une hache boomerang qui découpera vos ennemis"
  override def update(dt: Float): Unit = {
    if (activated) {
      // Implement crazy axe logic here
    }
  }
}