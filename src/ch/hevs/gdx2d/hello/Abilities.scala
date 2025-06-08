package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.{Gdx, Input}


import scala.collection.mutable.ArrayBuffer

abstract class Ability(val name: String, val actionType : Boolean, var unlocked : Boolean) {
  //actionType : true -> Active, false -> Passive
  val description : String = ""
  def update(): Unit = {}
  var activated = unlocked
  def setActivated() : Unit = {
    activated = true
  }
}


class healthBoost(player: Player) extends Ability("Boost de vie", false, false){
  override val description: String = "Augmente le nombre de points de vie"
  //TODO : Modifier comment marche les fonctions qui ne sont censées être activées qu'une seule fois
  override def update(): Unit = {
    if(activated){
      player.setHealth(50)
      activated = false
    }
  }
  //val test : healthBoost = new healthBoost(player)
}

class damageBoost(player: Player) extends Ability("Boost de dégâts", false, false){
  override val description: String = "Boost de dégâts"
  override def update(): Unit = {
    if(activated){
      player.getWeapon.dmg += 20
      activated = false
    }
  }
  //val test : damageBoost = new damageBoost(player)
}

class piercingBoost(player: Player) extends Ability("Boost de transpercement", false, false){
  override val description: String = "Boost de transpercement"
  override def update(): Unit = {
    if(activated){
      player.getWeapon.piercePower += 1
      activated = false
    }
  }
  // val test : piercingBoost = new piercingBoost(player)
}

class speedBoost(player: Player) extends Ability("Boost de vitesse de déplacement", false, false){
  override val description: String = "Boost de vitesse de déplacement"
  override def update(): Unit = {
    if(activated){
      player.setSpeed(25)
      activated = false
    }
  }
  //val test : speedBoost = new speedBoost(player)
}

class attackSpeedBoost(player: Player) extends Ability("Boost de vitesse d'attaque", false, false){
  override val description: String = "Boost de vitesse d'attaque"
  override def update(): Unit = {
    if(activated){
      player.setAttackSpeed(0.05f)
      activated = false
    }
  }
  //val test : attackSpeedBoost = new attackSpeedBoost(player) //ps c'est une bonne idée pour faire un rayon quand on met la valeur a fond
}

class rangeBoost(player: Player) extends Ability("Boost de portée", false, false){
  override val description: String = "Boost de portée"
  override def update(): Unit = {
    if(activated){

    }
  }
}

class stinky(player: Player, enemies : ArrayBuffer[Enemy]) extends Ability("Stinky fart", false, false) {
  override val description: String = "Zone de dégâts autour du joueur"
  val damage: Int = 1
  val radius = 300f
  var canBeDrawn = false

  override def update(): Unit = {
    canBeDrawn = activated
    if (canBeDrawn) {
      println("RAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
      enemies.foreach { enemy =>
        if (enemy.getPosition.dst(player.getPosition) < radius) {
          enemy.getHit(this)
        }
      }
    }
  }
}

class vampirisim(player: Player) extends Ability("Vampirisme", false, false){
  override val description: String = "Donne un chance de vampirisme à chaque coup"
  override def update(): Unit = {
    if(activated){

    }
  }
}

class randomLightning(player: Player) extends Ability("Éclair magique", false, false){
  override val description: String = "Donne une chance de frapper un ennemi aléatoire avec un éclair"
  override def update(): Unit = {
    if(activated){

    }
  }
}

class crazyAxe(player: Player) extends Ability("Hache boomerang", false, false){
  override val description: String = "Lance une hache boomerang qui découpera vos ennemis"
  override def update(): Unit = {
    if(activated){

    }
  }
}