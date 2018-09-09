package me.getrundelthd.main.engine

import me.getrundelthd.main.engine.base.GameObject
import java.awt.image.BufferedImage
import java.util.stream.Collectors


class Animator(val sprites : Array<BufferedImage>, val switchTime : Double, val rule : (a : Animator) -> Unit) {

    var currTime = 0.0
    var state = 0

    val sprite
        get() = sprites[state]


    fun update(delta: Double) {

        currTime -= delta
        rule(this)

        if (currTime <= 0) {
            currTime = switchTime
        }
    }

}

const val ENTITY_SIZE = 74

// entities are always solid
abstract class Entity(x: Double, y: Double, w: Int, h: Int, p : Int, val sprites: Array<BufferedImage>)
    : GameObject(x, y, w, h, true, false, true, null, p) {

    /**
     *  if movement is implemented -> seperate collision on the x and y - axes
     */

    var _tiles: List<GameObject> = ArrayList()

   // put the first sprite
    init {
        sprite = sprites[0]
    }

    protected var velX = 0.0
    protected var velY = 0.0

    protected var animTimer = 0.0

    protected var spriteCount = 0

    protected var dir: DIR = DIR.SOUTH

    open fun update(delta: Double) {
        animTimer += delta

        // collision detection
        x += velX * delta
        val overlapping = _tiles.stream().filter { it.collides(this) }.collect(Collectors.toList())

        if (!overlapping.isEmpty()) {
            if (velX > 0.0) {
                if (right > overlapping[0].left)
                    x = (overlapping[0].left - hitbox.width).toDouble()
            } else if (velX < 0.0)
                if (left < overlapping[0].right)
                    x = (overlapping[0].right).toDouble()

        }

        y += velY * delta
        val overlapping2 = _tiles.stream().filter { it.collides(this) }.collect(Collectors.toList())
        if (!overlapping2.isEmpty()) {
            if (velY > 0.0) {
                if (bottom > overlapping2[0].top)
                    y = (overlapping2[0].top - hitbox.height).toDouble()
            } else if (velY < 0.0)
                if (top < overlapping2[0].bottom)
                    y = (overlapping2[0].bottom).toDouble()
        }

        if (velX > 0) {
            dir = DIR.EAST
        } else if (velX < 0) {
            dir = DIR.WEST
        } else if (velY > 0) {
            dir = DIR.SOUTH
        } else if (velY < 0) {
            dir = DIR.NORTH
        }

    }

}