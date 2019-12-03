package gui

import freditor.Front
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
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp

private const val TILE_SIZE_MEDIUM = 40
private const val TILE_SIZE_LARGE = 64

private fun readTile(size: Int, name: String): BufferedImage {
    return ImageIO.read(WorldPanel::class.java.getResourceAsStream("/tiles/$size/$name.png"))
}

private fun readTile(size: Int, level: String, name: String): BufferedImage {
    if (level.length > 0) {
        val resource = WorldPanel::class.java.getResourceAsStream("/tiles/$size/$level/$name.png")
        if (resource != null) {
            return ImageIO.read(resource)
        }
    }
    return readTile(size, name);
}

private fun BufferedImage.rotatedCounterclockwise(): BufferedImage {
    val tx = AffineTransform()
    tx.rotate(Math.toRadians(-90.0), width / 2.0, height / 2.0)
    return AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR).filter(this, null)
}

private fun BufferedImage.mirrorImage(): BufferedImage {
    val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
    tx.translate(- width.toDouble(), 0.0);
    return AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(this, null)
}


class WorldPanel(private val atomicWorld: AtomicReference<World>) : JPanel() {

    private var tileSize: Int = Toolkit.getDefaultToolkit().screenSize.height.let { screenHeight ->
        if (screenHeight < 1000) TILE_SIZE_MEDIUM else TILE_SIZE_LARGE
    }

    private var beeper = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
    private var karels = emptyArray<BufferedImage>()
    private var walls = emptyArray<BufferedImage>()
	
    private var levelPath = ""
	
    fun levelImagePath(path : String ) {
		
        if(path != levelPath) {
            levelPath = path
            initializeBeeper()
            initializeKarels()
        }
    }

    private fun initialize() {
        initializeBeeper()
        initializeKarels()
        initializeWalls()

        val panelSize = Dimension(tileSize * Problem.WIDTH, tileSize * Problem.HEIGHT)
        minimumSize = panelSize
        preferredSize = panelSize
        maximumSize = panelSize
    }
	
    private fun initializeBeeper() {
        beeper = readTile(tileSize, levelPath, "beeper")
    }

    private fun initializeKarels() {
        val east = readTile(tileSize, levelPath,  "karel")
        val north = east.rotatedCounterclockwise()
        val west = east.mirrorImage()
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
        for (y in 0 until binaryLines) {
            var totalValue = 0
            var beeperValue = 1
            for (x in 9 downTo 2) {
                if (world.beeperAt(x, y)) {
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
        val width = str.length * Front.front.width
        val leftPad = (tileSize - width).shr(1)
        Front.front.drawString(this, x * tileSize + leftPad, y * tileSize, str, color)
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
        tileSize = TILE_SIZE_MEDIUM + TILE_SIZE_LARGE - tileSize
        initialize()
        revalidate()
    }

    init {
        initialize()
        listenToMouse()
    }
}
