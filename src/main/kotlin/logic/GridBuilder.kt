package logic

val fenced = GridBuilder().east(10).north(10).west(10).south(10)::copy
val binary = GridBuilder().spawn(1, 0).east(10).north(10).west(10).south(10)::copy

class GridBuilder(val grid: Grid = Grid(GRID_WIDTH * GRID_HEIGHT)) {

    fun copy(): GridBuilder = GridBuilder(grid.clone())

    private var position = WALL_BOTTOM_LEFT
    private var cellDistance = NORTH + WEST

    fun spawn(x: Int, y: Int): GridBuilder {
        position = wall(x, y)
        return this
    }

    private fun walk(direction: Byte, times: Int): GridBuilder {
        repeat(times) {
            position += direction
            grid[position] = true
            position += direction
        }
        return this
    }

    fun east(times: Int): GridBuilder = walk(EAST, times)

    fun north(times: Int): GridBuilder = walk(NORTH, times)

    fun west(times: Int): GridBuilder = walk(WEST, times)

    fun south(times: Int): GridBuilder = walk(SOUTH, times)

    private fun cellOrientation(distance: Int): GridBuilder {
        cellDistance = distance
        return this
    }

    fun cellNorthEast(): GridBuilder = cellOrientation(EAST + NORTH)

    fun cellNorthWest(): GridBuilder = cellOrientation(NORTH + WEST)

    fun cellSouthWest(): GridBuilder = cellOrientation(WEST + SOUTH)

    fun cellSouthEast(): GridBuilder = cellOrientation(SOUTH + EAST)

    fun drop(): GridBuilder {
        grid[position + cellDistance] = true
        return this
    }

    fun drop(x: Int, y: Int): GridBuilder {
        grid[cell(x, y)] = true
        return this
    }

    inline fun drop(x1: Int, y1: Int, x2: Int, y2: Int, shouldDrop: (Int, Int) -> Boolean): GridBuilder {
        for (y in y1..y2) {
            for (x in x1..x2) {
                grid[cell(x, y)] = shouldDrop(x, y)
            }
        }
        return this
    }

    inline fun drop(x1: Int, y1: Int, x2: Int, y2: Int, shouldDrop: () -> Boolean): GridBuilder {
        for (y in y1..y2) {
            for (x in x1..x2) {
                grid[cell(x, y)] = shouldDrop()
            }
        }
        return this
    }

    fun world(): World = World(grid)
}
