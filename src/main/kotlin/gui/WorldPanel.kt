package gui

import freditor.Fronts
import logic.World

import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.SwingUtilities

private const val FOLDER_40 = "40"
private const val FOLDER_64 = "64"

class WorldPanel(var world: World) : JPanel() {

    private var folder: String = if (screenHeight < 1000) FOLDER_40 else FOLDER_64

    private fun loadTile(name: String): BufferedImage {
        val image = ImageIO.read(WorldPanel::class.java.getResourceAsStream("/tiles/$folder/$name.png"))
        val scale: Int = screenHeight / 1000
        return if (scale <= 1) image else image.scaled(scale)
    }

    var tileSize = 0
        private set

    private var beeper = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    private var karels = emptyArray<BufferedImage>()
    private var walls = emptyArray<BufferedImage>()

    private fun loadTiles() {
        beeper = loadTile("beeper")
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
        walls = Array(16) { i ->
            loadTile("cross").apply {
                if (i and 1 != 0) graphics.drawImage(east, 0, 0, null)
                if (i and 2 != 0) graphics.drawImage(north, 0, 0, null)
                if (i and 4 != 0) graphics.drawImage(west, 0, 0, null)
                if (i and 8 != 0) graphics.drawImage(south, 0, 0, null)
            }
        }

        val panelSize = Dimension(tileSize * 10, tileSize * 10)
        minimumSize = panelSize
        preferredSize = panelSize
        maximumSize = panelSize
    }

    var antWorld: World? = null
    var showAntWorld: Boolean = false

    override fun paintComponent(graphics: Graphics) {
        var world = antWorld
        if (world == null || !showAntWorld) {
            world = this.world
        }

        graphics.drawWallsAndBeepers(world)
        graphics.drawKarel(world)
        graphics.drawNumbers(world)

        flushGraphicsBuffers()
    }

    private fun Graphics.drawWallsAndBeepers(world: World) {
        var position = 0
        for (y in 0 until 10) {
            for (x in 0 until 10) {
                drawTile(x, y, walls[world.floorPlan.wallsAt(position)])
                if (world.beeperAt(position)) {
                    drawTile(x, y, beeper)
                }
                ++position
            }
        }
    }

    private fun Graphics.drawKarel(world: World) {
        drawTile(world.x, world.y, karels[world.direction])
    }

    private fun Graphics.drawTile(x: Int, y: Int, tile: BufferedImage) {
        drawImage(tile, x * tileSize, y * tileSize, null)
    }

    var binaryLines = 0

    private fun Graphics.drawNumbers(world: World) {
        val shift = if (world.beeperAt(0, 9)) 24 else 0
        var y = 0
        var lines = binaryLines
        while (lines != 0) {
            var totalValue = 0
            var beeperValue = 1
            for (x in 9 downTo 2) {
                if (world.beeperAt(x, y)) {
                    drawNumber(x, y, beeperValue, 0x000000)
                    totalValue += beeperValue
                }
                beeperValue = beeperValue.shl(shift + 1).shr(shift)
            }
            if (lines.and(1) != 0) {
                drawNumber(0, y, totalValue, 0x008000)
            }
            lines = lines.shr(1)
            ++y
        }
    }

    private fun Graphics.drawNumber(x: Int, y: Int, value: Int, color: Int) {
        val str = "%3d".format(value)
        val width = str.length * Fronts.front.width
        val leftPad = (tileSize - width).shr(1)
        Fronts.front.drawString(this, x * tileSize + leftPad, y * tileSize, str, color)
    }

    private fun listenToMouse() {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (!event.component.isEnabled) return

                if (SwingUtilities.isLeftMouseButton(event)) {
                    if (antWorld != null) {
                        showAntWorld = !showAntWorld
                    }
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    switchTileSize()
                }
                repaint()
            }

            override fun mouseEntered(event: MouseEvent) {
                showAntWorld = true
                if (antWorld != null) {
                    repaint()
                }
            }

            override fun mouseExited(event: MouseEvent) {
                showAntWorld = false
                if (antWorld != null) {
                    repaint()
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
