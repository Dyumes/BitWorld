package ch.hevs.gdx2d

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}

trait GameMenu{
  def update(): Unit
  def draw(): Unit
  def isFinished : Boolean
}

class MenuManager {

  private var currentMenu: Option[GameMenu] = None

  def setMenu(menu: GameMenu): Unit = {
    currentMenu = Some(menu)
  }

  def clearMenu(): Unit = {
    currentMenu = None
  }

  def update(): Unit = {
    currentMenu.foreach(_.update())
    if (currentMenu.exists(_.isFinished)) clearMenu()
  }

  def draw(): Unit = {
    currentMenu.foreach(_.draw())
  }

  def isMenuActive: Boolean = currentMenu.isDefined
}

class MainMenu(
                uiFont: BitmapFont,
                uiBatch: SpriteBatch,
                uiCamera: OrthographicCamera
              ) extends GameMenu {
  private var finished = false

  override def update(): Unit = {
    if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
      finished = true
    }
  }

  override def draw(): Unit = {
    uiCamera.update()
    uiBatch.setProjectionMatrix(uiCamera.combined)

    uiBatch.begin()
    uiFont.getData.setScale(3f)
    uiFont.draw(uiBatch, "Appuyez sur ENTER pour jouer", 600, 540)
    uiBatch.end()
  }

  override def isFinished: Boolean = finished
}

class VictoryMenu(
                   uiFont: BitmapFont,
                   uiBatch: SpriteBatch,
                   uiCamera: OrthographicCamera
                 ) extends GameMenu {
  private var finished = false

  override def update(): Unit = {
    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
      finished = true
    }
  }

  override def draw(): Unit = {
    uiCamera.update()
    uiBatch.setProjectionMatrix(uiCamera.combined)

    uiBatch.begin()
    uiFont.getData.setScale(3f)
    uiFont.draw(uiBatch, "VICTORY! Appuyez sur ESC pour quitter", 400, 540)
    uiBatch.end()
  }

  override def isFinished: Boolean = finished
}




