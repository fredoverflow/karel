package logic

open class FloorPlan(protected val walls: LongArray) {
    init {
        assert(walls.size == 10)
    }

    // Each cell takes up 4 bits (for the 4 walls), hence the shift by 2.
    // Internally, the world is flipped horizontally, hence the sub from 9.
    // (We are using hex literals to write down some simple worlds,
    // and those start with the most significant digit on the left.)
    protected fun shift(x: Int): Int {
        return (9 - x).shl(2)
    }

    fun isClear(x: Int, y: Int, direction: Int): Boolean {
        return wallsAt(x, y).and(1.shl(direction)) == WALL_NONE
    }

    fun wallsAt(x: Int, y: Int): Int {
        return walls[y].ushr(shift(x)).toInt().and(WALL_ALL)
    }

    fun numberOfWallsAt(x: Int, y: Int): Int {
        val shift = wallsAt(x, y).shl(2)
        return 0x4332_3221_3221_2110L.ushr(shift).toInt().and(7)
    }

    fun builder(): FloorBuilder {
        return FloorBuilder(walls.clone())
    }

    fun world(): World {
        return World(0, 0, this)
    }

    companion object {
        const val WALL_NONE = 0

        const val WALL_EAST = 1
        const val WALL_NORTH = 2
        const val WALL_WEST = 4
        const val WALL_SOUTH = 8

        const val WALL_ALL = 15

        operator fun invoke(vararg walls: Long): FloorPlan {
            return FloorPlan(walls)
        }

        val empty = FloorPlan(
                0x6222222223L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0xc888888889L)

        val first = FloorPlan(
                0x0000000000L,
                0x0000000000L,
                0x0000000000L,
                0x0000000000L,
                0x0000000000L,
                0x0000000000L,
                0x0000000000L,
                0x6222300000L,
                0x4008900000L,
                0xc890000000L)

        val holes = FloorPlan(
                0x6222222223L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0xc000000001L,
                0x3c08080809L,
                0x03d7d7d7d6L)

        val stairs = FloorPlan(
                0x6222222223L,
                0x4000000001L,
                0x4000000001L,
                0x4000000801L,
                0x4000009741L,
                0x4000096141L,
                0x4000960141L,
                0x4009600141L,
                0x4096000141L,
                0xc9e88889c9L)

        val mountain = FloorPlan(
                0x6222222223L,
                0x4000080001L,
                0x4000174001L,
                0x400095c001L,
                0x4001603401L,
                0x4009401c01L,
                0x4016000341L,
                0x40940001c1L,
                0x4160000035L,
                0xc9c888889dL)

        val maze = FloorPlan(
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL,
                0xffffffffffL)

        val trap = FloorPlan(
                0x6222222223L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0x4000000001L,
                0xeaaaaaaaabL)
    }
}

class FloorBuilder(walls: LongArray) : FloorPlan(walls) {

    // calculate the appropriate bitmask to access and update walls
    private fun bitmask(x: Int, directions: Int): Long {
        return directions.toLong().shl(shift(x))
    }

    fun buildHorizontalWall(x: Int, y: Int): FloorBuilder {
        if (y < Problem.HEIGHT) {
            walls[y] = walls[y].or(bitmask(x, WALL_NORTH))
        }
        if (y > 0) {
            walls[y - 1] = walls[y - 1].or(bitmask(x, WALL_SOUTH))
        }
        return this
    }

    fun buildVerticalWall(x: Int, y: Int): FloorBuilder {
        if (x < Problem.WIDTH) {
            walls[y] = walls[y].or(bitmask(x, WALL_WEST))
        }
        if (x > 0) {
            walls[y] = walls[y].or(bitmask(x - 1, WALL_EAST))
        }
        return this
    }

    fun tearDownWall(x: Int, y: Int, direction: Int): FloorBuilder {
        walls[y] = walls[y].and(bitmask(x, 1.shl(direction)).inv())
        return this
    }
}
