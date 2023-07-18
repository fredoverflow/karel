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

fun wall(x: Int, y: Int): Int {
    return WALL_TOP_LEFT + ((y * GRID_WIDTH + x) shl 1)
}
