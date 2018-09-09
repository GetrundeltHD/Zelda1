package me.getrundelthd.main.engine.map

import me.getrundelthd.main.TILE_SIZE
import me.getrundelthd.main.drawHandler
import me.getrundelthd.main.drawing.TILES_PER_ROW
import me.getrundelthd.main.drawing.WINDOW_HEIGHT
import me.getrundelthd.main.drawing.WINDOW_WIDTH
import me.getrundelthd.main.engine.*
import me.getrundelthd.main.engine.base.GameObject
import me.getrundelthd.main.engine.interacables.Portal
import me.getrundelthd.main.engine.interacables.currentEnteringTime
import me.getrundelthd.main.io.OverworldSpriteLoader
import me.getrundelthd.main.io.loadScreen
import me.getrundelthd.main.mainGameState
import java.nio.file.Paths
import kotlin.math.abs

class Screen {

    val tiles: Array<Tile>
    val enemies: Array<Entity>?
    val pointers : Array<String>
    val portals : Array<Portal>?


    constructor(tiles: Array<Tile>, enemies: Array<Entity>?, pointers: Array<String>, portals : Array<Portal>?) {
        this.tiles = tiles
        this.enemies = enemies
        this.pointers = pointers
        this.portals = portals
    }

    constructor() {
        tiles = Array(TILES_PER_ROW * TILES_PER_ROW) {
            val x = it % TILES_PER_ROW
            val y = (it / TILES_PER_ROW)
            Tile(x, y, OverworldSpriteLoader.sprites[2])
        }

        enemies = null
        portals = null
        pointers = arrayOf("@None", "@None", "@None", "@None")
    }
}

class ScreenManager(var current: Screen, val player: Player) {

    var next: Screen? = null
    var offSetX = 0.0
    var offSetY = 0.0

    var dir: DIR? = null

    val transVel = 20.0

    fun update(delta: Double) {

        when (mainGameState) {

            GAMESTATE.MAIN -> {

                current.enemies?.forEach {it.update(delta)}

                current.portals?.forEach {
                    if(player.hitbox.intersects(it.hitbox))
                        it.teleport(current.tiles)
                }

                when {
                    player.right >= WINDOW_WIDTH.toDouble() -> transition(DIR.EAST)
                    player.left <= 0.0 -> transition(DIR.WEST)
                    player.bottom >= WINDOW_HEIGHT.toDouble() -> transition(DIR.SOUTH)
                    player.top <= 0.0 -> transition(DIR.NORTH)
                }
            }

            GAMESTATE.SCREEN_TRANSITION -> {
                // if the screen has the right position -> go into normal game mode
                if ((abs(offSetX) > WINDOW_WIDTH && (dir == DIR.EAST || dir == DIR.WEST))
                        || (abs(offSetY) > WINDOW_HEIGHT && (dir == DIR.NORTH || dir == DIR.SOUTH))) {

                    // remove the old screen
                    destroyScreen()
                    // set the new screen
                    current = next!!

                    // make sure the positions if the new screen are all right
                    for (i in current.tiles.indices) {
                        current.tiles[i].x = ((i % TILES_PER_ROW) * TILE_SIZE).toDouble()
                        current.tiles[i].y = ((i / TILES_PER_ROW) * TILE_SIZE).toDouble()
                    }

                    // set games state to normal
                    mainGameState = GAMESTATE.MAIN

                    // reset the offset
                    offSetX = 0.0
                    offSetY = 0.0

                    // set the player location so that the transition don't triggers immediately again
                    when(dir) {
                        DIR.EAST -> player.x = 1.0
                        DIR.WEST -> player.x = WINDOW_WIDTH - player.hitbox.width - 1.0
                        DIR.NORTH -> player.y = WINDOW_HEIGHT - player.hitbox.height - 1.0
                        DIR.SOUTH -> player.y = 1.0
                    }

                    // stop the method
                    return
                }

                // update the player and tile cords
                when (dir!!) {

                    DIR.NORTH -> {
                        player.y += delta * transVel
                        current.tiles.forEach {
                            it.y += delta * transVel
                        }
                        next!!.tiles.forEach {
                            it.y += delta * transVel
                        }
                    }

                    DIR.EAST -> {
                        player.x -= delta * transVel
                        current.tiles.forEach {
                            it.x -= delta * transVel
                        }
                        next!!.tiles.forEach {
                            it.x -= delta * transVel
                        }
                    }

                    DIR.SOUTH -> {
                        player.y -= delta * transVel
                        current.tiles.forEach {
                            it.y -= delta * transVel
                        }
                        next!!.tiles.forEach {
                            it.y -= delta * transVel
                        }
                    }

                    DIR.WEST -> {
                        player.x += delta * transVel
                        current.tiles.forEach {
                            it.x += delta * transVel
                        }
                        next!!.tiles.forEach {
                            it.x += delta * transVel
                        }
                    }

                }

                // update the offset
                offSetX += delta * transVel
                offSetY += delta * transVel
            }
        }

    }

    fun initNewScreen() {
        drawHandler.objects.addAll(current.tiles)
        if(current.enemies != null)
            drawHandler.objects.addAll(current.enemies as Array<out Entity>)

    }

    fun destroyScreen() {
        drawHandler.objects.removeAll(current.tiles)
        if(current.enemies != null)
            drawHandler.objects.removeAll(current.enemies as Array<out Entity>)
    }

    private fun transition(dir: DIR) {

        // change into transition state
        mainGameState = GAMESTATE.SCREEN_TRANSITION

        // save the direction of the transition for later
        this.dir = dir

        // get the right screen which will be the next
        val index = when (dir) {
            DIR.NORTH -> 0
            DIR.EAST -> 1
            DIR.SOUTH -> 2
            DIR.WEST -> 3
        }

        // load the next screen
        next = loadScreen(Paths.get("res", "maps", "overworld","${current.pointers[index]}.txt"))

        // offset the tile cords of the next screen
        when (dir) {
            DIR.NORTH -> for (i in next!!.tiles.indices) {
                next!!.tiles[i].y = ((i / TILES_PER_ROW) * TILE_SIZE).toDouble() - WINDOW_HEIGHT
            }
            DIR.EAST -> for (i in next!!.tiles.indices) {
                next!!.tiles[i].x = ((i % TILES_PER_ROW) * TILE_SIZE).toDouble() + WINDOW_WIDTH
            }
            DIR.SOUTH -> for (i in next!!.tiles.indices) {
                next!!.tiles[i].y = ((i / TILES_PER_ROW) * TILE_SIZE).toDouble() + WINDOW_HEIGHT
            }
            DIR.WEST -> for (i in next!!.tiles.indices) {
                next!!.tiles[i].x = ((i % TILES_PER_ROW) * TILE_SIZE).toDouble() - WINDOW_WIDTH
            }
        }

        // add all the tiles of the next screen to the drawhandler that they are drawn
        drawHandler.objects.addAll(next!!.tiles)
    }

}