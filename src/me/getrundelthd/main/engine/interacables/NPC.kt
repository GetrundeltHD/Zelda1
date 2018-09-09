package me.getrundelthd.main.engine.interacables

import me.getrundelthd.main.TILE_SIZE
import me.getrundelthd.main.drawing.WINDOW_WIDTH
import me.getrundelthd.main.engine.ENTITY_SIZE
import me.getrundelthd.main.engine.Entity
import me.getrundelthd.main.io.FontLoader
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage

val TOTAL_DISPLAY_TIME = 200.0
val FONT_SIZE = 48

class NPC(x: Double, y: Double, sprites: Array<BufferedImage>, val text: String) :
        Entity(x, y, ENTITY_SIZE, ENTITY_SIZE, 0, sprites) {

    var timePassed = 0.0
    val indecies = FontLoader.stringToIndices(text)
    private val ANIMTIME = 30.0

    override fun draw(g: Graphics) {
        super.draw(g)

        val perc = timePassed / TOTAL_DISPLAY_TIME
        val shownChars = text.substring(0, (text.length * perc).toInt())

        fun testString() {
            g.font = Font("Courier New", Font.BOLD, 50)
            g.drawString(shownChars, pos.x, pos.y)
        }


        val newLineIndecies = ArrayList<Int>(0)
        for (i in 0 until text.length)
            if (text[i] == '|') {
                newLineIndecies.add(i)
            }

        if (newLineIndecies.size == 0) {
            var posX = (WINDOW_WIDTH - text.length * FONT_SIZE * 1.1) / 2.0
            val posY = TILE_SIZE * 2.25

            for (i in 0 until (text.length * perc).toInt()) {
                g.drawImage(FontLoader.chars[indecies[i]], posX.toInt(), posY.toInt(), FONT_SIZE, FONT_SIZE, null)
                posX += FONT_SIZE * 1.1
            }
        } else {
            var posX = (WINDOW_WIDTH - newLineIndecies[0] * FONT_SIZE * 1.1) / 2.0
            var posY = TILE_SIZE * 2.25
            var index = 0

            for (i in 0 until (text.length * perc).toInt()) {
                if (indecies[i] != -1) {
                    g.drawImage(FontLoader.chars[indecies[i]], posX.toInt(), posY.toInt(), FONT_SIZE, FONT_SIZE, null)
                }
                if (i == newLineIndecies[index]) {
                    val numChars = if (newLineIndecies.size == 1)
                        text.length - newLineIndecies[0]
                    else {
                        index++
                        (newLineIndecies[index - 2] - newLineIndecies[index - 1])
                    }
                    posX = (WINDOW_WIDTH - numChars * FONT_SIZE) / 2.0
                    posY += FONT_SIZE * 1.1
                } else {
                    posX += FONT_SIZE * 1.1
                }
            }
        }
    }

    override fun update(delta: Double) {
        animTimer += delta

        if (timePassed < TOTAL_DISPLAY_TIME)
            timePassed += delta

        if (animTimer >= ANIMTIME) {
            spriteCount = (spriteCount + 1) % sprites.size
            sprite = sprites[spriteCount]
            animTimer = 0.0
        }


    }
}