package logic

import java.lang.reflect.Method

object World {

    const val HEIGHT = 10
    const val WIDTH = 10

    const val EAST = 0
    const val NORTH = 1
    const val WEST = 2
    const val SOUTH = 3

    val emptyWorld: KarelWorld = FloorPlan.empty.world()

    private fun pillars(): KarelWorld {
        var world = emptyWorld

        for (x in 0..9) {
            for (y in rng.nextInt(10)..9) {
                world = world.dropBeeper(x, y)
            }
        }
        return world
    }

    private val rng = java.util.Random()

    private val problemSets: List<List<List<String>>> = listOf(
            listOf(
                    listOf("karelsFirstProgram")
            ),
            listOf(
                    listOf("obtainArtefact", "defuseOneBomb", "defuseTwoBombs", "practiceHomerun"),
                    listOf("climbTheStairs", "fillTheHoles", "saveTheFlower", "mowTheLawn"),
                    listOf("harvestTheField", "repairTheStreet", "cleanTheRoom", "tileTheFloor"),
                    listOf("stealOlympicFire", "removeTheTiles", "walkTheLabyrinth")
            ),
            listOf(
                    listOf("hangTheLampions", "followTheSeeds", "cleanTheTunnels"),
                    listOf("increment", "decrement", "addSlow"),
                    listOf("saveTheFlowers", "findTeddyBear", "jumpTheHurdles"),
                    listOf("solveTheMaze", "quantize", "addFast")
            ),
            listOf(
                    listOf("partyAgain", "fetchTheStars"),
                    listOf("secureTheCave", "layAndRemoveTiles"),
                    listOf("findShelters")
            )
    )

    val problemDirectory: List<String> = ArrayList<String>().apply {
        for ((a, sets) in problemSets.withIndex()) {
            for ((b, set) in sets.withIndex()) {
                set.mapIndexedTo(this) { c, problem -> "%d.%d.%d %s".format(a, b + 1, c + 1, problem) }
            }
        }
    }

    val problemNames: List<String> = problemSets.flatten().flatten()

    val problemMethods: List<Method> = problemNames.map(this::methodOrVal)

    fun methodOrVal(name: String): Method {
        val clazz = this::class.java
        return try {
            clazz.getMethod(name)
        } catch (synthesizedValMethodsHaveGetPrefix: NoSuchMethodException) {
            clazz.getMethod("get${name[0].toUpperCase()}${name.substring(1)}")
        }
    }

    fun load(problemMethod: Method): KarelWorld {
        return problemMethod(this) as KarelWorld
    }

    val karelsFirstProgram: KarelWorld = {
        val world = FloorPlan.first.world()

        world.dropBeeper(1, 9).withKarelAt(0, 9, EAST)
    }()

    val obtainArtefact: KarelWorld = {
        val world = FloorPlan.empty.builder().buildVerticalWall(5, 5).world()

        world.dropBeeper(6, 5).withKarelAt(3, 5, EAST)
    }()

    val defuseOneBomb: KarelWorld = {
        val world = emptyWorld.dropBeeper(9, 9)

        world.withKarelAt(0, 9, EAST)
    }()

    val defuseTwoBombs: KarelWorld = {
        val world = emptyWorld.dropBeeper(0, 0).dropBeeper(9, 9)

        world.withKarelAt(0, 9, EAST)
    }()

    val practiceHomerun: KarelWorld = {
        val world = emptyWorld.dropBeeper(0, 0).dropBeeper(9, 0).dropBeeper(0, 9).dropBeeper(9, 9)

        world.withKarelAt(0, 9, EAST)
    }()

    val fillTheHoles: KarelWorld = {
        val world = FloorPlan.holes.world()

        world.withKarelAt(1, 8, EAST)
    }()

    val climbTheStairs: KarelWorld = {
        val world = FloorPlan.stairs.world()

        world.withKarelAt(0, 9, EAST)
    }()

    val saveTheFlower: KarelWorld = {
        val world = FloorPlan.mountain.world().dropBeeper(1, 9)

        world.withKarelAt(0, 9, EAST)
    }()

    val mowTheLawn: KarelWorld = {
        val world = emptyWorld

        world.withBeepers(0x3f0fL, 0xc3f0fc3f0fcL.shl(20)).withKarelAt(1, 7, EAST)
    }()

