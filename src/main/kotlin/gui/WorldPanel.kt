package gui

import freditor.Fronts
import logic.*

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Toolkit
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.SwingUtilities

private const val FOLDER_40 = "40"
private const val FOLDER_64 = "64"

private fun loadTile(folder: String, name: String): BufferedImage {
    val image = ImageIO.read(WorldPanel::class.java.getResourceAsStream("/tiles/$folder/$name.png"))
    val scale: Int = Toolkit.getDefaultToolkit().screenSize.height / 1000
    return if (scale <= 1) image else image.scaled(scale)
}

private fun BufferedImage.scaled(scale: Int): BufferedImage {
    require(scale >= 2) { "scale $scale too small" }

    val srcWidth = width
    val srcHeight = height
    val src = IntArray(srcWidth * srcHeight)
    getRGB(0, 0, srcWidth, srcHeight, src, 0, srcWidth)

    val dstWidth = srcWidth * scale
    val dstHeight = srcHeight * scale
    val dst = IntArray(dstWidth * dstHeight)

    var j = 0
    for (y in 0 until dstHeight) {
        val i = (y / scale) * srcHeight
        for (x in 0 until dstWidth) {
            dst[j++] = src[i + (x / scale)]
        }
    }
    val scaled = BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_INT_ARGB)
    scaled.setRGB(0, 0, dstWidth, dstHeight, dst, 0, dstWidth)
    return scaled
}

private fun BufferedImage.rotatedCounterclockwise(): BufferedImage {

    val srcWidth = width
    val srcHeight = height
    val src = IntArray(srcWidth * srcHeight)
    val dst = IntArray(srcHeight * srcWidth)
    getRGB(0, 0, srcWidth, srcHeight, src, 0, srcWidth)

    var j = 0
    for (x in srcWidth - 1 downTo 0) {
        var i = x
        for (y in 0 until srcHeight) {
            dst[j++] = src[i]
            i += srcWidth
        }
    }
    val rotated = BufferedImage(srcHeight, srcWidth, BufferedImage.TYPE_INT_ARGB)
    rotated.setRGB(0, 0, srcHeight, srcWidth, dst, 0, srcHeight)
    return rotated
}

class WorldPanel(private val world: World) : JPanel() {

    private var folder: String = Toolkit.getDefaultToolkit().screenSize.height.let { screenHeight ->
        if (screenHeight < 1000) FOLDER_40 else FOLDER_64
    }
    private var tileSize = 1 // smallest working dummy value before loadTiles() runs
    private var fullWall = 1
    private var halfWall = 1
    private var empty = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
    private var beeper = empty
    private var karels = emptyArray<BufferedImage>()
    private var horizontal = empty
    private var vertical = empty

    private fun loadTiles() {
        empty = loadTile(folder, "empty")
        beeper = loadTile(folder, "beeper")
        loadKarels()
        loadWalls()
        tileSize = beeper.width + fullWall

        val panelSize = Dimension(10 * tileSize + fullWall, 10 * tileSize + fullWall)
        minimumSize = panelSize
        preferredSize = panelSize
        maximumSize = panelSize
    }

    private fun loadKarels() {
        val east = loadTile(folder, "karel")
        val north = east.rotatedCounterclockwise()
        val west = north.rotatedCounterclockwise()
        val south = west.rotatedCounterclockwise()

        karels = arrayOf(east, north, west, south)
    }

    private fun loadWalls() {
        vertical = loadTile(folder, "wall")
        horizontal = vertical.rotatedCounterclockwise()
        fullWall = vertical.width
        halfWall = fullWall shr 1
    }

    override fun paintComponent(graphics: Graphics) {
        val world = world

        graphics.drawWalls(world)
        graphics.drawBeepersAndKarel(world)
        graphics.drawNumbers(world)

        // see https://stackoverflow.com/questions/19480076
        Toolkit.getDefaultToolkit().sync()
    }

    private fun Graphics.drawWalls(world: World) {
        for (y in 0..10) {
            var wall = WALL_TOP_LEFT + 2 * SOUTH * y
            for (x in 0..10) {
                if (world[wall + EAST]) {
                    drawImage(horizontal, x * tileSize + halfWall, y * tileSize, null)
                }
                if (world[wall + SOUTH]) {
                    drawImage(vertical, x * tileSize, y * tileSize + halfWall, null)
                }
            }
            wall += 2 * EAST
        }
    }

    private fun Graphics.drawBeepersAndKarel(world: World) {
        for (y in 0 until 10) {
            var cell = CELL_TOP_LEFT + 2 * SOUTH * y
            for (x in 0 until 10) {
                drawImage(if (world[cell]) beeper else empty, x * tileSize + fullWall, y * tileSize + fullWall, null)
            }
            cell += 2 * EAST
        }
        drawImage(karels[world.direction], world.x * tileSize + fullWall, world.y * tileSize + fullWall, null)
    }

    var binaryLines = 0

    private fun Graphics.drawNumbers(world: World) {
        val shift = if (world[CELL_BOTTOM_LEFT]) 24 else 0
        var y = 0
        var lines = binaryLines
        while (lines != 0) {
            var totalValue = 0
            var beeperValue = 1
            for (x in 9 downTo 2) {
                if (world[x, y]) {
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
        Fronts.front.drawString(this, x * tileSize + fullWall + leftPad, y * tileSize + fullWall, str, color)
    }

    private fun listenToMouse() {
        onMouseClicked { event ->
            if (!isEnabled) return@onMouseClicked

            if (SwingUtilities.isLeftMouseButton(event)) {
                toggleBeeper(event)
            } else if (SwingUtilities.isRightMouseButton(event)) {
                switchTileSize()
            }
            repaint()
        }
    }

    private fun toggleBeeper(event: MouseEvent) {
        val x = (event.x - halfWall) / tileSize
        val y = (event.y - halfWall) / tileSize
        world.toggleBeeper(x, y)
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
