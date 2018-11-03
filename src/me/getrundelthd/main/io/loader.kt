package me.getrundelthd.main.io


import com.sun.webkit.dom.EntityImpl
import me.getrundelthd.main.TILE_SIZE
import me.getrundelthd.main.drawing.SpriteData
import me.getrundelthd.main.drawing.TILES_PER_COL
import me.getrundelthd.main.drawing.TILES_PER_ROW
import me.getrundelthd.main.engine.ENTITY_SIZE
import me.getrundelthd.main.engine.Entity
import me.getrundelthd.main.engine.Tile
import me.getrundelthd.main.engine.interacables.NPC
import me.getrundelthd.main.engine.interacables.Portal
import me.getrundelthd.main.engine.map.Screen
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList

const val overWorldSpritesPath = "./res/gfx/sprites/overworld_extended2.png"
const val overWorldMetaPath = "./res/gfx/sprites/metaData_overworld.txt"
const val linkSpritesPath = "./res/gfx/sprites/link.png"
const val NPCSpritesPath = "./res/gfx/sprites/NPCs.png"
const val fontSpritePath = "./res/gfx/font/font.png"

// stores all overworld sprites in an array
// to get them look a sprite sheet indices
object OverworldSpriteLoader {
    val sprites: Array<SpriteData>

    init {

        // width and height of the sprite sheet
        val W = 18
        val H = 8

        // source size of an individual sprite
        val SIZE = 16

        // size of the spacing between single sprites
        val GAP = 1

        val spriteSheet = ImageIO.read(File(overWorldSpritesPath))

        val breader = BufferedReader(FileReader(File(overWorldMetaPath)))
        val metaData = Array<String>(W * H) { breader.readLine() }

        sprites = Array<SpriteData>(W * H) { i ->
            val x = i % W
            val y = i / W

            val img = loadSprite(spriteSheet, Rectangle(x * (SIZE + GAP) + 1,
                    y * (SIZE + GAP) + 1,
                    16,
                    16))

            val meta = metaData[x + y * W].split(" ")
            SpriteData(img, meta[0].toBoolean(), meta[1].toBoolean())
        }
    }

    fun getSize() = sprites.size
}

// (0,1) = SOUTH, (2) = NORTH, (3, 4) = EAST/WEST, (5) = IDLE
object PlayerSpritesLoader {
    val sprites: Array<BufferedImage>

    init {

        val spriteSheet = ImageIO.read(File(linkSpritesPath))
        val GAP_TO_BORDER = 1
        val GAP = 2
        val SPRITE_SIZE = 16

        // TODO: load all sprites
        sprites = Array<BufferedImage>(14) { i ->

            val x = i * (SPRITE_SIZE + GAP) + GAP_TO_BORDER
            val y = GAP_TO_BORDER

            loadSprite(spriteSheet, Rectangle(x, y, SPRITE_SIZE, SPRITE_SIZE))
        }
    }
}

// stores all the charactes of the ingame font
object FontLoader {

    val chars: Array<BufferedImage>
    val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ,!'&.\"?-    "

    init {

        val spriteSheet = ImageIO.read(File(fontSpritePath))
        val GAP_TO_LEFT = 9
        val GAP_TO_TOP = 6
        val SPACING = 8
        val SPRITE_SIZE = 8
        val W = 16
        val H = 3

        chars = Array(W * H) { i ->

            val x = i % W
            val y = i / W

            // println("$x ${GAP_TO_LEFT + x * (SPACING + SPRITE_SIZE)}")

            loadSprite(spriteSheet, Rectangle(GAP_TO_LEFT + x * (SPACING + SPRITE_SIZE),
                    GAP_TO_TOP + y * (SPACING + SPRITE_SIZE),
                    SPRITE_SIZE,
                    SPRITE_SIZE))
        }
    }

    fun stringToIndices(s: String) = Array(s.length) { i ->
        val v = s.toUpperCase()
        alphabet.indexOf(v[i])
    }

}

object NPCSpriteLoader {

    val sprites: Array<BufferedImage>

    init {
        val W = 4
        val H = 1
        val TILE_SIZE = 16
        val SPACING = 3

        val spriteSheet = ImageIO.read(File(NPCSpritesPath))
        sprites = Array(W * H) { i ->

            val x = i % W
            val y = 0
            loadSprite(spriteSheet, Rectangle(x * (TILE_SIZE + SPACING),
                    y,
                    TILE_SIZE,
                    TILE_SIZE))
        }
    }
}


private fun loadSprite(b: BufferedImage, location: Rectangle): BufferedImage {
    return b.getSubimage(location.x,
            location.y,
            location.width,
            location.height)
}

