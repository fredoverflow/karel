package logic

private const val CHARTED = '#'
private const val WALL = '_'
private const val FREE = ' '

// The number on an uncharted cell denotes its uncharted neighbours
private val labyrinth = """
# # # # # # # # # # # #
_ _ _ _ _ _ _ _ _ _ _ _
#_2_3_3_3_3_3_3_3_3_2_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_3_4_4_4_4_4_4_4_4_3_#
_ _ _ _ _ _ _ _ _ _ _ _
#_2_3_3_3_3_3_3_3_3_2_#
_ _ _ _ _ _ _ _ _ _ _ _
# # # # # # # # # # # #
""".toCharArray()

private const val EAST = 1
private const val NORTH = -24
private const val WEST = -1
private const val SOUTH = 24

private const val NEIGHBOUR_X = 2 * EAST
private const val NEIGHBOUR_Y = 2 * SOUTH
private const val ORIGIN = 1 + NEIGHBOUR_Y + NEIGHBOUR_X

private val turnLeft = IntArray(256).apply {
    this[EAST and 255] = NORTH
    this[NORTH and 255] = WEST
    this[WEST and 255] = SOUTH
    this[SOUTH and 255] = EAST
}

private val turnRight = IntArray(256).apply {
    this[EAST and 255] = SOUTH
    this[SOUTH and 255] = WEST
    this[WEST and 255] = NORTH
    this[NORTH and 255] = EAST
}

private val permutationsOfDirections = intArrayOf(
    0x01e8ff18,         // EAST, NORTH, WEST, SOUTH,
    0x01e818ff,         // EAST, NORTH, SOUTH, WEST,
    0x01ffe818,         // EAST, WEST, NORTH, SOUTH,
    0x01ff18e8,         // EAST, WEST, SOUTH, NORTH,
    0x0118e8ff,         // EAST, SOUTH, NORTH, WEST,
    0x0118ffe8,         // EAST, SOUTH, WEST, NORTH,

    0xe801ff18.toInt(), // NORTH, EAST, WEST, SOUTH,
    0xe80118ff.toInt(), // NORTH, EAST, SOUTH, WEST,
    0xe8ff0118.toInt(), // NORTH, WEST, EAST, SOUTH,
    0xe8ff1801.toInt(), // NORTH, WEST, SOUTH, EAST,
    0xe81801ff.toInt(), // NORTH, SOUTH, EAST, WEST,
    0xe818ff01.toInt(), // NORTH, SOUTH, WEST, EAST,

    0xff01e818.toInt(), // WEST, EAST, NORTH, SOUTH,
    0xff0118e8.toInt(), // WEST, EAST, SOUTH, NORTH,
    0xffe80118.toInt(), // WEST, NORTH, EAST, SOUTH,
    0xffe81801.toInt(), // WEST, NORTH, SOUTH, EAST,
    0xff1801e8.toInt(), // WEST, SOUTH, EAST, NORTH,
    0xff18e801.toInt(), // WEST, SOUTH, NORTH, EAST,

    0x1801e8ff,         // SOUTH, EAST, NORTH, WEST,
    0x1801ffe8,         // SOUTH, EAST, WEST, NORTH,
    0x18e801ff,         // SOUTH, NORTH, EAST, WEST,
    0x18e8ff01,         // SOUTH, NORTH, WEST, EAST,
    0x18ff01e8,         // SOUTH, WEST, EAST, NORTH,
    0x18ffe801,         // SOUTH, WEST, NORTH, EAST,
)

private inline fun forEachDirection(block: (Int) -> Unit) {
    val directions = permutationsOfDirections[kotlin.random.Random.nextInt(24)]

    block(directions shr 24)
    block(directions shl 8 shr 24)
    block(directions shl 16 shr 24)
    block(directions shl 24 shr 24)
}

private const val BACKTRACK = 0
private const val BACKTRACK_BUDGET_EXHAUSTED = -1

object LabyrinthGenerator {
    private var labyrinth = logic.labyrinth
    private var backtrackBudget = 0

    fun generateLabyrinth(): World {
        var destination: Int
        do {
            labyrinth = logic.labyrinth.clone()
            backtrackBudget = 1000
            destination = destinationOpen(ORIGIN, EAST, 99)
        } while (destination == BACKTRACK_BUDGET_EXHAUSTED)

        val walls = ByteArray(100)
        var i = 0
        var position = ORIGIN

        for (y in 0 until 10) {
            for (x in 0 until 10) {

                val east = labyrinth[position + EAST].code and 1
                val north = labyrinth[position + NORTH].code and 2
                val west = labyrinth[position + WEST].code and 4
                val south = labyrinth[position + SOUTH].code and 8

                walls[i++] = (south).or(west).or(north).or(east).toByte()

                position += NEIGHBOUR_X
            }
            position += NEIGHBOUR_Y - 10 * NEIGHBOUR_X
        }

        val y = destination / NEIGHBOUR_Y
        val x = destination % NEIGHBOUR_Y / NEIGHBOUR_X

        return FloorPlan(walls).world().apply {
            dropBeeper(x - 1, y - 1)
        }
    }

    private fun isUncharted(position: Int): Boolean {
        return labyrinth[position] >= '0'
    }

