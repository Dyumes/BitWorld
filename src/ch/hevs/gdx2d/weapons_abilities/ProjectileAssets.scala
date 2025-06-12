package ch.hevs.gdx2d.weapons_abilities
import ch.hevs.gdx2d.components.bitmaps.BitmapImage

// Assets image of all projectiles and weapons
object ProjectileAssets {
  val scalaImage = new BitmapImage("data/images/boss/scala.png")
  val scalaWidth = scalaImage.getImage.getWidth
  val scalaHeight = scalaImage.getImage.getHeight

  val bowImage = new BitmapImage("data/images/weapons/bow.png")
  val arrowImage = new BitmapImage("data/images/weapons/projectiles/placeholder_arrow.png")
  val arrowWidth = arrowImage.getImage.getWidth
  val arrowHeight = arrowImage.getImage.getHeight

  val spearImage = new BitmapImage("data/images/weapons/projectiles/spear.png")
  val spearWidth = spearImage.getImage.getWidth
  val spearHeight = spearImage.getImage.getHeight

  val orbImage = new BitmapImage("data/images/weapons/orb.png")
}