/*  map file format
   ------------------
    @map_type
    mappointer N ,mappointer E ,mappointer S ,mappointer W [or @None]
    tile ids...
    portal specs (p posx posy offset screen)
    npc specs (npc posx posy indexSprite text[_ = space, | = newLine])

 */

fun loadScreen(p: Path): Screen {
    try {
        val reader = BufferedReader(FileReader(p.toFile()))

        val type = reader.readLine()

        val pointers = reader.readLine()
        val mapPointers = pointers.split(",")

        val tiles = Array<Tile?>(TILES_PER_ROW * TILES_PER_COL) { null }
        val extraTiles = mutableListOf<Tile>()

        var index = 0
        for (j in 0 until TILES_PER_COL) {

            val line = reader.readLine()
            val data = line!!.split(",")

            var pos = 0

            for (elem in data) {
                when {
                    !elem.contains("[") -> {
                        tiles[index] = Tile(pos * TILE_SIZE, j * TILE_SIZE,
                                OverworldSpriteLoader.sprites[elem.toInt()])
                        pos++
                        index++
                    }
                    else -> {
                        val tileData = elem.split("[")
                        val sd = OverworldSpriteLoader.sprites[tileData[0].toInt()]
                        val regex = tileData[1].replace("]", "")
                        val lowerBound = when {
                            regex.contains("-") -> regex.split("-")[0].toInt()
                            else -> pos
                        }

                        val upperBound = when {
                            regex.contains("-") -> regex.split("-")[1].toInt()
                            else -> TILES_PER_ROW - 1
                        }

                        for (i in lowerBound..upperBound) {
                            tiles[index] = Tile(i * TILE_SIZE,
                                    j * TILE_SIZE, sd)
                            index++
                        }

                        pos = upperBound + 1
                    }
                }
            }
        }

        val portals: ArrayList<Portal> = ArrayList()
        val entities: ArrayList<Entity> = ArrayList()

        var line = reader.readLine()

        fun loadPortal(data: List<String>): Portal {
            val posx = data[1].toDouble() * TILE_SIZE
            val posy = data[2].toDouble() * TILE_SIZE
            val newX = data[3].toDouble() * TILE_SIZE
            val newY = data[4].toDouble() * TILE_SIZE
            val nextS = Paths.get("res", "maps", when {
                data[5].startsWith("c") -> "caves"
                data[5].startsWith("d") -> "dungeons"
                else -> "overworld"
            }, "${data[5]}.txt")

            return Portal(posx, posy, data[5].startsWith("c"), newX, newY, nextS)
        }

        fun loadNPC(data: List<String>): Entity {

            val posX = data[1].toDouble() * ENTITY_SIZE
            val posY = data[2].toDouble() * ENTITY_SIZE
            val sprites = if (!data[3].contains("[")) {
                arrayOf(NPCSpriteLoader.sprites[data[3].toInt()])
            } else {
                val spriteIndecies = data[3]
                        .replace("[", "")
                        .replace("]", "")
                        .split(",")
                        .map { it.toInt() }
                Array(spriteIndecies.size) {
                    NPCSpriteLoader.sprites[spriteIndecies[it]]
                }
            }
            val text = data[4].replace("null", "").toUpperCase().replace("_", " ")

            return NPC(posX, posY, sprites, text)
        }

        fun loadInvisBlock(data: List<String>): List<Tile> {
            val posX = data[1].toInt() * TILE_SIZE
            val invisSprite = OverworldSpriteLoader.sprites[87]

            if (data[2].contains("[")) {
                val posData = data[2]
                        .replace("[", "")
                        .replace("]", "")
                        .split("-")
                        .map { it.toInt() }

                val blocks = mutableListOf<Tile>()

                for (i in posData[0] until posData[1]) {
                    blocks.add(Tile(posX * TILE_SIZE, i * TILE_SIZE, invisSprite))
                }

                return blocks
            } else {
                val posY = data[2].toDouble() * TILE_SIZE

                return listOf(Tile(posX.toInt(), posY.toInt(), invisSprite))
            }
        }

        while (line != null) {

            val data = line.split(" ")

            when (data[0]) {
                "p" -> portals.add(loadPortal(data))
                "npc" -> entities.add(loadNPC(data))
                "invis" -> extraTiles.addAll(loadInvisBlock(data))
            }

            line = reader.readLine()
        }

        return Screen(tiles as Array<Tile>, entities.toTypedArray(), mapPointers.toTypedArray(), when (portals.size) {
            0 -> null
            else -> portals.toTypedArray()
        })
    } catch (e: Exception) {
        e.printStackTrace()
        return Screen()
    }
}