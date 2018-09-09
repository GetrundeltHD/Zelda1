package me.getrundelthd.main.drawing

import me.getrundelthd.main.TILE_SIZE
import me.getrundelthd.main.blackPerc
import me.getrundelthd.main.currFps
import me.getrundelthd.main.engine.GAMESTATE
import me.getrundelthd.main.engine.Player
import me.getrundelthd.main.engine.base.GameObject
import me.getrundelthd.main.mainGameState
import java.awt.Canvas
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.image.BufferedImage
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.floor

data class SpriteData(val sprite : BufferedImage, val solid : Boolean, val water : Boolean)

val TILES_PER_COL = 11
val TILES_PER_ROW = 16

val WINDOW_WIDTH = TILE_SIZE * TILES_PER_ROW
val WINDOW_HEIGHT = TILE_SIZE * TILES_PER_COL

class DrawHandler : Canvas() {

    val window = JFrame()
    val objects = CopyOnWriteArrayList<GameObject>()
    val pane = JPanel()

    init {
        window.title = "The Legend of Zelda"
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        pane.preferredSize = Dimension(me.getrundelthd.main.drawing.WINDOW_WIDTH,
                me.getrundelthd.main.drawing.WINDOW_HEIGHT)
        pane.maximumSize = Dimension(me.getrundelthd.main.drawing.WINDOW_WIDTH,
                me.getrundelthd.main.drawing.WINDOW_HEIGHT)
        pane.minimumSize = Dimension(me.getrundelthd.main.drawing.WINDOW_WIDTH,
                me.getrundelthd.main.drawing.WINDOW_HEIGHT)
        window.contentPane = pane
        this.setSize(me.getrundelthd.main.drawing.WINDOW_WIDTH, me.getrundelthd.main.drawing.WINDOW_HEIGHT)
        pane.add(this)
        window.pack()
        window.setLocationRelativeTo(null)

        window.isResizable = false
        window.isVisible = true

        addKeyListener(Player.PlayerInputHandler)
        addFocusListener(Player.PlayerInputHandler)
        requestFocus()

        createBufferStrategy(3)
    }

    fun render() {

        val g = bufferStrategy.drawGraphics

        fun drawFPS() {
            g.font = Font("Courier New", Font.BOLD, 42)
            g.color = Color.WHITE
            g.drawString("FPS: $currFps", 10, 50)
        }

        fun drawStartRect(perc: Double) {
            val step = 10
            val amount = floor(perc) - (floor(perc) % step)

            val x1 = ((WINDOW_WIDTH / 2.0) * (amount / 100.0)).toInt()
            val x2 = WINDOW_WIDTH  - ((WINDOW_WIDTH / 2.0) * (amount / 100.0)).toInt()

            g.color = Color.BLACK
            g.fillRect(0, 0, x1, WINDOW_HEIGHT)
            g.fillRect(x2, 0, WINDOW_WIDTH, WINDOW_HEIGHT)
        }

        g.color = Color(0xffffff)
        g.fillRect(0,  0, me.getrundelthd.main.drawing.WINDOW_WIDTH, me.getrundelthd.main.drawing.WINDOW_HEIGHT)

        objects.sortWith(Comparator.reverseOrder())
        objects.forEach {
            it?.draw(g)
        }

        if(mainGameState == GAMESTATE.START) {
            drawStartRect(blackPerc)
        }

        // drawFPS()

        bufferStrategy.show()

    }
}