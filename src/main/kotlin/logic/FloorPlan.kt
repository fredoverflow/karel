package logic

@JvmInline
value class FloorPlan(private val walls: ByteArray) {

    fun isClear(position: Int, direction: Int): Boolean {
        return walls[position].toInt().and(1 shl direction) == 0
    }

    fun wallsAt(position: Int): Int {
        return walls[position].toInt().and(WALL_ALL)
    }

    fun numberOfWallsAt(position: Int): Int {
        val shift = wallsAt(position).shl(2)
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

        operator fun invoke(vararg walls: Byte): FloorPlan {
            require(walls.size == 100) { walls.size }
            return FloorPlan(walls)
        }

        private const val A: Byte = 10
        private const val B: Byte = 11
        private const val C: Byte = 12
        private const val D: Byte = 13
        private const val E: Byte = 14
        private const val F: Byte = 15

        val empty = FloorPlan(
            6, 2, 2, 2, 2, 2, 2, 2, 2, 3,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            C, 8, 8, 8, 8, 8, 8, 8, 8, 9,
        )

        val first = FloorPlan(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            6, 2, 2, 2, 3, 0, 0, 0, 0, 0,
            4, 0, 0, 8, 9, 0, 0, 0, 0, 0,
            C, 8, 9, 0, 0, 0, 0, 0, 0, 0,
        )

        val holes = FloorPlan(
            6, 2, 2, 2, 2, 2, 2, 2, 2, 3,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            C, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            3, C, 0, 8, 0, 8, 0, 8, 0, 9,
            0, 3, D, 7, D, 7, D, 7, D, 6,
        )

        val stairs = FloorPlan(
            6, 2, 2, 2, 2, 2, 2, 2, 2, 3,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 8, 0, 1,
            4, 0, 0, 0, 0, 0, 9, 7, 4, 1,
            4, 0, 0, 0, 0, 9, 6, 1, 4, 1,
            4, 0, 0, 0, 9, 6, 0, 1, 4, 1,
            4, 0, 0, 9, 6, 0, 0, 1, 4, 1,
            4, 0, 9, 6, 0, 0, 0, 1, 4, 1,
            C, 9, E, 8, 8, 8, 8, 9, C, 9,
        )

        val mountain = FloorPlan(
            6, 2, 2, 2, 2, 2, 2, 2, 2, 3,
            4, 0, 0, 0, 0, 8, 0, 0, 0, 1,
            4, 0, 0, 0, 1, 7, 4, 0, 0, 1,
            4, 0, 0, 0, 9, 5, C, 0, 0, 1,
            4, 0, 0, 1, 6, 0, 3, 4, 0, 1,
            4, 0, 0, 9, 4, 0, 1, C, 0, 1,
            4, 0, 1, 6, 0, 0, 0, 3, 4, 1,
            4, 0, 9, 4, 0, 0, 0, 1, C, 1,
            4, 1, 6, 0, 0, 0, 0, 0, 3, 5,
            C, 9, C, 8, 8, 8, 8, 8, 9, D,
        )

        val maze = FloorPlan(
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F,
        )

        val binary = FloorPlan(
            0, 6, 2, 2, 2, 2, 2, 2, 2, 3,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 4, 0, 0, 0, 0, 0, 0, 0, 1,
            0, C, 8, 8, 8, 8, 8, 8, 8, 9,
        )

        val trap = FloorPlan(
            6, 2, 2, 2, 2, 2, 2, 2, 2, 3,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            4, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            E, A, A, A, A, A, A, A, A, B,
        )
    }
}

@JvmInline
value class FloorBuilder(private val walls: ByteArray) {

    fun buildHorizontalWall(x: Int, y: Int): FloorBuilder {
        var position = y * 10 + x
        if (position < 100) {
            walls[position] = walls[position].toInt().or(FloorPlan.WALL_NORTH).toByte()
        }
        position -= 10
        if (position >= 0) {
            walls[position] = walls[position].toInt().or(FloorPlan.WALL_SOUTH).toByte()
        }
        return this
    }

    fun buildVerticalWall(x: Int, y: Int): FloorBuilder {
        val position = y * 10 + x
        if (x < 10) {
            walls[position] = walls[position].toInt().or(FloorPlan.WALL_WEST).toByte()
        }
        if (x > 0) {
            walls[position - 1] = walls[position - 1].toInt().or(FloorPlan.WALL_EAST).toByte()
        }
        return this
    }

    fun tearDownWall(position: Int, direction: Int): FloorBuilder {
        walls[position] = walls[position].toInt().and(1.shl(direction).inv()).toByte()
        return this
    }

    fun world(): World {
        return FloorPlan(walls).world()
    }
}
