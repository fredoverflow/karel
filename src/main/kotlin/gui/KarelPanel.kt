package gui

import freditor.Front
import logic.KarelWorld
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

private const val TILE_SIZE_MEDIUM = 40
private const val TILE_SIZE_LARGE = 64

private fun readTile(size: Int, name: String): BufferedImage {
    return ImageIO.read(KarelPanel::class.java.getResourceAsStream("/tiles/$size/$name.png"))
}

private fun BufferedImage.rotatedCounterclockwise(): BufferedImage {
    require(width == height) { "image is not a square" }

    val tileSize = width
    val src = IntArray(tileSize * tileSize)
    val dst = IntArray(tileSize * tileSize)
    getRGB(0, 0, tileSize, tileSize, src, 0, tileSize)
    for (y in 0 until tileSize) {
        for (x in 0 until tileSize) {
            dst[y * tileSize + x] = src[x * tileSize + (tileSize - 1 - y)]
        }
    }
    val rotated = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
    rotated.setRGB(0, 0, tileSize, tileSize, dst, 0, tileSize)
    return rotated
}

class KarelPanel(private val atomicKarel: AtomicReference<KarelWorld>) : JPanel() {

    private var tileSize: Int = Toolkit.getDefaultToolkit().screenSize.height.let { screenHeight ->
        if (screenHeight < 1000) TILE_SIZE_MEDIUM else TILE_SIZE_LARGE
    }

    private var beeper = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
    private var karels = emptyArray<BufferedImage>()
    private var walls = emptyArray<BufferedImage>()

    private fun initialize() {
        beeper = readTile(tileSize, "beeper")
        initializeKarels()
        initializeWalls()

        val panelSize = Dimension(tileSize * World.WIDTH, tileSize * World.HEIGHT)
        minimumSize = panelSize
        preferredSize = panelSize
        maximumSize = panelSize
    }

    private fun initializeKarels() {
        val east = readTile(tileSize, "karel")
        val north = east.rotatedCounterclockwise()
        val west = north.rotatedCounterclockwise()
        val south = west.rotatedCounterclockwise()

        karels = arrayOf(east, north, west, south)
    }

    private fun initializeWalls() {
        walls = Array(16) { readTile(tileSize, "cross") }

        val east = readTile(tileSize, "wall")
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
        val karel = atomicKarel.get()

        graphics.drawWallsAndBeepers(karel)
        graphics.drawKarel(karel)
        graphics.drawNumbers(karel)

        // see https://stackoverflow.com/questions/19480076
        Toolkit.getDefaultToolkit().sync()
    }

    private fun Graphics.drawWallsAndBeepers(karel: KarelWorld) {
        for (y in 0 until World.HEIGHT) {
            for (x in 0 until World.WIDTH) {
                drawTile(x, y, walls[karel.floorPlan.wallsAt(x, y)])
                if (karel.beeperAt(x, y)) {
                    drawTile(x, y, beeper)
                }
            }
        }
    }

    private fun Graphics.drawKarel(karel: KarelWorld) {
        drawTile(karel.x, karel.y, karels[karel.direction])
    }

    private fun Graphics.drawTile(x: Int, y: Int, tile: BufferedImage) {
        drawImage(tile, x * tileSize, y * tileSize, null)
    }

    var binaryLines = 0

    private fun Graphics.drawNumbers(karel: KarelWorld) {
        for (y in 0 until binaryLines) {
            var totalValue = 0
            var beeperValue = 1
            for (x in 9 downTo 2) {
                if (karel.beeperAt(x, y)) {
                    drawNumber(x, y, beeperValue, 0x000000)
                    totalValue += beeperValue
                }
                beeperValue = beeperValue.shl(1)
            }
            drawNumber(0, y, totalValue, 0x008000)
        }
    }

    private fun Graphics.drawNumber(x: Int, y: Int, value: Int, color: Int) {
        val str = "%3d".format(value)
        val width = str.length * Front.font.width
        val leftPad = (tileSize - width).shr(1)
        Front.font.drawString(this, x * tileSize + leftPad, y * tileSize, str, color)
    }

    private fun listenToMouse() {
        onMouseClicked { event ->
            if (SwingUtilities.isLeftMouseButton(event)) {
                toggleBeeper(event)
            } else if (SwingUtilities.isRightMouseButton(event)) {
                switchTileSize()
            }
        }
        repaint()
    }

    private fun toggleBeeper(event: MouseEvent) {
        val x = event.x / tileSize
        val y = event.y / tileSize
        atomicKarel.set(atomicKarel.get().toggleBeeper(x, y))
    }

    private fun switchTileSize() {
        tileSize = TILE_SIZE_MEDIUM + TILE_SIZE_LARGE - tileSize
        initialize()
        revalidate()
    }

    init {
        initialize()
        listenToMouse()
    }
}
