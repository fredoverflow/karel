package logic

val fenced = WorldBuilder()
    .east(10).north(10).west(10).south(10)::copy

val binary = WorldBuilder().spawn(1, 10)
    .east(9).north(10).west(9).south(10)::copy

class WorldBuilder(val grid: Grid = Grid(GRID_WIDTH * GRID_HEIGHT)) {

    fun copy(): WorldBuilder = WorldBuilder(grid.clone())

    private var position = WALL_BOTTOM_LEFT
    private var cellDistance = NORTH + WEST

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

    fun cellNorthEast(): WorldBuilder = cellOrientation(EAST + NORTH)

    fun cellNorthWest(): WorldBuilder = cellOrientation(NORTH + WEST)

    fun cellSouthWest(): WorldBuilder = cellOrientation(WEST + SOUTH)

    fun cellSouthEast(): WorldBuilder = cellOrientation(SOUTH + EAST)

    fun drop(): WorldBuilder {
        grid[position + cellDistance] = true
        return this
    }

    fun drop(x: Int, y: Int): WorldBuilder {
        grid[cell(x, y)] = true
        return this
    }

    inline fun drop(x1: Int, y1: Int, x2: Int, y2: Int, shouldDrop: () -> Boolean): WorldBuilder {
        var cell = CELL_TOP_LEFT
        for (y in y1..y2) {
            for (x in x1..x2) {
                grid[cell] = shouldDrop()
                cell += CELL_NEXT_COLUMN
            }
            cell += CELL_NEXT_ROW
        }
        return this
    }

    fun placeKarel() = World(grid, CELL_BOTTOM_LEFT, EAST, NORTH, WEST, SOUTH)

    fun placeKarel(x: Int, y: Int) = World(grid, cell(x, y), EAST, NORTH, WEST, SOUTH)

    fun placeKarel(x: Int, y: Int, direction: Int) = World(grid, cell(x, y),
        delta(direction + 0),
        delta(direction + 1),
        delta(direction + 2),
        delta(direction + 3))
}