    val harvestTheField: KarelWorld = {
        val world = emptyWorld

        world.withBeepers(0x805L, 0x2a1542a05008000).withKarelAt(5, 8, NORTH)
    }()

    fun repairTheStreet(): KarelWorld {
        val builder = FloorPlan.empty.builder()

        for (x in 0..9) {
            if (rng.nextBoolean()) {
                builder.buildHorizontalWall(x, 9)
            } else {
                builder.buildVerticalWall(x, 9)
                builder.buildVerticalWall(x + 1, 9)
            }
        }
        return builder.world().withKarelAt(0, 8, EAST)
    }

    fun cleanTheRoom(): KarelWorld {
        var world = emptyWorld

        for (y in 0..9) {
            for (x in 0..9) {
                if (rng.nextBoolean()) {
                    world = world.dropBeeper(x, y)
                }
            }
        }
        return world.withKarelAt(0, 9, EAST)
    }

    val tileTheFloor: KarelWorld = {
        emptyWorld.withKarelAt(0, 9, EAST)
    }()

    val stealOlympicFire: KarelWorld = {
        val world = FloorPlan.stairs.world().dropBeeper(7, 3)

        world.withKarelAt(0, 9, EAST)
    }()

    val removeTheTiles: KarelWorld = {
        val world = emptyWorld.fillWithBeepers()

        world.withKarelAt(0, 9, EAST)
    }()

    fun walkTheLabyrinth(): KarelWorld {
        return generateRandomLabyrinth()
    }

    fun hangTheLampions(): KarelWorld {
        val builder = FloorPlan.empty.builder()

        for (x in 0..9) {
            builder.buildHorizontalWall(x, 1 + rng.nextInt(3))
        }
        val world = builder.world().withBeepers(1023L.shl(90 - 64), 0)
        return world.withKarelAt(0, 9, EAST)
    }

    val followTheSeeds: KarelWorld = {
        val world = emptyWorld.withBeepers(0xffc017f50L, 0x55d5555157d405ffL)
        world.withKarelAt(5, 4, WEST)
    }()

    fun cleanTheTunnels(): KarelWorld {
        return pillars().withKarelAt(0, 9, EAST)
    }

    fun increment(): KarelWorld {
        var world = emptyWorld

        for (x in 2..9) {
            if (rng.nextBoolean()) {
                world = world.dropBeeper(x, 0)
            }
        }
        return world.withKarelAt(9, 0, WEST)
    }

    fun decrement(): KarelWorld = increment()

    fun addSlow(): KarelWorld {
        var world = emptyWorld

        for (y in 0..1) {
            for (x in 2..9) {
                if (rng.nextBoolean()) {
                    world = world.dropBeeper(x, y)
                }
            }
        }
        return world.withKarelAt(9, 0, WEST)
    }

    fun saveTheFlowers(): KarelWorld {
        val builder = FloorPlan.empty.builder()

        var y1 = rng.nextInt(5)
        var y2 = rng.nextInt(1 + y1)
        var y3 = rng.nextInt(1 + y2)
        var y4 = rng.nextInt(1 + y3)
        y1 += 5
        y2 += 4
        y3 += 3
        y4 += 2

        for (y in y1 until 10) builder.buildVerticalWall(1, y)
        builder.buildHorizontalWall(1, y1)
        for (y in y2 until y1) builder.buildVerticalWall(2, y)
        builder.buildHorizontalWall(2, y2)
        for (y in y3 until y2) builder.buildVerticalWall(3, y)
        builder.buildHorizontalWall(3, y3)
        for (y in y4 until y3) builder.buildVerticalWall(4, y)
        builder.buildHorizontalWall(4, y4)
        for (y in 1 until y4) builder.buildVerticalWall(5, y)

        builder.buildHorizontalWall(5, 1)

        var y7 = rng.nextInt(6)
        var y6 = rng.nextInt(1 + y7)
        var y5 = rng.nextInt(1 + y6)
        y7 += 4
        y6 += 3
        y5 += 2

        for (y in 1 until y5) builder.buildVerticalWall(6, y)
        builder.buildHorizontalWall(6, y5)
        for (y in y5 until y6) builder.buildVerticalWall(7, y)
        builder.buildHorizontalWall(7, y6)
        for (y in y6 until y7) builder.buildVerticalWall(8, y)
        builder.buildHorizontalWall(8, y7)
        for (y in y7 until 10) builder.buildVerticalWall(9, y)

        val world = builder.world().dropBeeper(1, y1 - 1).dropBeeper(2, y2 - 1).dropBeeper(3, y3 - 1).dropBeeper(4, y4 - 1)
        return world.withKarelAt(0, 9, EAST)
    }

