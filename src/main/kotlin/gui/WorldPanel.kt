package gui

import freditor.Fronts
import logic.Problem
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
        loadKarels()
        loadWalls()

        val panelSize = Dimension(tileSize * Problem.WIDTH, tileSize * Problem.HEIGHT)
        minimumSize = panelSize
        preferredSize = panelSize
        maximumSize = panelSize
    }

    private fun loadKarels() {
        val east = loadTile("karel")
        val north = east.rotatedCounterclockwise()
        val west = north.rotatedCounterclockwise()
        val south = west.rotatedCounterclockwise()

        karels = arrayOf(east, north, west, south)
    }

    private fun loadWalls() {
        walls = Array(16) { loadTile("cross") }

        val east = loadTile("wall")
        val north = east.rotatedCounterclockwise()
        val west = north.rotatedCounterclockwise()
        val south = west.rotatedCounterclockwise()

        intArrayOf(1, 3, 5, 7, 9, 11, 13, 15).forEach { drawWall(it, east) }
        intArrayOf(2, 3, 6, 7, 10, 11, 14, 15).forEach { drawWall(it, north) }
        intArrayOf(4, 5, 6, 7, 12, 13, 14, 15).forEach { drawWall(it, west) }
        intArrayOf(8, 9, 10, 11, 12, 13, 14, 15).forEach { drawWall(it, south) }
    }

    private fun drawWall(index: Int, wall: BufferedImage) {
        walls[index].graphics.drawImage(wall, 0, 0, null)
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
        for (y in 0 until Problem.HEIGHT) {
            for (x in 0 until Problem.WIDTH) {
                drawTile(x, y, walls[world.floorPlan.wallsAt(x, y)])
                if (world.beeperAt(x, y)) {
                    drawTile(x, y, beeper)
                }
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
