package gui

import freditor.Fronts
import logic.Problem
import logic.World

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Toolkit
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference

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
    require(width == height) { "$width * $height is not a square" }

    val srcSize = width
    val dstSize = srcSize * scale
    val src = IntArray(srcSize * srcSize)
    val dst = IntArray(dstSize * dstSize)
    getRGB(0, 0, srcSize, srcSize, src, 0, srcSize)

    var j = 0
    for (y in 0 until dstSize) {
        val i = (y / scale) * srcSize
        for (x in 0 until dstSize) {
            dst[j++] = src[i + (x / scale)]
        }
    }
    val scaled = BufferedImage(dstSize, dstSize, BufferedImage.TYPE_INT_ARGB)
    scaled.setRGB(0, 0, dstSize, dstSize, dst, 0, dstSize)
    return scaled
}

private fun BufferedImage.rotatedCounterclockwise(): BufferedImage {
    require(width == height) { "$width * $height is not a square" }

    val tileSize = width
    val src = IntArray(tileSize * tileSize)
    val dst = IntArray(tileSize * tileSize)
    getRGB(0, 0, tileSize, tileSize, src, 0, tileSize)

    var j = 0
    for (y in 0 until tileSize) {
        var i = tileSize - 1 - y
        for (x in 0 until tileSize) {
            dst[j++] = src[i]
            i += tileSize
        }
    }
    val rotated = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
    rotated.setRGB(0, 0, tileSize, tileSize, dst, 0, tileSize)
    return rotated
}

class WorldPanel(private val atomicWorld: AtomicReference<World>) : JPanel() {

    private var folder: String = Toolkit.getDefaultToolkit().screenSize.height.let { screenHeight ->
        if (screenHeight < 1000) FOLDER_40 else FOLDER_64
    }
    private var tileSize = 1 // smallest working dummy value before loadTiles() runs
    private var beeper = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
    private var karels = emptyArray<BufferedImage>()
    private var walls = emptyArray<BufferedImage>()

    private fun loadTiles() {
        beeper = loadTile(folder, "beeper")
        tileSize = beeper.width
        loadKarels()
        loadWalls()

        val panelSize = Dimension(tileSize * Problem.WIDTH, tileSize * Problem.HEIGHT)
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
        walls = Array(16) { loadTile(folder, "cross") }

        val east = loadTile(folder, "wall")
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

    override fun paintComponent(graphics: Graphics) {
        val world = atomicWorld.get()

        graphics.drawWallsAndBeepers(world)
        graphics.drawKarel(world)
        graphics.drawNumbers(world)

        // see https://stackoverflow.com/questions/19480076
        Toolkit.getDefaultToolkit().sync()
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
        onMouseClicked { event ->
            if (SwingUtilities.isLeftMouseButton(event)) {
                toggleBeeper(event)
            } else if (SwingUtilities.isRightMouseButton(event)) {
                switchTileSize()
            }
            repaint()
        }
    }

    private fun toggleBeeper(event: MouseEvent) {
        val x = event.x / tileSize
        val y = event.y / tileSize
        atomicWorld.updateAndGet { it.toggleBeeper(x, y) }
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
