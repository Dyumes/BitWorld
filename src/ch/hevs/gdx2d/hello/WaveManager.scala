package ch.hevs.gdx2d.hello

import scala.collection.mutable.ArrayBuffer

class WaveManager {
  private var waveNumber = 0

  def update(enemies : ArrayBuffer[Enemy])  = {
    if(enemies.isEmpty) {
      waveNumber += 1
    }
  }

  def generateWave(): Int = {
    val count : Int = 5 * waveNumber
    count
  }
}
