package logic

val fenced = WorldBuilder().east(10).north(10).west(10).south(10)::copy
val binary = WorldBuilder().spawn(1, 0).east(10).north(10).west(10).south(10)::copy

class WorldBuilder(private val grid: Grid = Grid(GRID_WIDTH * GRID_HEIGHT)) {

    fun copy(): WorldBuilder = WorldBuilder(grid.clone())

    private var position = WALL_BOTTOM_LEFT
    private var cellDistance = EAST + NORTH

    fun spawn(x: Int, y: Int): WorldBuilder {
        position = wall(x, y)
        return this
    }

    private fun walk(direction: Byte, times: Int): WorldBuilder {
        repeat(times) {
            position += direction
            grid[position] = true
            position += direction
        }
        return this
    }

    fun east(times: Int): WorldBuilder = walk(EAST, times)

    fun north(times: Int): WorldBuilder = walk(NORTH, times)

    fun west(times: Int): WorldBuilder = walk(WEST, times)

    fun south(times: Int): WorldBuilder = walk(SOUTH, times)

    private fun cellOrientation(distance: Int): WorldBuilder {
        cellDistance = distance
        return this
    }

    fun drop(x: Int, y: Int): WorldBuilder {
        grid[cell(x, y)] = true
        return this
    }

    fun cellNorthEast(): WorldBuilder = cellOrientation(EAST + NORTH)

    fun cellNorthWest(): WorldBuilder = cellOrientation(NORTH + WEST)

    fun cellSouthWest(): WorldBuilder = cellOrientation(WEST + SOUTH)

    fun cellSouthEast(): WorldBuilder = cellOrientation(SOUTH + EAST)

    fun drop(): WorldBuilder {
        grid[position + cellDistance] = true
        return this
    }

    fun toWorld(): World = World(grid)
}
