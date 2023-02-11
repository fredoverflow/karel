package logic

import java.math.BigInteger

val UNKNOWN = 0.toBigInteger()
val ONE = 1.toBigInteger()
val TWO = 2.toBigInteger()
val SHUFFLE = TWO..65536.toBigInteger()

class Problem(
    val index: String,
    val name: String,
    val story: String,
    val goal: String,
    val binaryLines: Int,
    val numWorlds: BigInteger,
    val createWorld: (id: Int) -> World
) {
    override fun toString(): String = "$index $name"

    val isRandom: Boolean
        get() = numWorlds != ONE

    fun randomWorld(): World {
        return createWorld(rng.nextInt().ushr(1))
    }

    fun randomWorldIds(): Sequence<Int> = when (numWorlds) {
        ONE -> sequenceOf(0)

        in SHUFFLE -> (0 until numWorlds.toInt()).asSequence().shuffled()

        else -> generateSequence { 0 }
    }

    companion object {
        const val HEIGHT = 10
        const val WIDTH = 10

        const val EAST = 0
        const val NORTH = 1
        const val WEST = 2
        const val SOUTH = 3

        val emptyWorld: World = FloorPlan.empty.world()

        private fun pillars(): World {
            var world = emptyWorld

            for (x in 0..9) {
                for (y in rng.nextInt(11)..9) {
                    world = world.dropBeeper(x, y)
                }
            }
            return world
        }

        private fun randomByte(rng: WorldEntropy): World {
            var world = FloorPlan.binary.world()

            world = world.withBeepers(0, rng.nextInt(256).shl(2).toLong())

            return world.withKarelAt(9, 0, WEST)
        }

        private fun randomBytes(rng: WorldEntropy, direction: Int): World {
            var world = FloorPlan.binary.world()

            world = world.withBeepers(0, (rng.nextInt(256).shl(2) + rng.nextInt(256).shl(12)).toLong())

            return world.withKarelAt(9, 0, direction)
        }

        private val rng = java.util.Random()

        val karelsFirstProgram = Problem(
            "0.0.1",
            "karelsFirstProgram",
            "Click the GOAL button (top left)\nand watch Karel go. Drag slider\nto adjust animation speed.\nCan you program Karel to perform\nthe same steps? Test with START!",
            "\u0001\u0005\u0001\u0002\u0001\u0004\u0001\u0006\u0001\u0000",
            0,
            ONE,
        ) {
            val world = FloorPlan.first.world()

            world.dropBeeper(1, 9).withKarelAt(0, 9, EAST)
        }

        val obtainArtifact = Problem(
            "1.1.1",
            "obtainArtifact",
            "Karel auditions for the new Indy\nmovie. To demonstrate talent,\nKarel re-enacts the classic scene\nwhere Indy saves some valuable\nartifact from an ancient temple.",
            "\u0004\ua106\u0005\ua106\u0006\u0000\u0001\u0002\u0001\u0001\u0001\u0002\u0001\u0000",
            0,
            ONE,
        ) {
            val world = FloorPlan.empty.builder().buildVerticalWall(5, 5).world()

            world.dropBeeper(6, 5).withKarelAt(3, 5, EAST)
        }

        val defuseOneBomb = Problem(
            "1.1.2",
            "defuseOneBomb",
            "Karel the demolition expert\ndefuses a bomb at the other end\nof the room and returns filled\nwith pride and self-confidence.\nHave you learned repeat (n) yet?",
            "\ua106\u0005\u0003\ua106\u0003\u0000\u8009\u0001\u9107\u0000",
            0,
            ONE,
        ) {
            val world = emptyWorld.dropBeeper(9, 9)

            world.withKarelAt(0, 9, EAST)
        }

        val defuseTwoBombs = Problem(
            "1.1.3",
            "defuseTwoBombs",
            "One bomb is no problem for Karel.\nLet's spice up the challenge!\nShouldn't this be rather simple,\ngiven that Karel already knows\nhow to defuse one single bomb?",
            "\ua102\u0002\ua108\u0005\u0003\ua108\u0003\u0000\u8009\u0001\u9109\u0000",
            0,
            ONE,
        ) {
            val world = emptyWorld.dropBeeper(0, 0).dropBeeper(9, 9)

            world.withKarelAt(0, 9, EAST)
        }

        val practiceHomeRun = Problem(
            "1.1.4",
            "practiceHomeRun",
            "Karel's heart burns for baseball,\nbut merely watching does not cut\nit anymore. Tonight, let's sneak\ninto the stadium and perform our\nfirst home run. Adrenaline rush!",
            "\u8004\u8009\u0001\u9102\u0005\u0002\u9101\u0000",
            0,
            ONE,
        ) {
            val world = emptyWorld.dropBeeper(0, 0).dropBeeper(9, 0).dropBeeper(0, 9).dropBeeper(9, 9)

            world.withKarelAt(0, 9, EAST)
        }

        val climbTheStairs = Problem(
            "1.2.1",
            "climbTheStairs",
            "The elevator seems to be\nout of service as of late...\nBut Karel is still pumped from\nthat home run and full of energy!",
            "\u0001\u8006\u0002\u0001\u0004\u0001\u9102\u0000",
            0,
            ONE,
        ) {
            val world = FloorPlan.stairs.world()

            world.withKarelAt(0, 9, EAST)
        }

        val fillTheHoles = Problem(
            "1.2.2",
            "fillTheHoles",
            "Karel considers a career in den-\ndistry. The local dental school\nhas Open House day. Coincidence?\nKarel gets to fill 4 carious\nteeth with dental amalgam. Ouch!",
            "\u8004\u0001\u0004\u0001\u0006\u0003\u0001\u0004\u0001\u9101\u0000",
            0,
            ONE,
        ) {
            val world = FloorPlan.holes.world()

            world.withKarelAt(1, 8, EAST)
        }

        val saveTheFlower = Problem(
            "1.2.3",
            "saveTheFlower",
            "During a vacation in the alps,\nKarel discovers a rare flower\nwhich has trouble blooming\nat such low altitude...\nIt's a long way to the top!",
            "\u0001\u0005\u8004\u0002\u0001\u0001\u0004\u0001\u9103\u0006\u8004\u0001\u0004\u0001\u0001\u0002\u910b\u0000",
            0,
            ONE,
        ) {
            val world = FloorPlan.mountain.world().dropBeeper(1, 9)

            world.withKarelAt(0, 9, EAST)
        }

        val mowTheLawn = Problem(
            "1.2.4",
            "mowTheLawn",
            "Karel promised Granger to help in\nthe garden. Granger has already\npulled up the weeds, so Karel\ncan focus on mowing the lawn.",
            "\u8002\ua106\u0004\u0001\u0004\u9101\ua10a\u0002\u0001\u0002\u8006\u0001\u0005\u910b\u0001\u0000",
            0,
            ONE,
        ) {
            val world = emptyWorld

            world.withBeepers(0x3f0fL, 0xc3f0fc3f0fcL.shl(20)).withKarelAt(1, 7, EAST)
        }

        val harvestTheField = Problem(
            "1.3.1",
            "harvestTheField",
            "Granger is an agricult -- erm...\nfarmer. After mowing the lawn,\nKarel can't reject the desperate\nplea for help on the farm.\nThe wheat is already overripe!",
            "\ua105\u0004\u0001\u0004\u0001\ua10a\u0001\u0002\u0001\u0002\u8003\u0005\u0004\u0001\u0002\u0001\u910b\u0005\u0000",
            0,
            ONE,
        ) {
            val world = emptyWorld

            world.withBeepers(0x805L, 0x2a1542a05008000L).withKarelAt(5, 7, NORTH)
        }

        val repairTheStreet = Problem(
            "1.3.2",
            "repairTheStreet",
            "Click the DICE button. Notice\nsomething? Not all streets are\ncreated equal! Have you learned\nabout the if/else statement yet?\nF7..F11 are Karel's conditions.",
            "\u8009\ua104\u0001\u9101\u000b\uc10c\u0004\u0001\u0006\u0003\u0001\u0004\u0000",
            0,
            TWO.pow(10),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = FloorPlan.empty.builder()

            for (x in 0..9) {
                if (rng.nextBoolean()) {
                    builder.buildHorizontalWall(x, 9)
                } else {
                    builder.buildVerticalWall(x, 9)
                    builder.buildVerticalWall(x + 1, 9)
                }
            }
            builder.world().withKarelAt(0, 8, EAST)
        }

        val cleanTheRoom = Problem(
            "1.3.3",
            "cleanTheRoom",
            "Granger is paying Karel a surprise\nvisit. But Karel's apartment\nis *really* out of shape :(\nThe chaos is almost overwhelming.\nCan Karel clean up in time?",
            "\u8004\ua106\u0004\u0001\u0004\u9101\ua10a\u0002\u0001\u0002\u8009\ua10e\u0001\u910b\u0007\uc111\u0005\u0000",
            0,
            TWO.pow(100),
        ) {
            var world = emptyWorld

            world = world.withBeepers(rng.nextLong(), rng.nextLong())

            world.withKarelAt(0, 9, EAST)
        }

        val tileTheFloor = Problem(
            "1.3.4",
            "tileTheFloor",
            "During a routine visit to the\nhardware store, Karel can't\nresist buying some flagstones.\nThey seem to be a perfect fit\nfor the luxurious bathroom!",
            "\u8064\u0006\u000a\u000c\u0008\u000e\uc108\u0002\u0001\u9101\u0000",
            0,
            ONE,
        ) {
            emptyWorld.withKarelAt(0, 9, EAST)
        }

        val stealOlympicFire = Problem(
            "1.4.1",
            "stealOlympicFire",
            "Karel is mad with olympic fever\nand somehow comes to believe\nit would be a good idea to\nsteal the olympic fire O_o\nLet's hope nobody will notice...",
            "\u0001\u8006\u0002\u0001\u0004\u0001\u9102\u0005\u0001\u0004\u8006\u0001\u910b\u0002\u0001\u0000",
            0,
            ONE,
        ) {
            val world = FloorPlan.stairs.world().dropBeeper(7, 3)

            world.withKarelAt(0, 9, EAST)
        }

        val removeTheTiles = Problem(
            "1.4.2",
            "removeTheTiles",
            "The flagstones were supposed to\nbe a surprise for Karel's new\nsweetheart, Taylor. Too bad green\nis not Taylor's favourite color.\nOh well, back to square one...",
            "\u8064\u0005\u0008\ud105\u0002\u0001\u9101\u0000",
            0,
            ONE,
        ) {
            val world = emptyWorld.fillWithBeepers()

            world.withKarelAt(0, 9, EAST)
        }

        val walkTheLabyrinth = Problem(
            "1.4.3",
            "walkTheLabyrinth",
            "Click DICE several times.\nNote how the generated labyrinths\nare rather simple? They contain\nneither crossroads nor dead ends.\nExactly one path to the beeper!",
            "\u8063\u000a\ud108\u0009\uc107\u0002\ub108\u0004\u0001\u9101\u0000",
            0,
            UNKNOWN,
        ) {
            generateLabyrinth()
        }

        val hangTheLampions = Problem(
            "2.1.1",
            "hangTheLampions",
            "Karel was assembled 10 years ago!\nTo celebrate this anniversary,\nKarel bought 10 lampions. Now all\nthat's left to do is hang them\nfrom the (irregular) ceiling.",
            "\u8009\ua104\u0001\u9101\u0002\u0005\ua10d\u0006\u0003\ua10d\u0002\u0000\u0001\u000a\ud10c\u0000",
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = FloorPlan.empty.builder()

            for (x in 0..9) {
                builder.buildHorizontalWall(x, 1 + rng.nextInt(3))
            }
            val world = builder.world().withBeepers(1023L.shl(90 - 64), 0L)
            world.withKarelAt(0, 9, EAST)
        }

        val followTheSeeds = Problem(
            "2.1.2",
            "followTheSeeds",
            "Karel had insomnia and decided\nto take a walk in the forest.\nFortunately, Karel was smart\nenough to leave a trail of seeds\nto find the way back...",
            "\u0008\uc109\u0008\uc107\u0001\u0005\ub102\u0002\ub100\u0000",
            0,
            ONE,
        ) {
            val world = emptyWorld.withBeepers(0xffc017f50L, 0x55d5555157d405ffL)
            world.withKarelAt(5, 4, WEST)
        }

        val cleanTheTunnels = Problem(
            "2.1.3",
            "cleanTheTunnels",
            "Karel the coal miner discovers\nten tunnels of varying lengths\nfilled with valuable coal.\n(Does your solution work for\ntunnels of length 0 and 10?)",
            "\u8009\ua104\u0001\u9101\u0007\uc113\u0002\u0005\u0008\uc10d\u0001\u0005\ub108\u0003\u000a\uc112\u0001\ub10e\u0002\u0000",
            0,
            11.toBigInteger().pow(10),
        ) {
            pillars().withKarelAt(0, 9, EAST)
        }

        val increment = Problem(
            "2.2.1",
            "increment",
            "Do you know binary numbers?\nen.wikipedia.org/wiki/Binary_number\nde.wikipedia.org/wiki/Dualsystem\nKarel wants to add 1 to a byte.\nThis is almost trivial in binary.",
            "\u0007\uc105\u0005\u0001\ub100\u0006\u0000",
            0b1,
            TWO.pow(8),
        ) { id ->
            randomByte(WorldEntropy(id))
        }

        val decrement = Problem(
            "2.2.2",
            "decrement",
            "Karel wants to subtract 1 from\na byte. Notice any similarity\nto increment? (What happens if\nKarel decrements the byte zero?\nYou can click in Karel's world!)",
            "\u0007\ud107\u0006\u000a\uc107\u0001\ub100\u0005\u0000",
            0b1,
            TWO.pow(8),
        ) { id ->
            randomByte(WorldEntropy(id))
        }

        val addSlow = Problem(
            "2.2.3",
            "addSlow",
            "Welcome to the slowest adding\nmachine in the world! Karel just\ndecrements the first byte\nand increments the second byte\nuntil the first byte is zero.",
            "\ua114\u000a\uc11c\u0003\ua11e\u0004\u0001\u0004\u0007\uc10d\u0005\u0001\ub108\u0006\u0003\ua11e\u0002\u0001\u0002\ub100\u0007\ud11b\u0006\u000a\uc11b\u0001\ub114\u0005\u0000\u0001\u000a\ud11d\u0000",
            0b11,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), WEST)
        }

        val saveTheFlowers = Problem(
            "2.3.1",
            "saveTheFlowers",
            "Karel climbs Mt. Everest. On the\nway up, Karel collects 4 flowers\nthat do not get enough sunlight\non the west side of the mountain.\nEast is where the sun comes up!",
            "\u8004\ua110\u0005\u9101\ua110\u8004\u0006\u0001\u0004\u000a\uc10d\u0001\ub109\u0002\u9106\u0000\u0002\u000b\ud115\u0001\ub111\u0004\u0001\u0000",
            0,
            3920.toBigInteger(),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = FloorPlan.empty.builder()

            val upPermutations =
                "5432643265326542654374327532754275437632764276437652765376548432853285428543863286428643865286538654873287428743875287538754876287638764876594329532954295439632964296439652965396549732974297439752975397549762976397649765983298429843985298539854986298639864986598729873987498759876"
            val up = rng.nextInt(upPermutations.length / 4) * 4
            val y1 = upPermutations[up] - '0'
            val y2 = upPermutations[up + 1] - '0'
            val y3 = upPermutations[up + 2] - '0'
            val y4 = upPermutations[up + 3] - '0'

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

            val downPermutations =
                "234235236237238239245246247248249256257258259267268269278279289345346347348349356357358359367368369378379389456457458459467468469478479489567568569578579589678679689789"
            val down = rng.nextInt(downPermutations.length / 3) * 3
            val y5 = downPermutations[down] - '0'
            val y6 = downPermutations[down + 1] - '0'
            val y7 = downPermutations[down + 2] - '0'

            for (y in 1 until y5) builder.buildVerticalWall(6, y)
            builder.buildHorizontalWall(6, y5)
            for (y in y5 until y6) builder.buildVerticalWall(7, y)
            builder.buildHorizontalWall(7, y6)
            for (y in y6 until y7) builder.buildVerticalWall(8, y)
            builder.buildHorizontalWall(8, y7)
            for (y in y7 until 10) builder.buildVerticalWall(9, y)

            val world =
                builder.world().dropBeeper(1, y1 - 1).dropBeeper(2, y2 - 1).dropBeeper(3, y3 - 1).dropBeeper(4, y4 - 1)
            world.withKarelAt(0, 9, EAST)
        }

        val findTeddyBear = Problem(
            "2.3.2",
            "findTeddyBear",
            "In the middle of the night, Karel\nawakens from a terrible dream.\nThe teddy bear will provide\ncomfort. It should lay somewhere\nnear the edge of the bed...",
            "\u0007\ud108\u000a\uc106\u0001\ub100\u0002\ub100\u0000",
            0,
            16000.toBigInteger(),
        ) { id ->
            val rng = WorldEntropy(id)
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
            world.withKarelAt(x, y, dir)
        }

        val jumpTheHurdles = Problem(
            "2.3.3",
            "jumpTheHurdles",
            "Karel signs up for the Olympics\nand is allowed to participate\nin the hurdle runs. After jumping\nall the hurdles, Karel receives a\nspecial medal made of copper!",
            "\u0007\ud114\u000a\uc106\u0001\ub100\u0002\u000b\ud10b\u0001\ub107\u0004\u0001\u0004\u000a\uc112\u0001\ub10e\u0002\ub100\u0000",
            0,
            1111100000.toBigInteger(),
        ) {
            val xBeeper = 5 + rng.nextInt(5)
            val builder = FloorPlan.empty.builder()

            for (x in 1..xBeeper) {
                for (y in 0 until rng.nextInt(10)) {
                    builder.buildVerticalWall(x, 9 - y)
                }
            }
            builder.world().dropBeeper(xBeeper, 9).withKarelAt(0, 9, EAST)
        }

        val solveTheMaze = Problem(
            "2.4.1",
            "solveTheMaze",
            "Study the random mazes carefully.\nThey contain both crossroads and\ndead ends, but no loops. Maintain\ncontact with Karel's left wall\nand you should find the beeper!",
            "\u0007\ud10f\u0009\uc106\u0002\ub10d\u000a\ud10d\u000b\uc10c\u0004\ub10d\u0003\u0001\ub100\u0000",
            0,
            UNKNOWN,
        ) {
            val builder = FloorPlan.maze.builder()
            var world = builder.world().fillWithBeepers()

            fun generateMaze() {
                val angle = rng.nextInt(4)
                world = world.pickBeeper().turn(angle)
                repeat(4) {
                    if (world.beeperAhead()) {
                        builder.tearDownWall(world.x, world.y, world.direction)
                        world = world.moveForward()
                        generateMaze()
                        world = world.turnAround()
                        builder.tearDownWall(world.x, world.y, world.direction)
                        world = world.moveForward().turnAround()
                    }
                    world = world.turnLeft()
                }
                world = world.turn(-angle)
            }

            generateMaze()
            val x = rng.nextInt(10)
            val y = rng.nextInt(10)
            world.dropBeeper(x, y).withKarelAt(0, 0, EAST)
        }

        val quantizeBits = Problem(
            "2.4.2",
            "quantizeBits",
            "Karel the hacker is eavesdropping\non an analog communications line\nand writes down 10 bits encoded\nas 0..5 (0) or 6..10 (1). Convert\nto always 0 (0) or always 10 (1).",
            "\u8009\ua104\u0001\u9101\u0007\uc124\u0002\u8005\u0001\u9108\u0007\uc11a\u0008\uc110\u0001\ub10c\u000a\uc115\u0001\u0006\ub110\u0003\u0001\u000a\ud116\ub123\u0003\u0008\ud11f\u0001\ub11b\u0001\u0005\u0008\ud11f\u0002\u0000",
            0,
            11.toBigInteger().pow(10),
        ) {
            pillars().withKarelAt(0, 9, EAST)
        }

        val addFast = Problem(
            "2.4.3",
            "addFast",
            "Karel adds two bytes from the\n1st and 2nd row and stores the\nsum in the 4th row. The 3rd row\nis reserved for the carry bits.\n(Does \"carry the 1\" ring a bell?)",
            "\u8008\u0007\u0001\u0007\u0001\u0007\u0001\u0004\uc115\uc11c\uc10c\u0006\u0001\u0004\u0001\u0006\u0001\u0001\u0003\u9101\u0000\ud11c\uc118\u0006\u0001\u0004\u0001\ub110\ud10c\ub117",
            0b1011,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), SOUTH)
        }

        val partyAgain = Problem(
            "3.1.1",
            "partyAgain",
            "Karel is preparing the next big\nparty. Unfortunately, the floor\nis so soaked from the last party\nthat care must be taken not to\nbreak through into the cellar!",
            "\u8009\ua104\u0001\u9101\u0002\u0005\ua109\u0002\u0000\u000a\ud10e\u0006\u0003\u0000\u0001\ua109\u0001\u0000",
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = FloorPlan.trap.builder()

            for (x in 0..9) {
                builder.buildHorizontalWall(x, 1 + rng.nextInt(3))
            }
            val world = builder.world().withBeepers(1023L.shl(80 - 64), 0L)
            world.withKarelAt(0, 8, EAST)
        }

        val fetchTheStars = Problem(
            "3.1.2",
            "fetchTheStars",
            "Karel arranges a romantic date\nwith Taylor on a frozen lake to\n\"fetch the stars from the sky\",\nwhich is German for \"goes to\nthe ends of the world and back\".",
            "\u8009\ua104\u0001\u9101\u0002\ua109\u0006\u0002\u0000\u000a\ud10e\u0005\u0003\u0000\u0001\ua109\u0001\u0000",
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = FloorPlan.trap.builder()
            var world = builder.world()

            for (x in 0..9) {
                val y = 1 + rng.nextInt(3)
                builder.buildHorizontalWall(x, y)
                world = world.dropBeeper(x, y)
            }
            world.withKarelAt(0, 8, EAST)
        }

        val secureTheCave = Problem(
            "3.2.1",
            "secureTheCave",
            "Karel the cave explorer earns a\nliving as a tourist guide. For\nsafety measures, Karel breaks all\nstalactites from the ceiling and\nre-erects them as stalagmites.",
            "\u8009\ua104\u0001\u9101\u0002\ua109\ua10e\u0004\u0000\u0001\u000a\ud109\u0003\u0000\u0007\uc109\u0005\u0001\ua10e\u0006\u0001\u0000",
            0,
            9.toBigInteger().pow(10),
        ) {
            val builder = FloorPlan.empty.builder()
            var world = builder.world()

            for (x in 0..9) {
                val y = 1 + rng.nextInt(3)
                builder.buildHorizontalWall(x, y)
                for (a in y..y + rng.nextInt(3)) {
                    world = world.dropBeeper(x, a)
                }
            }
            world.withKarelAt(0, 9, EAST)
        }

        val layAndRemoveTiles = Problem(
            "3.2.2",
            "layAndRemoveTiles",
            "Karel tries a different set of\nflagstones. But again, Taylor\nis not enamored with the result.\nHence Karel immediately removes\nthe flagstones, in reverse order.",
            "\u0007\uc104\u0003\u0000\u0006\u000a\u0008\u000c\u000d\uc10e\u0001\ua100\u0001\ub113\u0002\u0001\ua100\u0001\u0004\u0005\u0000",
            0,
            ONE,
        ) {
            emptyWorld.withKarelAt(0, 9, EAST)
        }

        val findShelters = Problem(
            "3.3.1",
            "findShelters",
            "Karel is part of an expedition to\nthe north pole. The first task is\nfinding storm-proof shelters.\nMark Karel's path with beepers,\nbut leave the shelters empty!",
            "\u8004\u000a\u0008\u000c\u000d\uc112\u0001\u0009\u000a\u000b\u000e\u000e\uc10f\u0006\ua100\u0003\u0001\u0003\u0002\u9101\u0000",
            0,
            UNKNOWN,
        ) {
            val builder = FloorPlan.empty.builder()

            repeat(25) {
                builder.buildHorizontalWall(rng.nextInt(10), 1 + rng.nextInt(9))
                builder.buildVerticalWall(1 + rng.nextInt(9), rng.nextInt(10))
            }
            val x = rng.nextInt(10)
            val y = rng.nextInt(10)
            val dir = rng.nextInt(4)
            builder.world().withKarelAt(x, y, dir)
        }

        val addSmart = Problem(
            "3.3.2",
            "addSmart",
            "Karel adds two bytes from the\n1st and 2nd row and stores the\nsum in the 3rd row. Dropping and\nchecking carry bits is no longer\nnecessary. What a smart robot!",
            "\u000b\uc123\u0007\u0008\u000e\ud108\ua120\ub100\u0007\u0008\u000d\uc10e\ua120\ub110\ua124\ub100\u000b\uc123\u0007\u0008\u000e\ud118\ua124\ub100\u0007\u0008\u000d\uc11e\ua124\ub110\ua120\ub110\u0004\u0001\u0002\u0000\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u0000",
            0b111,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), SOUTH)
        }

        val computeFibonacci = Problem(
            "3.3.3",
            "computeFibonacci",
            "Given 2 Fibonacci numbers,\nKarel computes the next 8.\n\nen.wikipedia.org/wiki/Fibonacci_number\nde.wikipedia.org/wiki/Fibonacci-Folge",
            "\u8008\ua10b\u0001\u0002\u000a\uc108\u0001\ub104\u0004\u9101\u0000\u000b\uc12e\u0007\u0008\u000e\ud113\ua12b\ub10b\u0007\u0008\u000d\uc119\ua12b\ub11b\ua12f\ub10b\u000b\uc12e\u0007\u0008\u000e\ud123\ua12f\ub10b\u0007\u0008\u000d\uc129\ua12f\ub11b\ua12b\ub11b\u0004\u0001\u0002\u0000\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u0000",
            0b1111111111,
            5.toBigInteger(),
        ) { id ->
            val world = FloorPlan.binary.world().withKarelAt(9, 0, SOUTH)
            when (id % 5) {
                0 -> world.dropBeeper(9, 1) // 0 1
                1 -> world.dropBeeper(9, 0).dropBeeper(9, 1) // 1 1
                2 -> world.dropBeeper(9, 0).dropBeeper(8, 1) // 1 2
                3 -> world.dropBeeper(8, 0).dropBeeper(8, 1).dropBeeper(9, 1) // 2 3
                4 -> world.dropBeeper(8, 0).dropBeeper(9, 0).dropBeeper(7, 1).dropBeeper(9, 1) // 3 5

                else -> error(id)
            }
        }

        val problems: List<Problem> = listOf(
            karelsFirstProgram,

            obtainArtifact,
            defuseOneBomb,
            defuseTwoBombs,
            practiceHomeRun,
            climbTheStairs,
            fillTheHoles,
            saveTheFlower,
            mowTheLawn,
            harvestTheField,
            repairTheStreet,
            cleanTheRoom,
            tileTheFloor,
            stealOlympicFire,
            removeTheTiles,
            walkTheLabyrinth,

            hangTheLampions,
            followTheSeeds,
            cleanTheTunnels,
            increment,
            decrement,
            addSlow,
            saveTheFlowers,
            findTeddyBear,
            jumpTheHurdles,
            solveTheMaze,
            quantizeBits,
            addFast,

            partyAgain,
            fetchTheStars,
            secureTheCave,
            layAndRemoveTiles,
            findShelters,
            addSmart,
            computeFibonacci,
        )
    }
}
