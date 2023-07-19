package logic

val fenced = GridBuilder().east(10).north(10).west(10).south(10)::copy
val binary = GridBuilder().spawn(1, 0).east(10).north(10).west(10).south(10)::copy

class GridBuilder(private val grid: Grid = Grid(GRID_WIDTH * GRID_HEIGHT)) {

    fun copy(): GridBuilder = GridBuilder(grid.clone())

    private var position = WALL_BOTTOM_LEFT
    private var cellDistance = EAST + NORTH

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

    fun world(): World = World(grid)
}