    fun findTeddyBear(): KarelWorld {
        var world = emptyWorld

        val xy = rng.nextInt(10)
        when (rng.nextInt(4)) {
            EAST -> world = world.dropBeeper(9, xy)
            WEST -> world = world.dropBeeper(0, xy)
            NORTH -> world = world.dropBeeper(xy, 0)
            SOUTH -> world = world.dropBeeper(xy, 9)
        }
        val x = rng.nextInt(10)
        val y = rng.nextInt(10)
        val dir = rng.nextInt(4)
        return world.withKarelAt(x, y, dir)
    }

    fun jumpTheHurdles(): KarelWorld {
        val xBeeper = 5 + rng.nextInt(5)
        val builder = FloorPlan.empty.builder()

        for (x in 1..xBeeper) {
            for (y in 0 until rng.nextInt(10)) {
                builder.buildVerticalWall(x, 9 - y)
            }
        }
        return builder.world().dropBeeper(xBeeper, 9).withKarelAt(0, 9, EAST)
    }

    fun solveTheMaze(): KarelWorld {
        val builder = FloorPlan.maze.builder()
        var karel = builder.world().fillWithBeepers()

        fun generateMaze() {
            val angle = rng.nextInt(4)
            karel = karel.pickBeeper().turn(angle)
            repeat(4) {
                if (karel.beeperAhead()) {
                    builder.tearDownWall(karel.x, karel.y, karel.direction)
                    karel = karel.moveForward()
                    generateMaze()
                    karel = karel.turnAround()
                    builder.tearDownWall(karel.x, karel.y, karel.direction)
                    karel = karel.moveForward().turnAround()
                }
                karel = karel.turnLeft()
            }
            karel = karel.turn(-angle)
        }

        generateMaze()
        val x = rng.nextInt(10)
        val y = rng.nextInt(10)
        return karel.dropBeeper(x, y).withKarelAt(0, 0, EAST)
    }

    fun quantize(): KarelWorld {
        return pillars().withKarelAt(0, 9, EAST)
    }

    fun addFast(): KarelWorld {
        var world = emptyWorld

        for (y in 0..1) {
            for (x in 2..9) {
                if (rng.nextBoolean()) {
                    world = world.dropBeeper(x, y)
                }
            }
        }
        return world.withKarelAt(9, 0, SOUTH)
    }

    fun partyAgain(): KarelWorld {
        val builder = FloorPlan.trap.builder()

        for (x in 0..9) {
            builder.buildHorizontalWall(x, 1 + rng.nextInt(3))
        }
        val world = builder.world().withBeepers(1023.shl(80 - 64), 0)
        return world.withKarelAt(0, 8, EAST)
    }

    fun fetchTheStars(): KarelWorld {
        val builder = FloorPlan.trap.builder()
        var world = builder.world()

        for (x in 0..9) {
            val y = 1 + rng.nextInt(3)
            builder.buildHorizontalWall(x, y)
            world = world.dropBeeper(x, y)
        }
        return world.withKarelAt(0, 8, EAST)
    }

    fun secureTheCave(): KarelWorld {
        val builder = FloorPlan.empty.builder()
        var world = builder.world()

        for (x in 0..9) {
            val y = 1 + rng.nextInt(3)
            builder.buildHorizontalWall(x, y)
            for (a in y..y + rng.nextInt(3)) {
                world = world.dropBeeper(x, a)
            }
        }
        return world.withKarelAt(0, 9, EAST)
    }

    val layAndRemoveTiles: KarelWorld = {
        emptyWorld.withKarelAt(0, 9, EAST)
    }()

    fun findShelters(): KarelWorld {
        val builder = FloorPlan.empty.builder()

        repeat(25) {
            builder.buildHorizontalWall(rng.nextInt(10), 1 + rng.nextInt(9))
            builder.buildVerticalWall(1 + rng.nextInt(9), rng.nextInt(10))
        }
        val x = rng.nextInt(10)
        val y = rng.nextInt(10)
        val dir = rng.nextInt(4)
        return builder.world().withKarelAt(x, y, dir)
    }
}
