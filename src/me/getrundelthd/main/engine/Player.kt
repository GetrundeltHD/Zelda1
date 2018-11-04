package me.getrundelthd.main.engine

import me.getrundelthd.main.console
import me.getrundelthd.main.drawHandler
import me.getrundelthd.main.engine.items.ITEMS
import me.getrundelthd.main.mainGameState
import me.getrundelthd.main.utils.flip
import java.awt.Color
import java.awt.Graphics
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.awt.event.KeyListener
import java.awt.image.BufferedImage

const val PLAYER_SPEED = 6.0
const val CONSOLE_COOLDOWN = 20.0

class Player(x: Double, y: Double, sprites: Array<BufferedImage>) :
        Entity(x, y, ENTITY_SIZE, ENTITY_SIZE, 0, sprites) {

    val getheredItems = listOf<ITEMS>()

    val animator: Animator
    var consoleTimer = CONSOLE_COOLDOWN

    init {
        val animSprites = arrayOf(
                sprites[0],
                sprites[1],
                sprites[3],
                sprites[4],
                sprites[2],
                flip(sprites[2]),
                flip(sprites[3]),
                flip(sprites[4])
        )

        // init the animator with the animation rules
        animator = Animator(animSprites, 12.5) {
            if (!(velX == 0.0 && velY == 0.0)) {
                val state = it.state
                when (dir) {
                    DIR.NORTH -> if (state != 4 && state != 5)
                        it.state = (if (it.state == 4) 5 else 4)
                    else if (it.currTime <= 0)
                        it.state = (if (it.state == 4) 5 else 4)
                    DIR.SOUTH -> if (state != 0 && state != 1)
                        it.state = (if (it.state == 0) 1 else 0)
                    else if (it.currTime <= 0)
                        it.state = (if (it.state == 0) 1 else 0)
                    DIR.EAST -> if (state != 2 && state != 3)
                        it.state = (if (it.state == 2) 3 else 3)
                    else if (it.currTime <= 0)
                        it.state = (if (it.state == 2) 3 else 2)
                    DIR.WEST -> if (state != 6 && state != 7)
                        it.state = (if (it.state == 6) 7 else 6)
                    else if (it.currTime <= 0)
                        it.state = (if (it.state == 6) 7 else 6)
                }
            }

        }

    }


    override fun update(delta: Double) {

        when (mainGameState) {
            GAMESTATE.MAIN -> {

                if(consoleTimer <= CONSOLE_COOLDOWN)
                    consoleTimer += delta

                when {
                    PlayerInputHandler.ioStates[VK_W] ||
                            PlayerInputHandler.ioStates[VK_UP] -> {
                        velX = 0.0
                        velY = -PLAYER_SPEED
                    }

                    PlayerInputHandler.ioStates[VK_A] ||
                            PlayerInputHandler.ioStates[VK_LEFT] -> {
                        velX = -PLAYER_SPEED
                        velY = 0.0
                    }

                    PlayerInputHandler.ioStates[VK_D] ||
                            PlayerInputHandler.ioStates[VK_RIGHT] -> {
                        velX = PLAYER_SPEED
                        velY = 0.0
                    }

                    PlayerInputHandler.ioStates[VK_S] ||
                            PlayerInputHandler.ioStates[VK_DOWN] -> {
                        velX = 0.0
                        velY = PLAYER_SPEED
                    }

                    PlayerInputHandler.ioStates[VK_CONTROL] -> {
                        if (consoleTimer >= CONSOLE_COOLDOWN) {
                            mainGameState = GAMESTATE.CONSOLE
                            drawHandler.UI_Elements.add(console)
                            drawHandler.addKeyListener(console.consoleHandler)
                        }
                    }

                    else -> {
                        velX = 0.0
                        velY = 0.0
                    }
                }
            }
            else -> {
                velX = 0.0
                velY = 0.0
            }
        }

        super.update(delta)

        animator.update(delta)
        sprite = animator.sprite
    }

    override fun draw(g: Graphics) {
        super.draw(g)

        fun drawHitbox() {
            g.color = Color(255, 0, 0, 100)
            g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height)
        }

        drawHitbox()
    }

    override fun toString(): String {
        return "<Player x: ${pos.x} y: ${pos.y} velX: $velX velY: $velY>"
    }

    object PlayerInputHandler : KeyListener, FocusListener {

        val ioStates = BooleanArray(255) { false }

        override fun keyTyped(e: KeyEvent?) {
        }

        override fun keyPressed(e: KeyEvent) {
            ioStates[e.keyCode] = true
        }

        override fun keyReleased(e: KeyEvent) {
            ioStates[e.keyCode] = false
        }

        override fun focusLost(e: FocusEvent?) {
            for (i in ioStates.indices) {
                ioStates[i] = false
            }
        }

        override fun focusGained(e: FocusEvent?) {
        }
    }
}