package logic

import common.toInt

class World(
    private var grid: Grid,
    private var position: Int,
    private var front: Byte, private var left: Byte, private var back: Byte, private var right: Byte
) {
    fun copy(): World {
        return World(grid.clone(), position, front, left, back, right)
    }

    fun gridEquals(that: World): Boolean {
        return this.grid.contentEquals(that.grid)
    }

    fun positionAndGridEquals(that: World): Boolean {
        return this.position == that.position && this.grid.contentEquals(that.grid)
    }

    operator fun get(position: Int): Boolean {
        return grid[position]
    }

    operator fun get(x: Int, y: Int): Boolean {
        return grid[cell(x, y)]
    }

    val pos: Int
        get() = position

    val x: Int
        get() = ((position % SOUTH) ushr 1) - 1

    val y: Int
        get() = ((position / SOUTH) ushr 1) - 1

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

    fun countBeepers(): Int {
        return countBeepersInColumn(0) +
                countBeepersInColumn(1) +
                countBeepersInColumn(2) +
                countBeepersInColumn(3) +
                countBeepersInColumn(4) +
                countBeepersInColumn(5) +
                countBeepersInColumn(6) +
                countBeepersInColumn(7) +
                countBeepersInColumn(8) +
                countBeepersInColumn(9)
    }

    fun countBeepersInColumn(x: Int): Int {
        val cell = cell(x, 0)
        return grid[cell + 0 * SOUTH].toInt() +
                grid[cell + 2 * SOUTH].toInt() +
                grid[cell + 4 * SOUTH].toInt() +
                grid[cell + 6 * SOUTH].toInt() +
                grid[cell + 8 * SOUTH].toInt() +
                grid[cell + 10 * SOUTH].toInt() +
                grid[cell + 12 * SOUTH].toInt() +
                grid[cell + 14 * SOUTH].toInt() +
                grid[cell + 16 * SOUTH].toInt() +
                grid[cell + 18 * SOUTH].toInt()
    }

    fun binaryNumber(y: Int): Int {
        val right = cell(9, y)
        return grid[right - 0].toInt(1) +
                grid[right - 2].toInt(2) +
                grid[right - 4].toInt(4) +
                grid[right - 6].toInt(8) +
                grid[right - 8].toInt(16) +
                grid[right - 10].toInt(32) +
                grid[right - 12].toInt(64) +
                grid[right - 14].toInt(128)
    }
}
