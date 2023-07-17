package logic

/*
  0 ······················
 22 ··=·=·=·=·=·=·=·=·=·=·
 44 ·|·|·|·|·|·|·|·|·|·|·|
 66 ··=·=·=·=·=·=·=·=·=·=·
 88 ·|·|·|·|·|·|·|·|·|·|·|
110 ··=·=·=·=·=·=·=·=·=·=·
132 ·|·|·|·|·|·|·|·|·|·|·|
154 ··=·=·=·=·=·=·=·=·=·=·
176 ·|·|·|·|·|·|·|·|·|·|·|
198 ··=·=·=·=·=·=·=·=·=·=·
220 ·|·|·|·|·|·|·|·|·|·|·|
242 ··=·=·=·=·=·=·=·=·=·=·
264 ·|·|·|·|·|·|·|·|·|·|·|
286 ··=·=·=·=·=·=·=·=·=·=·
308 ·|·|·|·|·|·|·|·|·|·|·|
330 ··=·=·=·=·=·=·=·=·=·=·
352 ·|·|·|·|·|·|·|·|·|·|·|
374 ··=·=·=·=·=·=·=·=·=·=·
396 ·|·|·|·|·|·|·|·|·|·|·|
418 ··=·=·=·=·=·=·=·=·=·=·
440 ·|·|·|·|·|·|·|·|·|·|·|
462 ··=·=·=·=·=·=·=·=·=·=·
484 ······················
*/

const val GRID_WIDTH = 22
const val GRID_HEIGHT = 23

const val WALL_TOP_LEFT = 23
const val WALL_TOP_RIGHT = 43
const val WALL_BOTTOM_LEFT = 463
const val WALL_BOTTOM_RIGHT = 483

const val CELL_TOP_LEFT = 46
const val CELL_TOP_RIGHT = 64
const val CELL_BOTTOM_LEFT = 442
const val CELL_BOTTOM_RIGHT = 460

const val EAST: Byte = +1
const val NORTH: Byte = -22
const val WEST: Byte = -1
const val SOUTH: Byte = +22

fun cell(x: Int, y: Int): Int {
    return CELL_TOP_LEFT + ((y * GRID_WIDTH + x) shl 1)
}

class World(grid: BooleanArray, x: Int, y: Int) {

    private var grid = grid.clone()

    private var position = cell(x, y)

    private var front = EAST
    private var left = NORTH
    private var back = WEST
    private var right = SOUTH

    operator fun get(position: Int): Boolean {
        return grid[position]
    }

    operator fun get(x: Int, y: Int): Boolean {
        return grid[cell(x, y)]
    }

    val x: Int
        get() = (position ushr 1) % 11 - 1

    val y: Int
        get() = (position ushr 1) / 11 - 1

    val direction: Int
        get() = when (front) {
            EAST -> 0
            NORTH -> 1
            WEST -> 2
            SOUTH -> 3

            else -> throw AssertionError(String.format("illegal front %08x", front))
        }

    fun moveForward() {
        if (grid[position + front]) throw BlockedByWall()
        position += 2 * front
    }

    fun turnLeft() {
        val temp = front
        front = left
        left = back
        back = right
        right = temp
    }

    fun turnAround() {
        var temp = front
        front = back
        back = temp

        temp = left
        left = right
        right = temp
    }

    fun turnRight() {
        val temp = front
        front = right
        right = back
        back = left
        left = temp
    }

    fun pickBeeper() {
        if (grid[position].not()) throw CellIsEmpty()
        grid[position] = false
    }

    fun dropBeeper() {
        if (grid[position]) throw CellIsFull()
        grid[position] = true
    }

    fun toggleBeeper(x: Int, y: Int) {
        val position = cell(x, y)
        grid[position] = grid[position].not()
    }

    fun onBeeper(): Boolean {
        return grid[position]
    }

    fun beeperAhead(): Boolean {
        return grid[position + 2 * front]
    }

    fun leftIsClear(): Boolean {
        return grid[position + left].not()
    }

    fun frontIsClear(): Boolean {
        return grid[position + front].not()
    }

    fun rightIsClear(): Boolean {
        return grid[position + right].not()
    }
}

fun emptyGrid(): BooleanArray {
    return BooleanArray(GRID_WIDTH * GRID_HEIGHT)
}

private val PLOT_WALLS = """([>^<v])(\d+)""".toRegex()

fun BooleanArray.plotWalls(origin: Int, program: String): BooleanArray {
    var position = origin
    for (matchResult in PLOT_WALLS.findAll(program)) {
        val (direction, count) = matchResult.destructured
        val wall = when (direction) {
            ">" -> EAST
            "^" -> NORTH
            "<" -> WEST
            "v" -> SOUTH

            else -> throw AssertionError("illegal direction $direction")
        }
        repeat(count.toInt()) {
            position += wall
            this[position] = true
            position += wall
        }
    }
    return this
}

fun BooleanArray.dropBeeper(x: Int, y: Int): BooleanArray {
    this[cell(x, y)] = true
    return this
}

fun BooleanArray.dropBeepers(vararg xy: Int): BooleanArray {
    for (i in xy.indices step 2) {
        dropBeeper(xy[i], xy[i + 1])
    }
    return this
}

fun freeRoamingGrid(): BooleanArray {
    return emptyGrid().plotWalls(WALL_TOP_LEFT, ">10 v10 <10 ^10")
}

fun main() {
    freeRoamingGrid().map { if (it) 1 else 0 }.chunked(22).forEach(::println)
}