    private fun causesPartition(position: Int, direction: Int): Boolean {
        return !isUncharted(position + 2 * direction) &&
                isUncharted(position + 2 * turnLeft[direction and 255]) &&
                isUncharted(position + 2 * turnRight[direction and 255])
    }

    private fun destinationOpen(position: Int, direction: Int, uncharted: Int): Int {
        if (causesPartition(position, direction)) return BACKTRACK

        val unchartedNeighbours = labyrinth[position]
        labyrinth[position] = CHARTED

        var potentialDeadEnds = 0
        // A potential dead end is a neighbour that will turn into a dead end
        // unless we tear down the wall and pick it as our next position
        if (--labyrinth[position + NEIGHBOUR_X] == '1') ++potentialDeadEnds
        if (--labyrinth[position - NEIGHBOUR_Y] == '1') ++potentialDeadEnds
        if (--labyrinth[position - NEIGHBOUR_X] == '1') ++potentialDeadEnds
        if (--labyrinth[position + NEIGHBOUR_Y] == '1') ++potentialDeadEnds

        if (potentialDeadEnds == 0) {
            // We can roam freely without consequence
            forEachDirection { dir ->
                val neighbour = position + 2 * dir
                if (isUncharted(neighbour)) {
                    val wall = position + dir
                    labyrinth[wall] = FREE
                    destinationOpen(neighbour, dir, uncharted - 1).let { if (it != BACKTRACK) return it }
                    labyrinth[wall] = WALL
                }
            }
        } else if (potentialDeadEnds == 2) {
            // We must eliminate one of the potential dead ends by picking it
            forEachDirection { dir ->
                val neighbour = position + 2 * dir
                if (labyrinth[neighbour] == '1') {
                    val wall = position + dir
                    labyrinth[wall] = FREE
                    destinationFound(neighbour, dir, uncharted - 1).let { if (it != BACKTRACK) return it }
                    labyrinth[wall] = WALL
                }
            }
        } else if (potentialDeadEnds == 1) {
            // We can either eliminate the potential dead end by picking it,
            // or turn it into an actual dead end by picking another neighbour
            forEachDirection { dir ->
                val neighbour = position + 2 * dir
                if (isUncharted(neighbour)) {
                    val wall = position + dir
                    labyrinth[wall] = FREE
                    if (labyrinth[neighbour] == '1') {
                        destinationOpen(neighbour, dir, uncharted - 1).let { if (it != BACKTRACK) return it }
                    } else {
                        destinationFound(neighbour, dir, uncharted - 1).let { if (it != BACKTRACK) return it }
                    }
                    labyrinth[wall] = WALL
                }
            }
        }

        if (--backtrackBudget < 0) return BACKTRACK_BUDGET_EXHAUSTED

        labyrinth[position] = unchartedNeighbours
        labyrinth[position + NEIGHBOUR_X]++
        labyrinth[position - NEIGHBOUR_Y]++
        labyrinth[position - NEIGHBOUR_X]++
        labyrinth[position + NEIGHBOUR_Y]++

        return BACKTRACK
    }

    private fun destinationFound(position: Int, direction: Int, uncharted: Int): Int {
        if (causesPartition(position, direction)) return BACKTRACK
        if (uncharted == 0) return position

        val unchartedNeighbours = labyrinth[position]
        labyrinth[position] = CHARTED

        var potentialDeadEnds = 0
        // A potential dead end is a neighbour that will turn into a dead end
        // unless we tear down the wall and pick it as our next position
        if (--labyrinth[position + NEIGHBOUR_X] == '1') ++potentialDeadEnds
        if (--labyrinth[position - NEIGHBOUR_Y] == '1') ++potentialDeadEnds
        if (--labyrinth[position - NEIGHBOUR_X] == '1') ++potentialDeadEnds
        if (--labyrinth[position + NEIGHBOUR_Y] == '1') ++potentialDeadEnds

        if (potentialDeadEnds == 0) {
            // We can roam freely without consequence
            forEachDirection { dir ->
                val neighbour = position + 2 * dir
                if (isUncharted(neighbour)) {
                    val wall = position + dir
                    labyrinth[wall] = FREE
                    destinationFound(neighbour, dir, uncharted - 1).let { if (it != BACKTRACK) return it }
                    labyrinth[wall] = WALL
                }
            }
        } else if (potentialDeadEnds == 1) {
            // We must eliminate the potential dead end by picking it,
            // because we already found our destination earlier
            forEachDirection { dir ->
                val neighbour = position + 2 * dir
                if (labyrinth[neighbour] == '1') {
                    val wall = position + dir
                    labyrinth[wall] = FREE
                    destinationFound(neighbour, dir, uncharted - 1).let { if (it != BACKTRACK) return it }
                    labyrinth[wall] = WALL
                }
            }
        }

        if (--backtrackBudget < 0) return BACKTRACK_BUDGET_EXHAUSTED

        labyrinth[position] = unchartedNeighbours
        labyrinth[position + NEIGHBOUR_X]++
        labyrinth[position - NEIGHBOUR_Y]++
        labyrinth[position - NEIGHBOUR_X]++
        labyrinth[position + NEIGHBOUR_Y]++

        return BACKTRACK
    }
}
