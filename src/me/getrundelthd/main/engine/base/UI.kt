package me.getrundelthd.main.engine.base

import me.getrundelthd.main.*
import me.getrundelthd.main.engine.GAMESTATE
import me.getrundelthd.main.engine.Player
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.KeyEvent.*

interface UI : Drawable

const val consoleHeight = TILE_SIZE
const val CURSOR_COOLDOWN = 50.0

class Console : UI {

    var cursorTimer = 0.0

    val consoleHandler = object : KeyListener {
        override fun keyTyped(e: KeyEvent?) {

            if (e == null) return

            val char = e.keyChar

            if (char.isDigit() || char.isLetter() || char == ' ') {
                console.buffer += char
            }

        }

        override fun keyPressed(e: KeyEvent?) {

            if(e == null) return

            when (e.keyCode) {

                VK_BACK_SPACE ->
                    if(buffer.isNotEmpty())
                        console.buffer = console.buffer.substring(0, console.buffer.length - 1)
                VK_ESCAPE ->
                    console.buffer = ""
                VK_CONTROL -> {
                    mainGameState = GAMESTATE.MAIN
                    drawHandler.UI_Elements.remove(console)
                    player.consoleTimer = 0.0
                    drawHandler.removeKeyListener(this)
                }
                VK_ENTER -> {
                    when (console.checkCommand()) {
                        0 -> {
                            console.buffer = ""
                            console.errorOnCommand = false
                            console.commandNotFound = false
                        }
                        -1 -> console.commandNotFound = true
                        -2 -> console.errorOnCommand = true
                    }
                }
            }
        }

        override fun keyReleased(e: KeyEvent?) {
        }
    }


    // holds the characters that are currently in the console
    var buffer = ""

    var errorOnCommand = false
    var commandNotFound = false

    // stores all commands
    private val commands = listOf(

            // sets the position of the player on the screen
            Command("setpos", 2) {

                val posX = it[0].toDouble()
                val posY = it[1].toDouble()

                player.x = posX
                player.y = posY
            }
    )

    /**
     * Takes the current character buffer and checks
     * if the command is valid
     * -> if it is -> execute the command with its args
     *      -> if the execution fails -> return -2
     *      -> if the execution passes -> return 0
     * -> else -> return -1
     */
    fun checkCommand(): Int {
        val tokens = buffer.split(" ")

        var found = false
        var error = false
        // loop through every command and search for the given name
        commands.forEach loop@{

            found = true

            if (it.name == tokens[0] && it.argc == tokens.size - 1) {
                try {
                    it.action(tokens.subList(1, tokens.size))
                } catch (e: Exception) {
                    e.printStackTrace()
                    error = true
                }

                return@loop
            }
        }

        return when {
            found && !error -> 0
            !found -> -1
            found && error -> -2
            else -> 0
        }

    }

    /**
     * Draws the console to the screen
     */
    override fun draw(g: Graphics) {
        g.color = Color(0, 0, 0, 100)
        g.fillRect(0, 0, drawHandler.width, drawHandler.height)

        g.color = Color.BLACK
        g.fillRect(0, drawHandler.height - consoleHeight, drawHandler.width, drawHandler.height)

        g.color = Color.WHITE
        g.font = java.awt.Font("Courier New", Font.PLAIN, (consoleHeight * 0.5).toInt())
        g.drawString(buffer, 5, (drawHandler.height - consoleHeight * .3).toInt())

        if(cursorTimer * 3 > CURSOR_COOLDOWN) {
            // draw Cursor
            g.fillRect(g.fontMetrics.stringWidth(buffer) + 10, (drawHandler.height - consoleHeight * 0.7).toInt(),
                    3, (consoleHeight * .5).toInt())
        }

        g.color = Color.RED
        g.font = g.font.deriveFont(Font.BOLD)
        val posX = 5
        val posY = drawHandler.height - consoleHeight * 1.5
        if (errorOnCommand)
            g.drawString("Error while executing command!", posX, posY.toInt())
        else if (commandNotFound)
            g.drawString("Command was not found!", posX, posY.toInt())

    }


    fun update(delta: Double) {
        cursorTimer += delta

        if(cursorTimer > CURSOR_COOLDOWN)
            cursorTimer = 0.0
    }

}

private class Command(val name: String, val argc: Int, val action: ((List<String>) -> Unit))