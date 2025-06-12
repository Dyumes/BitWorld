package ch.hevs.gdx2d

import ch.hevs.gdx2d.Entity.Player
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}

// Trait defining a generic game menu interface.
// Every menu must implement update logic, rendering, and termination condition.
trait GameMenu {
  def update(): Unit          // Called each frame to process input or logic
  def draw(): Unit            // Called each frame to render the menu
  def isFinished: Boolean     // Indicates if the menu has completed its function
}

// MenuManager is responsible for handling the currently active menu.
// It abstracts away the lifecycle and transitions between menus.
class MenuManager {
  private var currentMenu: Option[GameMenu] = None

  // Sets a new active menu
  def setMenu(menu: GameMenu): Unit = {
    currentMenu = Some(menu)
  }

  // Clears the current menu (e.g., when it's finished)
  def clearMenu(): Unit = {
    currentMenu = None
  }

  // Delegates update logic to the active menu, if any
  def update(): Unit = {
    currentMenu.foreach(_.update())
    // Automatically clears the menu when it is done
    if (currentMenu.exists(_.isFinished)) clearMenu()
  }

  // Delegates drawing to the active menu, if any
  def draw(): Unit = {
    currentMenu.foreach(_.draw())
  }

  // Indicates whether a menu is currently active
  def isMenuActive: Boolean = currentMenu.isDefined
}

// Implementation of the main menu.
// Displays a start prompt and finishes when ENTER is pressed.
class MainMenu(
                uiFont: BitmapFont,
                uiBatch: SpriteBatch,
                uiCamera: OrthographicCamera
              ) extends GameMenu {
  private var finished = false

  override def update(): Unit = {
    // Transition out of menu on ENTER key
    if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
      finished = true
    }
  }

  override def draw(): Unit = {
    uiCamera.update()
    uiBatch.setProjectionMatrix(uiCamera.combined)

    uiBatch.begin()
    uiFont.getData.setScale(3f)
    uiFont.draw(uiBatch, "Press ENTER to play", 600, 540)
    uiBatch.end()
  }

  override def isFinished: Boolean = finished
}

// Implementation of the victory (or game over) screen.
// Finishes on ESCAPE key and displays a message based on player's state.
class VictoryMenu(
                   uiFont: BitmapFont,
                   uiBatch: SpriteBatch,
                   uiCamera: OrthographicCamera,
                   g: GdxGraphics,
                   player: Player
                 ) extends GameMenu {
  private var finished = false

  override def update(): Unit = {
    // Exit the menu on ESCAPE key
    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
      finished = true
    }
  }

  override def draw(): Unit = {
    g.clear()
    uiCamera.update()
    uiBatch.setProjectionMatrix(uiCamera.combined)

    uiBatch.begin()
    uiFont.getData.setScale(3f)

    // Message depends on whether the player is still alive
    if (player.isAlive) {
      uiFont.draw(uiBatch, "VICTORY! Press ESC to quit", 400, 540)
    } else {
      uiFont.draw(uiBatch, "GAME OVER", 400, 540)
    }

    uiBatch.end()
  }

  override def isFinished: Boolean = finished
}
