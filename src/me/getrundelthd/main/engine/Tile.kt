package me.getrundelthd.main.engine

import me.getrundelthd.main.TILE_SIZE
import me.getrundelthd.main.drawing.SpriteData
import me.getrundelthd.main.engine.base.GameObject
import java.awt.*

// s for solid
class Tile(x: Int, y: Int, sprite: SpriteData)
    : GameObject(x.toDouble(), y.toDouble(), TILE_SIZE, TILE_SIZE,
        sprite.solid, sprite.water, true, sprite.sprite, 100) {

    override fun draw(g: Graphics) {

        fun drawGrid() {
            g.color = Color(100, 100, 100, 100)
            (g as Graphics2D).stroke = BasicStroke(3f)
            g.drawRect(rect.x, rect.y, rect.width, rect.height)
        }

        fun drawInfo() {
            g.color = Color.RED
            g.font = Font("Courier New", Font.BOLD, 20)
            g.drawString(solid.toString(), pos.x, pos.y + 30)
        }


        if (sprite != null && visible) {
            g.drawImage(sprite, pos.x, pos.y, TILE_SIZE, TILE_SIZE, null)

            // DEBUG METHOD
            drawGrid()
            // drawInfo()
        }
    }

    override fun toString(): String {
        return "<Tile x: $x y: $y solid: $solid>"
    }
}