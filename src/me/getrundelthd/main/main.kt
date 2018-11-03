package me.getrundelthd.main

import me.getrundelthd.main.drawing.DrawHandler
import me.getrundelthd.main.drawing.TILES_PER_COL
import me.getrundelthd.main.drawing.TILES_PER_ROW
import me.getrundelthd.main.engine.GAMESTATE
import me.getrundelthd.main.engine.Player
import me.getrundelthd.main.engine.interacables.CAVE_ENTERING_TIME
import me.getrundelthd.main.engine.interacables.NPC
import me.getrundelthd.main.engine.interacables.currentEnteringTime
import me.getrundelthd.main.engine.map.ScreenManager
import me.getrundelthd.main.io.FontLoader
import me.getrundelthd.main.io.OverworldSpriteLoader
import me.getrundelthd.main.io.PlayerSpritesLoader
import me.getrundelthd.main.io.loadScreen
import java.nio.file.Paths

const val TILE_SIZE = 96

// TODO: missing sprite white tree, replace stone on grave yard!!!

val drawHandler = DrawHandler()
val player = Player((TILES_PER_ROW / 2 * TILE_SIZE).toDouble(),
        (TILES_PER_COL / 2 * TILE_SIZE).toDouble(), PlayerSpritesLoader.sprites)

val s = loadScreen(Paths.get("res", "maps", "overworld", "H8.txt"))
val scrManager = ScreenManager(s, player)

var currFps = 0
// TODO: set game state to start when played first time on save state
var mainGameState = GAMESTATE.MAIN

fun main(args : Array<String>) {

    scrManager.initNewScreen()
    drawHandler.objects.add(player)

    var lastLoopTime = System.nanoTime()
    val TARGET_FPS = 60
    val OPTIMAL_TIME = 1000000000 / TARGET_FPS

    var fps = 0
    var lastFpsTime = 0L

    while(true) {

        val now = System.nanoTime()
        val updateLength = now - lastLoopTime
        lastLoopTime = now
        val delta = updateLength / OPTIMAL_TIME.toDouble()

        lastFpsTime += updateLength
        fps++

        if(lastFpsTime >= 1000000000) {
            lastFpsTime = 0
            currFps = fps
            fps = 0
        }

        draw()
        update(delta)

        val timeOut = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000

        drawHandler.window.title = "The Legend of Zelda - FPS: $currFps"

        Thread.sleep(when {
            timeOut > 0 -> timeOut
            else -> 1
        })
    }
}

fun draw() {
    drawHandler.render()
}

// for start up black rect effect
var blackPerc = 100.0
var caveAnimCount = 0.0

fun update(delta : Double) {

    when(mainGameState) {

        GAMESTATE.MENU -> {

        }
        GAMESTATE.START -> {
            blackPerc -= delta
            if(blackPerc <= 0.0)
                mainGameState = GAMESTATE.MAIN
        }
        GAMESTATE.SCREEN_TRANSITION -> {
            player.update(delta)
            scrManager.update(delta)
        }
        GAMESTATE.CAVE_ENTERING -> {

            val activePortal = scrManager.current.portals.filter { it.entered }[0]
            if(currentEnteringTime >= CAVE_ENTERING_TIME) {
                // set new screen to cave
                scrManager.destroyScreen()
                scrManager.current = activePortal.destScreen
                scrManager.initNewScreen()
                activePortal.resetLocation()
                mainGameState = GAMESTATE.MAIN
                return
            }

            currentEnteringTime += delta
            caveAnimCount += delta

            // do player anim
            player.y = activePortal.y + TILE_SIZE * .15 + (TILE_SIZE * (currentEnteringTime / CAVE_ENTERING_TIME) - .15)
            if(caveAnimCount >= 8.0) {
                player.animator.state = if (player.animator.state == 5) 4 else 5
                player.sprite = player.animator.sprites[player.animator.state]
                caveAnimCount = 0.0
            }
        }
        GAMESTATE.MAIN -> {
            player._tiles = scrManager.current.allTiles
            player.update(delta)
            scrManager.update(delta)
        }

    }

    // println(currentEnteringTime)


}