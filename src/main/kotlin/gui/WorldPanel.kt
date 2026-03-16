package gui

import freditor.Fronts
import logic.World
import logic.WorldDiff
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.SwingUtilities

private const val FOLDER_40 = "40"
private const val FOLDER_64 = "64"

class WorldPanel(initialWorld: World) : JComponent() {

    var world = initialWorld
        set(new) {
            val old = field
            if (old === new) return
            field = new

            if (!old.floorPlan.sameAs(new.floorPlan)) {
                paintImmediately(0, 0, width, height)
                return
            }

            WorldDiff(old, new, binaryLines).run {
                if (!isEmpty()) {
                    paintImmediately(left * tileSize, top * tileSize, width() * tileSize, height() * tileSize)
                }
            }
        }

    private var folder: String = if (screenHeight < 1000) FOLDER_40 else FOLDER_64

    private fun loadTile(name: String): BufferedImage {
        val image = ImageIO.read(WorldPanel::class.java.getResourceAsStream("/tiles/$folder/$name.png"))
        val scale: Int = screenHeight / 1000
        return if (scale <= 1) image else image.scaled(scale)
    }

    var tileSize = 0
        private set

    private var karels = emptyArray<BufferedImage>()
    private var wallsAndBeepers = emptyArray<BufferedImage>()

    private fun loadTiles() {
        val beeper = loadTile("beeper")
        tileSize = beeper.width

        var east = loadTile("karel")
        var north = east.rotatedCounterclockwise()
        var west = north.rotatedCounterclockwise()
        var south = west.rotatedCounterclockwise()
        karels = arrayOf(east, north, west, south)

        east = loadTile("wall")
        north = east.rotatedCounterclockwise()
        west = north.rotatedCounterclockwise()
        south = west.rotatedCounterclockwise()
        wallsAndBeepers = Array(32) { i ->
            loadTile("cross").apply {
                if (i and 1 != 0) graphics.drawImage(east, 0, 0, null)
                if (i and 2 != 0) graphics.drawImage(north, 0, 0, null)
                if (i and 4 != 0) graphics.drawImage(west, 0, 0, null)
                if (i and 8 != 0) graphics.drawImage(south, 0, 0, null)
                if (i and 16 != 0) graphics.drawImage(beeper, 0, 0, null)
            }
        }

        val panelSize = Dimension(tileSize * 10, tileSize * 10)
        minimumSize = panelSize
        preferredSize = panelSize
        maximumSize = panelSize
    }

    override fun paintComponent(graphics: Graphics) {
        val bounds = graphics.clipBounds
        val left = bounds.x / tileSize
        val right = (bounds.x + bounds.width - 1) / tileSize
        val top = bounds.y / tileSize
        val bottom = (bounds.y + bounds.height - 1) / tileSize

        graphics.drawWallsAndBeepers(left, right, top, bottom)
        graphics.drawKarel()
        graphics.drawNumbers(left, right, top, bottom)

        flushGraphicsBuffers()
    }

    private fun Graphics.drawWallsAndBeepers(left: Int, right: Int, top: Int, bottom: Int) {
        for (y in top..bottom) {
            for (x in left..right) {
                val position = y * 10 + x
                var index = world.floorPlan.wallsAt(position)
                if (world.beeperAt(position)) {
                    index += 16
                }
                drawTile(x, y, wallsAndBeepers[index])
            }
        }
    }

    private fun Graphics.drawKarel() {
        drawTile(world.x, world.y, karels[world.direction])
    }

    private fun Graphics.drawTile(x: Int, y: Int, tile: BufferedImage) {
        drawImage(tile, x * tileSize, y * tileSize, null)
    }

    var binaryLines = 0

    private fun Graphics.drawNumbers(left: Int, right: Int, top: Int, bottom: Int) {
        if (binaryLines == 0) return

        val drawSum = (left == 0)
        val left = left.coerceAtLeast(2)
        val bytes = world.allBytes()
        val shift = if (world.beeperAt(0, 9)) 24 else 0

        for (y in top..bottom) {
            if (binaryLines ushr y and 1 != 0) {
                if (drawSum) {
                    drawNumber(0, y, bytes[y] shl shift shr shift, 0x008000)
                }
                for (x in left..right) {
                    if (world.beeperAt(x, y)) {
                        drawNumber(x, y, 512 ushr x shl shift shr shift, 0x000000)
                    }
                }
            }
        }
    }

    private fun Graphics.drawNumber(x: Int, y: Int, value: Int, color: Int) {
        val width = Fronts.front.width * if (value <= -100) 4 else 3
        val rightPad = (tileSize - width) shr 1
        Fronts.front.drawIntRight(this, (x + 1) * tileSize - rightPad, y * tileSize, value, color)
    }

    var leftWorld: World? = null
    var rightWorld: World? = null

    private fun listenToMouse() {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (!event.component.isEnabled) return

                if (SwingUtilities.isLeftMouseButton(event)) {
                    leftWorld?.let {
                        world = it
                        return
                    }
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    rightWorld?.let {
                        world = it
                        return
                    }
                    switchTileSize()
                }
            }
        })
    }

    private fun switchTileSize() {
        when (folder) {
            FOLDER_40 -> folder = FOLDER_64
            FOLDER_64 -> folder = FOLDER_40
        }
        loadTiles()
        revalidate()
    }

    init {
        loadTiles()
        listenToMouse()
    }
}
