package logic

@JvmInline
value class FloorPlan(private val walls: LongArray) {
    init {
        assert(walls.size == 10)
    }

    fun isClear(x: Int, y: Int, direction: Int): Boolean {
        return walls[y].ushr(4 * x + direction).and(1L) == 0L
    }

    fun wallsAt(x: Int, y: Int): Int {
        return walls[y].ushr(4 * x).toInt().and(WALL_ALL)
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

        operator fun invoke(vararg ws: Long): FloorPlan {
            for (i in ws.indices) {
                // flip hex literal
                val w = ws[i]
                ws[i] = w.shr(0x24).and(0x000000000f) or
                        w.shr(0x1c).and(0x00000000f0) or
                        w.shr(0x14).and(0x0000000f00) or
                        w.shr(0x0c).and(0x000000f000) or
                        w.shr(0x04).and(0x00000f0000) or
                        w.shl(0x04).and(0x0000f00000) or
                        w.shl(0x0c).and(0x000f000000) or
                        w.shl(0x14).and(0x00f0000000) or
                        w.shl(0x1c).and(0x0f00000000) or
                        w.shl(0x24).and(0xf000000000)
            }
            return FloorPlan(ws)
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
            0xc888888889L,
        )

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
            0xc890000000L,
        )

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
            0x03d7d7d7d6L,
        )

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
            0xc9e88889c9L,
        )

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
            0xc9c888889dL,
        )

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
            0xffffffffffL,
        )

        val binary = FloorPlan(
            0x0622222223L,
            0x0400000001L,
            0x0400000001L,
            0x0400000001L,
            0x0400000001L,
            0x0400000001L,
            0x0400000001L,
            0x0400000001L,
            0x0400000001L,
            0x0c88888889L,
        )

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
            0xeaaaaaaaabL,
        )
    }
}

@JvmInline
value class FloorBuilder(private val walls: LongArray) {

    // calculate the appropriate bitmask to access and update walls
    private fun bitmask(x: Int, directions: Int): Long {
        return directions.toLong().shl(4 * x)
    }

    fun buildHorizontalWall(x: Int, y: Int): FloorBuilder {
        if (y < Problem.HEIGHT) {
            walls[y] = walls[y].or(bitmask(x, FloorPlan.WALL_NORTH))
        }
        if (y > 0) {
            walls[y - 1] = walls[y - 1].or(bitmask(x, FloorPlan.WALL_SOUTH))
        }
        return this
    }

    fun buildVerticalWall(x: Int, y: Int): FloorBuilder {
        if (x < Problem.WIDTH) {
            walls[y] = walls[y].or(bitmask(x, FloorPlan.WALL_WEST))
        }
        if (x > 0) {
            walls[y] = walls[y].or(bitmask(x - 1, FloorPlan.WALL_EAST))
        }
        return this
    }

    fun tearDownWall(x: Int, y: Int, direction: Int): FloorBuilder {
        walls[y] = walls[y].and(bitmask(x, 1.shl(direction)).inv())
        return this
    }

    fun world(): World {
        return FloorPlan(walls).world()
    }
}
