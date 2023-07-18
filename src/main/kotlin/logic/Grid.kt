package logic

typealias Grid = BooleanArray

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
const val CELL_NEXT_COLUMN = 2
const val CELL_NEXT_ROW = 24

const val EAST: Byte = +1
const val NORTH: Byte = -22
const val WEST: Byte = -1
const val SOUTH: Byte = +22

fun cell(x: Int, y: Int): Int {
    return CELL_TOP_LEFT + ((y * GRID_WIDTH + x) shl 1)
}

fun newGrid(): Grid {
    return Grid(GRID_WIDTH * GRID_HEIGHT)
}

private val PLOT_WALLS = """([>^<v])(\d+)""".toRegex()

fun Grid.plotWalls(origin: Int, program: String): Grid {
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

fun Grid.dropBeeper(x: Int, y: Int): Grid {
    this[cell(x, y)] = true
    return this
}

fun Grid.dropBeepers(vararg xy: Int): Grid {
    for (i in xy.indices step 2) {
        dropBeeper(xy[i], xy[i + 1])
    }
    return this
}

val fencedGrid: () -> Grid = newGrid().plotWalls(WALL_TOP_LEFT, ">10 v10 <10 ^10")::clone

val binaryGrid: () -> Grid = newGrid().plotWalls(WALL_TOP_RIGHT, "v10 <9 ^10 >9")::clone
