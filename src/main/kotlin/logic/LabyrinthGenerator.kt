package logic

import logic.Problem.Companion.EAST
import logic.Problem.Companion.NORTH
import logic.Problem.Companion.SOUTH
import logic.Problem.Companion.WEST

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

// Sometimes, due to "bad luck" with the random number generator,
// the labyrinth generation algorithm causes a noticeable pause.
// A practical fix is to run the algorithm multiple times in parallel
// and let the quickest execution "win".
private const val PARALLEL_TASKS = 16

fun generateRandomLabyrinth(): World {
    val pool = Executors.newFixedThreadPool(PARALLEL_TASKS)
    val service = ExecutorCompletionService<World>(pool)

    repeat(PARALLEL_TASKS) {
        service.submit(LabyrinthGenerator())
    }

    val firstWorld = service.take().get()
    pool.shutdownNow()
    return firstWorld.withKarelAt(0, 0, EAST)
}

private const val CHARTED = '#'
private const val WALL = '/'
private const val FREE = ' '

private const val SENTINEL = "# # # # # # # # # # # #"
private const val VERTICAL = "/ / / / / / / / / / / /"
// The number on an uncharted cell denotes its uncharted neighbours
private const val EDGE = "#/2/3/3/3/3/3/3/3/3/2/#"
private const val NORM = "#/3/4/4/4/4/4/4/4/4/3/#"

private const val LABYRINTH = SENTINEL +
        VERTICAL + EDGE +
        VERTICAL + NORM +
        VERTICAL + NORM +
        VERTICAL + NORM +
        VERTICAL + NORM +
        VERTICAL + NORM +
        VERTICAL + NORM +
        VERTICAL + NORM +
        VERTICAL + NORM +
        VERTICAL + EDGE +
        VERTICAL + SENTINEL

private const val WALL_X = 1
private const val WALL_Y = 23
private const val NEIGHBOUR_X = 2 * WALL_X
private const val NEIGHBOUR_Y = 2 * WALL_Y

private const val ORIGIN = NEIGHBOUR_Y + NEIGHBOUR_X

private val WALLS = intArrayOf(WALL_X, -WALL_Y, -WALL_X, WALL_Y)
private val NEIGHBOURS = intArrayOf(NEIGHBOUR_X, -NEIGHBOUR_Y, -NEIGHBOUR_X, NEIGHBOUR_Y)

private val directionPermutations: Array<IntArray> = arrayOf(
        intArrayOf(EAST, NORTH, WEST, SOUTH),
        intArrayOf(EAST, NORTH, SOUTH, WEST),
        intArrayOf(EAST, WEST, NORTH, SOUTH),
        intArrayOf(EAST, WEST, SOUTH, NORTH),
        intArrayOf(EAST, SOUTH, NORTH, WEST),
        intArrayOf(EAST, SOUTH, WEST, NORTH),

        intArrayOf(NORTH, EAST, WEST, SOUTH),
        intArrayOf(NORTH, EAST, SOUTH, WEST),
        intArrayOf(NORTH, WEST, EAST, SOUTH),
        intArrayOf(NORTH, WEST, SOUTH, EAST),
        intArrayOf(NORTH, SOUTH, EAST, WEST),
        intArrayOf(NORTH, SOUTH, WEST, EAST),

        intArrayOf(WEST, EAST, NORTH, SOUTH),
        intArrayOf(WEST, EAST, SOUTH, NORTH),
        intArrayOf(WEST, NORTH, EAST, SOUTH),
        intArrayOf(WEST, NORTH, SOUTH, EAST),
        intArrayOf(WEST, SOUTH, EAST, NORTH),
        intArrayOf(WEST, SOUTH, NORTH, EAST),

        intArrayOf(SOUTH, EAST, NORTH, WEST),
        intArrayOf(SOUTH, EAST, WEST, NORTH),
        intArrayOf(SOUTH, NORTH, EAST, WEST),
        intArrayOf(SOUTH, NORTH, WEST, EAST),
        intArrayOf(SOUTH, WEST, EAST, NORTH),
        intArrayOf(SOUTH, WEST, NORTH, EAST)
)

class LabyrinthGenerator : Callable<World> {

    private val randomNumberGenerator = java.util.Random()

    private fun randomDirectionPermutation(): IntArray {
        return directionPermutations[randomNumberGenerator.nextInt(directionPermutations.size)]
    }

    private val labyrinth = LABYRINTH.toCharArray()

    private data class Solution(val destination: Int) : Exception()

    override fun call(): World {
        try {
            destinationOpen(ORIGIN, EAST, 99)
            throw AssertionError("search space exhausted")
        } catch (solution: Solution) {
            val walls = LongArray(10)
            for (y in 1..10) {
                var line = 0L
                for (x in 1..10) {
                    val position = y * NEIGHBOUR_Y + x * NEIGHBOUR_X
                    val east = labyrinth[position + WALL_X].toLong().and(1)
                    val north = labyrinth[position - WALL_Y].toLong().and(2)
                    val west = labyrinth[position - WALL_X].toLong().and(4)
                    val south = labyrinth[position + WALL_Y].toLong().and(8)
                    line = line.shl(4).or(south).or(west).or(north).or(east)
                }
                walls[y - 1] = line
            }
            val destination = solution.destination
            val y = destination / NEIGHBOUR_Y
            val x = destination % NEIGHBOUR_Y / NEIGHBOUR_X
            return FloorPlan(walls).world().dropBeeper(x - 1, y - 1)
        } catch (ex: InterruptedException) {
            return Problem.emptyWorld
        }
    }

