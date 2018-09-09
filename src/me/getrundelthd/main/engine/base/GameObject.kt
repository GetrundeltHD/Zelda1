package me.getrundelthd.main.engine.base

import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.math.roundToInt


// TODO: tinker with water
abstract class GameObject(var x: Double, var y: Double, val w: Int, val h: Int,
                          val solid : Boolean, val water : Boolean,
                          val visible: Boolean, var sprite: BufferedImage?, var drawOrder : Int)
    : Drawable, Comparable<GameObject> {

    val pos: Point
        get() = Point(x.roundToInt(), y.roundToInt())

    val rect: Rectangle
        get() = Rectangle(x.roundToInt(), y.roundToInt(), w, h)

    val hitbox
        get() = rect

    val left
        get() = hitbox.x

    val right
        get() = hitbox.x + hitbox.width

    val top
        get() = hitbox.y

    val bottom
        get() = hitbox.y + hitbox.height

    fun collides(o: GameObject) = o.solid && this.solid && hitbox.intersects(o.hitbox)

    override fun draw(g: Graphics) {
        if (sprite != null && visible)
            g.drawImage(sprite, pos.x, pos.y, rect.width, rect.height, null)

    }

    override fun compareTo(other: GameObject): Int {
        return Integer.compare(drawOrder, other.drawOrder)
    }
}