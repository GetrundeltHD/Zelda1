package me.getrundelthd.main.engine.interacables

import me.getrundelthd.main.*
import me.getrundelthd.main.drawing.TILES_PER_ROW
import me.getrundelthd.main.engine.GAMESTATE
import me.getrundelthd.main.engine.Tile
import me.getrundelthd.main.engine.base.GameObject
import me.getrundelthd.main.io.loadScreen
import java.awt.Color
import java.awt.Graphics
import java.nio.file.Path
import kotlin.math.roundToInt

const val CAVE_ENTERING_TIME = 75.0
var currentEnteringTime = 0.0

class Portal(x : Double, y : Double, val enteringCave : Boolean, val newX : Double,val newY : Double, val nextScreen : Path) :
    GameObject(x, y, TILE_SIZE, TILE_SIZE, false, false, false, null, 0) {

    var entered = false
    val destScreen
        get() = loadScreen(nextScreen)

    fun teleport(tiles : Array<Tile>) {

        // if dungeon or cave is entered -> play anim else load screen
        if(enteringCave)
            mainGameState = GAMESTATE.CAVE_ENTERING
        else {
            scrManager.destroyScreen()
            scrManager.current = destScreen
            scrManager.initNewScreen()
            resetLocation()
            return
        }

        // set up player anim
        val posX = x.roundToInt() / TILE_SIZE
        val posY = y.roundToInt() / TILE_SIZE
        val t = tiles[posX + (posY + 1) * TILES_PER_ROW]

        t.drawOrder = -1
        entered = true
        currentEnteringTime = 0.0

        // make player centered in portal
        player.x = x + TILE_SIZE * .1
        player.y = y + TILE_SIZE * .15
    }

    fun resetLocation() {
        player.x = newX
        player.y = newY
    }


    override fun toString(): String {
        return "<Portal x: $x  y: $y  entered: $entered  next: $nextScreen>"
    }
}