    private fun isUncharted(position: Int): Boolean {
        return labyrinth[position] >= '0'
    }

    private fun causesPartition(position: Int, direction: Int): Boolean {
        val front = position + NEIGHBOURS[direction]
        val left = position + NEIGHBOURS[(direction + 1).and(3)]
        val right = position + NEIGHBOURS[(direction - 1).and(3)]
        return !isUncharted(front) && isUncharted(left) && isUncharted(right)
    }

    private fun destinationOpen(position: Int, direction: Int, uncharted: Int) {
        if (Thread.interrupted()) throw InterruptedException()
        if (causesPartition(position, direction)) return
        if (uncharted == 0) throw Solution(position)

        val unchartedNeighbours = labyrinth[position]
        // The current cell is no longer uncharted
        labyrinth[position] = CHARTED

        val east = position + NEIGHBOUR_X
        val north = position - NEIGHBOUR_Y
        val west = position - NEIGHBOUR_X
        val south = position + NEIGHBOUR_Y

        // The current cell is no longer an uncharted neighbour of its neighbours
        --labyrinth[east]
        --labyrinth[north]
        --labyrinth[west]
        --labyrinth[south]

        var potentialDeadEnds = 0
        // A potential dead end is a neighbour that will turn into a dead end
        // unless we tear down the wall and pick it as our next position
        if (labyrinth[east] == '1') ++potentialDeadEnds
        if (labyrinth[north] == '1') ++potentialDeadEnds
        if (labyrinth[west] == '1') ++potentialDeadEnds
        if (labyrinth[south] == '1') ++potentialDeadEnds

        if (potentialDeadEnds == 0) {
            // We can roam freely without consequence
            for (dir in randomDirectionPermutation()) {
                val neighbour = position + NEIGHBOURS[dir]
                if (isUncharted(neighbour)) {
                    val wall = position + WALLS[dir]
                    labyrinth[wall] = FREE
                    destinationOpen(neighbour, dir, uncharted - 1)
                    labyrinth[wall] = WALL
                }
            }
        } else if (potentialDeadEnds == 2) {
            // We must eliminate one of the potential dead ends by picking it
            for (dir in randomDirectionPermutation()) {
                val neighbour = position + NEIGHBOURS[dir]
                if (labyrinth[neighbour] == '1') {
                    val wall = position + WALLS[dir]
                    labyrinth[wall] = FREE
                    destinationFound(neighbour, dir, uncharted - 1)
                    labyrinth[wall] = WALL
                }
            }
        } else if (potentialDeadEnds == 1) {
            // We can either eliminate the potential dead end by picking it,
            // or turn it into an actual dead end by picking another neighbour
            for (dir in randomDirectionPermutation()) {
                val neighbour = position + NEIGHBOURS[dir]
                if (isUncharted(neighbour)) {
                    val wall = position + WALLS[dir]
                    labyrinth[wall] = FREE
                    if (labyrinth[neighbour] == '1') {
                        destinationOpen(neighbour, dir, uncharted - 1)
                    } else {
                        destinationFound(neighbour, dir, uncharted - 1)
                    }
                    labyrinth[wall] = WALL
                }
            }
        }

        // backtrack
        ++labyrinth[east]
        ++labyrinth[north]
        ++labyrinth[west]
        ++labyrinth[south]

        labyrinth[position] = unchartedNeighbours
    }

    private fun destinationFound(position: Int, direction: Int, uncharted: Int) {
        if (Thread.interrupted()) throw InterruptedException()
        if (causesPartition(position, direction)) return
        if (uncharted == 0) throw Solution(position)

        val unchartedNeighbours = labyrinth[position]
        // The current cell is no longer uncharted
        labyrinth[position] = CHARTED

        val east = position + NEIGHBOUR_X
        val north = position - NEIGHBOUR_Y
        val west = position - NEIGHBOUR_X
        val south = position + NEIGHBOUR_Y

        // The current cell is no longer an uncharted neighbour of its neighbours
        --labyrinth[east]
        --labyrinth[north]
        --labyrinth[west]
        --labyrinth[south]

        var potentialDeadEnds = 0
        // A potential dead end is a neighbour that will turn into a dead end
        // unless we tear down the wall and pick it as our next position
        if (labyrinth[east] == '1') ++potentialDeadEnds
        if (labyrinth[north] == '1') ++potentialDeadEnds
        if (labyrinth[west] == '1') ++potentialDeadEnds
        if (labyrinth[south] == '1') ++potentialDeadEnds

        if (potentialDeadEnds == 0) {
            // We can roam freely without consequence
            for (dir in randomDirectionPermutation()) {
                val neighbour = position + NEIGHBOURS[dir]
                if (isUncharted(neighbour)) {
                    val wall = position + WALLS[dir]
                    labyrinth[wall] = FREE
                    destinationFound(neighbour, dir, uncharted - 1)
                    labyrinth[wall] = WALL
                }
            }
        } else if (potentialDeadEnds == 1) {
            // We must eliminate the potential dead end by picking it,
            // because we already found our destination earlier
            for (dir in randomDirectionPermutation()) {
                val neighbour = position + NEIGHBOURS[dir]
                if (labyrinth[neighbour] == '1') {
                    val wall = position + WALLS[dir]
                    labyrinth[wall] = FREE
                    destinationFound(neighbour, dir, uncharted - 1)
                    labyrinth[wall] = WALL
                }
            }
        }

        // backtrack
        ++labyrinth[east]
        ++labyrinth[north]
        ++labyrinth[west]
        ++labyrinth[south]

        labyrinth[position] = unchartedNeighbours
    }
}
