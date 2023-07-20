package logic

class World(
    private var grid: Grid,
    private var position: Int,
    private var front: Byte, private var left: Byte, private var back: Byte, private var right: Byte
) {
    fun copy(): World {
        return World(grid.clone(), position, front, left, back, right)
    }

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

    fun countBeepers(): Int {
        var count = 0
        var index = CELL_TOP_LEFT
        for (y in 0 until 10) {
            for (x in 0 until 10) {
                if (grid[index]) {
                    ++count
                }
                index += CELL_NEXT_COLUMN
            }
            index += CELL_NEXT_ROW
        }
        return count
    }
}
