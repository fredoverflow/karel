package logic

import vm.Instructions
import vm.toInstructions
import java.math.BigInteger

val UNKNOWN = 0.toBigInteger()
val ONE = 1.toBigInteger()
val TWO = 2.toBigInteger()
val SHUFFLE = TWO..65536.toBigInteger()

class Problem(
    val index: String,
    separator: String,
    val name: String,
    val level: Level,
    val story: String,
    val goal: Instructions,
    val check: Check,
    val binaryLines: Int,
    val numWorlds: BigInteger,
    val createWorld: (id: Int) -> World
) {
    private val string = "$index $separator $name"

    override fun toString() = string

    val isRandom: Boolean
        get() = numWorlds != ONE

    private val random = Zufall()

    fun randomWorld(): World {
        return createWorld(random.nextInt65536())
    }

    fun randomWorlds(): Sequence<World> = when (numWorlds) {

        ONE -> sequenceOf(createWorld(0))

        in SHUFFLE -> shuffledWorlds()

        else -> generateSequence { createWorld(0) }
    }

    private fun shuffledWorlds(): Sequence<World> {

        var n = numWorlds.toInt()
        val a = IntArray(n) { index -> index }

        return generateSequence {
            if (n == 0) return@generateSequence null

            val i = random.nextInt(n)
            val world = createWorld(a[i])
            a[i] = a[--n]
            world
        }
    }

    companion object {
        const val EAST = 0
        const val NORTH = 1
        const val WEST = 2
        const val SOUTH = 3

        private fun pillars(random: Zufall): World {
            return FloorPlan.empty.world().apply {
                for (x in 0..9) {
                    for (y in random.nextInt(11)..9) {
                        dropBeeper(x, y)
                    }
                }
            }
        }

        private fun randomByte(entropy: WorldEntropy): World {
            return FloorPlan.binary.world().apply {
                setBeepers(0, entropy.nextByte().shl(2).toLong())
                setKarel(9, 0, WEST)
            }
        }

        private fun randomBytes(entropy: WorldEntropy, direction: Int): World {
            return FloorPlan.binary.world().apply {
                setBeepers(0, (entropy.nextByte().shl(2) + entropy.nextByte().shl(12)).toLong())
                setKarel(9, 0, direction)
            }
        }

        val introduction = Problem(
            "0.0.1",
            "",
            "karelsFirstProgram",
            Level.REPEAT,
            "Click the GOAL button (top left)\nand watch Karel go. Drag slider\nto adjust animation speed.\nCan you program Karel to perform\nthe same steps? Test with START!",
            "\u0001\u0005\u0001\u0002\u0001\u0004\u0001\u0006\u0001\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.introduction.world().apply {
                dropBeeper(1, 9)
                setKarel(0, 9, EAST)
            }
        }

        val obtainArtifact = Problem(
            "1.1.1",
            "\uD83E\uDD20",
            "obtainArtifact",
            Level.REPEAT,
            "Karel auditions for the new Indy\nmovie. To demonstrate talent,\nKarel re-enacts the classic scene\nwhere Indy saves some valuable\nartifact from an ancient temple.",
            "\u0004\u1c06\u0005\u1c06\u0006\u0000\u0001\u0002\u0001\u0001\u0001\u0002\u0001\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.empty.builder().buildVerticalWall(5, 5).world().apply {
                dropBeeper(6, 5)
                setKarel(3, 5, EAST)
            }
        }

        val defuseOneBomb = Problem(
            "1.1.2",
            "",
            "defuseOneBomb",
            Level.REPEAT,
            "Karel the demolition expert\ndefuses a bomb at the other end\nof the room and returns filled\nwith pride and self-confidence.\nHave you learned repeat (n) yet?",
            "\u1c06\u0005\u0003\u1c06\u0003\u0000\u1a09\u0001\u1b07\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                dropBeeper(9, 9)
                setKarel(0, 9, EAST)
            }
        }

        val defuseTwoBombs = Problem(
            "1.1.3",
            "",
            "defuseTwoBombs",
            Level.REPEAT,
            "One bomb is no problem for Karel.\nLet's spice up the challenge!\nShouldn't this be rather simple,\ngiven that Karel already knows\nhow to defuse one single bomb?",
            "\u1c02\u0002\u1c08\u0005\u0003\u1c08\u0003\u0000\u1a09\u0001\u1b09\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                dropBeeper(0, 0)
                dropBeeper(9, 9)
                setKarel(0, 9, EAST)
            }
        }

        val practiceHomeRun = Problem(
            "1.1.4",
            "",
            "practiceHomeRun",
            Level.REPEAT,
            "Karel's heart burns for baseball,\nbut merely watching does not cut\nit anymore. Tonight, let's sneak\ninto the stadium and perform our\nfirst home run. Adrenaline rush!",
            "\u1a04\u1a09\u0001\u1b02\u0005\u0002\u1b01\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                dropBeeper(0, 0)
                dropBeeper(9, 0)
                dropBeeper(0, 9)
                dropBeeper(9, 9)
                setKarel(0, 9, EAST)
            }
        }

        val climbTheStairs = Problem(
            "1.2.1",
            "",
            "climbTheStairs",
            Level.REPEAT,
            "The elevator seems to be\nout of service as of late...\nBut Karel is still pumped from\nthat home run and full of energy!",
            "\u0001\u1a06\u0002\u0001\u0004\u0001\u1b02\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.stairs.world().apply {
                setKarel(0, 9, EAST)
            }
        }

        val fillTheHoles = Problem(
            "1.2.2",
            "",
            "fillTheHoles",
            Level.REPEAT,
            "Karel considers a career in den-\ndistry. The local dental school\nhas Open House day. Coincidence?\nKarel gets to fill 4 carious\nteeth with dental amalgam. Ouch!",
            "\u1a04\u0001\u0004\u0001\u0006\u0003\u0001\u0004\u0001\u1b01\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.holes.world().apply {
                setKarel(1, 8, EAST)
            }
        }

        val saveTheFlower = Problem(
            "1.2.3",
            "",
            "saveTheFlower",
            Level.REPEAT,
            "During a vacation in the alps,\nKarel discovers a rare flower\nwhich has trouble blooming\nat such low altitude...\nIt's a long way to the top!",
            "\u0001\u0005\u1a04\u0002\u0001\u0001\u0004\u0001\u1b03\u0006\u1a04\u0001\u0004\u0001\u0001\u0002\u1b0b\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.mountain.world().apply {
                dropBeeper(1, 9)
                setKarel(0, 9, EAST)
            }
        }

        val mowTheLawn = Problem(
            "1.2.4",
            "",
            "mowTheLawn",
            Level.REPEAT,
            "Karel promised Granger to help in\nthe garden. Granger has already\npulled up the weeds, so Karel\ncan focus on mowing the lawn.",
            "\u1a02\u1c06\u0004\u0001\u0004\u1b01\u1c0a\u0002\u0001\u0002\u1a06\u0001\u0005\u1b0b\u0001\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                setBeepers(0x3f0f, 0xc3f0fc3f0fc shl 20)
                setKarel(1, 7, EAST)
            }
        }

        val harvestTheField = Problem(
            "1.3.1",
            "",
            "harvestTheField",
            Level.REPEAT,
            "Granger is an agricult -- erm...\nfarmer. After mowing the lawn,\nKarel can't reject the desperate\nplea for help on the farm.\nThe wheat is already overripe!",
            "\u1c05\u0004\u0001\u0004\u0001\u1c0a\u0001\u0002\u0001\u0002\u1a03\u0005\u0004\u0001\u0002\u0001\u1b0b\u0005\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                setBeepers(0x805, 0x2a1542a05008000)
                setKarel(5, 7, NORTH)
            }
        }

        val repairTheStreet = Problem(
            "1.3.2",
            "",
            "repairTheStreet",
            Level.IF,
            "Click the DICE button. Notice\nsomething? Not all streets are\ncreated equal! Have you learned\nabout the if/else statement yet?\nF7..F11 are Karel's conditions.",
            "\u1a09\u1c04\u0001\u1b01\u000b\u1e0c\u0004\u0001\u0006\u0003\u0001\u0004\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            TWO.pow(10),
        ) { id ->
            val entropy = WorldEntropy(id)
            val builder = FloorPlan.empty.builder()

            for (x in 0..9) {
                if (entropy.nextBoolean()) {
                    builder.buildHorizontalWall(x, 9)
                } else {
                    builder.buildVerticalWall(x, 9)
                    builder.buildVerticalWall(x + 1, 9)
                }
            }

            builder.world().apply {
                setKarel(0, 8, EAST)
            }
        }

        val cleanTheRoom: Problem = Problem(
            "1.3.3",
            "",
            "cleanTheRoom",
            Level.IF,
            "Granger is paying Karel a surprise\nvisit. But Karel's apartment\nis *really* out of shape :(\nThe chaos is almost overwhelming.\nCan Karel clean up in time?",
            "\u1a04\u1c06\u0004\u0001\u0004\u1b01\u1c0a\u0002\u0001\u0002\u1a09\u0007\u1e0e\u0005\u0001\u1b0b\u0007\u1e13\u0005\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            TWO.pow(100),
        ) {
            val random = cleanTheRoom.random

            FloorPlan.empty.world().apply {
                setBeepers(random.nextLong68719476736(), random.nextLong())
                setKarel(0, 9, EAST)
            }
        }

        val tileTheFloor = Problem(
            "1.3.4",
            "",
            "tileTheFloor",
            Level.IF,
            "During a routine visit to the\nhardware store, Karel can't\nresist buying some flagstones.\nThey seem to be a perfect fit\nfor the luxurious bathroom!",
            "\u1a64\u0006\u0008\u1f06\u000a\u1f07\u0002\u0001\u1b01\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                setKarel(0, 9, EAST)
            }
        }

        val stealOlympicFire = Problem(
            "1.4.1",
            "",
            "stealOlympicFire",
            Level.IF,
            "Karel is mad with olympic fever\nand somehow comes to believe\nit would be a good idea to\nsteal the olympic fire O_o\nLet's hope nobody will notice...",
            "\u0001\u1a06\u0002\u0001\u0004\u0001\u1b02\u0005\u0001\u0004\u1a06\u0001\u1b0b\u0002\u0001\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.stairs.world().apply {
                dropBeeper(7, 3)
                setKarel(0, 9, EAST)
            }
        }

        val removeTheTiles = Problem(
            "1.4.2",
            "",
            "removeTheTiles",
            Level.IF,
            "The flagstones were supposed to\nbe a surprise for Karel's new\nsweetheart, Taylor. Too bad green\nis not Taylor's favourite color.\nOh well, back to square one...",
            "\u1a64\u0005\u0008\u1f05\u0002\u0001\u1b01\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                fillWithBeepers()
                setKarel(0, 9, EAST)
            }
        }

        val walkTheLabyrinth = Problem(
            "1.4.3",
            "",
            "walkTheLabyrinth",
            Level.IF,
            "Click DICE several times.\nNote how the generated labyrinths\nare rather simple? They contain\nneither crossroads nor dead ends.\nExactly one path to the beeper!",
            "\u1a63\u000a\u1f0a\u0009\u1e09\u0002\u0001\u1b01\u0000\u0004\u0001\u1b01\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            UNKNOWN,
        ) {
            LabyrinthGenerator.generateLabyrinth()
        }

        val hangTheLampions = Problem(
            "2.1.1",
            "\uD83C\uDFEE",
            "hangTheLampions",
            Level.WHILE,
            "Karel was assembled 10 years ago!\nTo celebrate this anniversary,\nKarel bought 10 lampions. Now all\nthat's left to do is hang them\nfrom the (irregular) ceiling.",
            "\u1a09\u1c04\u0001\u1b01\u0002\u0005\u0001\u000a\u1f06\u0006\u0003\u0001\u000a\u1f0b\u0002\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            val entropy = WorldEntropy(id)
            val builder = FloorPlan.empty.builder()

            for (x in 0..9) {
                builder.buildHorizontalWall(x, 1 + entropy.nextInt(3))
            }

            builder.world().apply {
                setBeepers(0xffc000000, 0)
                setKarel(0, 9, EAST)
            }
        }

        val followTheSeeds = Problem(
            "2.1.2",
            "",
            "followTheSeeds",
            Level.WHILE,
            "Karel had insomnia and decided\nto take a walk in the forest.\nFortunately, Karel was smart\nenough to leave a trail of seeds\nto find the way back...",
            "\u0001\u0005\u0008\u1f00\u0002\u0008\u1f00\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                setBeepers(0xffc017f50, 0x55d5555157d405ff)
                setKarel(5, 4, WEST)
            }
        }

        val cleanTheTunnels: Problem = Problem(
            "2.1.3",
            "",
            "cleanTheTunnels",
            Level.WHILE,
            "Karel the coal miner discovers\nten tunnels of varying lengths\nfilled with valuable coal.\n(Does your solution work for\ntunnels of length 0 and 10?)",
            "\u1a09\u1c04\u0001\u1b01\u0007\u1e13\u0002\u0005\u0008\u1e14\u0001\u0005\u0008\u1f0a\u0003\u0001\u000a\u1f0f\u0002\u0000\u0004\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            11.toBigInteger().pow(10),
        ) {
            pillars(cleanTheTunnels.random).apply {
                setKarel(0, 9, EAST)
            }
        }

        val increment = Problem(
            "2.2.1",
            "",
            "increment",
            Level.WHILE,
            "Do you know binary numbers?\nen.wikipedia.org/wiki/Binary_number\nde.wikipedia.org/wiki/Dualsystem\nKarel wants to add 1 to a byte.\nThis is almost trivial in binary.",
            "\u0007\u1e06\u0005\u0001\u0007\u1f02\u0006\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0b1,
            TWO.pow(8),
        ) { id ->
            randomByte(WorldEntropy(id))
        }

        val decrement = Problem(
            "2.2.2",
            "",
            "decrement",
            Level.WHILE,
            "Karel wants to subtract 1 from\na byte. Notice any similarity\nto increment? (What happens if\nKarel decrements the byte zero?\nYou can click in Karel's world!)",
            "\u0007\u1f08\u0006\u000a\u1e08\u0001\u0007\u1e02\u0005\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0b1,
            TWO.pow(8),
        ) { id ->
            randomByte(WorldEntropy(id))
        }

        val addSlow = Problem(
            "2.2.3",
            "",
            "addSlow",
            Level.WHILE,
            "Welcome to the slowest adding\nmachine in the world! Karel just\ndecrements the first byte\nand increments the second byte\nuntil the first byte is zero.",
            "\u0007\u1f20\u0006\u000a\u1e26\u0001\u0007\u1e02\u0005\u0003\u0001\u000a\u1f0a\u0004\u0001\u0004\u0007\u1e23\u0005\u0001\u0007\u1f12\u0006\u0003\u0001\u000a\u1f18\u0002\u0001\u0002\u0007\u1e02\u0005\u0003\u1d0d\u0006\u0003\u1d1b\u0005\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0b11,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), WEST)
        }

        val saveTheFlowers = Problem(
            "2.3.1",
            "",
            "saveTheFlowers",
            Level.WHILE,
            "Karel climbs Mt. Everest. On the\nway up, Karel collects 4 flowers\nthat do not get enough sunlight\non the west side of the mountain.\nEast is where the sun comes up!",
            "\u1a05\u1d03\u0005\u0002\u0001\u000b\u1e04\u0004\u0001\u1b02\u1a04\u0006\u0001\u0004\u0001\u000a\u1f0e\u0002\u1b0b\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            3920.toBigInteger(),
        ) { id ->
            val entropy = WorldEntropy(id)
            val builder = FloorPlan.empty.builder()

            val upPermutations =
                "5432643265326542654374327532754275437632764276437652765376548432853285428543863286428643865286538654873287428743875287538754876287638764876594329532954295439632964296439652965396549732974297439752975397549762976397649765983298429843985298539854986298639864986598729873987498759876"
            val up = entropy.nextInt(upPermutations.length / 4) * 4
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
            val down = entropy.nextInt(downPermutations.length / 3) * 3
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

            builder.world().apply {
                dropBeeper(1, y1 - 1)
                dropBeeper(2, y2 - 1)
                dropBeeper(3, y3 - 1)
                dropBeeper(4, y4 - 1)
                setKarel(0, 9, EAST)
            }
        }

        val findTeddyBear = Problem(
            "2.3.2",
            "",
            "findTeddyBear",
            Level.WHILE,
            "In the middle of the night, Karel\nawakens from a terrible dream.\nThe teddy bear will provide\ncomfort. It should lay somewhere\nnear the edge of the bed...",
            "\u0007\u1f08\u000a\u1e06\u0001\u1d00\u0002\u1d00\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            14400.toBigInteger(),
        ) { id ->
            val entropy = WorldEntropy(id)

            FloorPlan.empty.world().apply {
                val zeroToEight = entropy.nextInt(9)

                when (entropy.nextDirection()) {
                    NORTH -> dropBeeper(zeroToEight, 0)
                    EAST -> dropBeeper(9, zeroToEight)
                    SOUTH -> dropBeeper(9 - zeroToEight, 9)
                    WEST -> dropBeeper(0, 9 - zeroToEight)
                }

                val x = entropy.nextInt(10)
                val y = entropy.nextInt(10)
                val dir = entropy.nextDirection()
                setKarel(x, y, dir)
            }
        }

        val jumpTheHurdles: Problem = Problem(
            "2.3.3",
            "",
            "jumpTheHurdles",
            Level.WHILE,
            "Karel signs up for the Olympics\nand is allowed to participate\nin the hurdle runs. After jumping\nall the hurdles, Karel receives a\nspecial medal made of copper!",
            "\u0007\u1f13\u000a\u1e06\u0001\u1d00\u0002\u0001\u000b\u1e07\u0004\u0001\u0004\u0001\u000a\u1f0d\u0002\u0007\u1e02\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            1111100000.toBigInteger(),
        ) {
            val random = jumpTheHurdles.random
            val xBeeper = 5 + random.nextInt(5)
            val builder = FloorPlan.empty.builder()

            for (x in 1..xBeeper) {
                for (y in 0 until random.nextInt(10)) {
                    builder.buildVerticalWall(x, 9 - y)
                }
            }

            builder.world().apply {
                dropBeeper(xBeeper, 9)
                setKarel(0, 9, EAST)
            }
        }

        val solveTheMaze = Problem(
            "2.4.1",
            "",
            "solveTheMaze",
            Level.WHILE,
            "Study the random mazes carefully.\nThey contain both crossroads and\ndead ends, but no loops. Maintain\ncontact with Karel's left wall\nand you should find the beeper!",
            "\u0007\u1f1a\u0009\u1e09\u0002\u0001\u0007\u1e02\u0000\u000a\u1e0f\u0001\u0007\u1e02\u0000\u000b\u1e16\u0004\u0001\u0007\u1e02\u0000\u0003\u0001\u0007\u1e02\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            UNKNOWN,
        ) {
            MazeGenerator.generateMaze()
        }

        val quantizeBits: Problem = Problem(
            "2.4.2",
            "",
            "quantizeBits",
            Level.WHILE,
            "Karel the hacker is eavesdropping\non an analog communications line\nand writes down 10 bits encoded\nas 0..5 (0) or 6..10 (1). Convert\nto always 0 (0) or always 10 (1).",
            "\u1a09\u1c04\u0001\u1b01\u0007\u1e2a\u0002\u0001\u0001\u0001\u0001\u0001\u0007\u1e1f\u0008\u1e15\u0001\u0008\u1f10\u000a\u1e19\u0001\u0006\u000a\u1f15\u0003\u0001\u000a\u1f1a\u0002\u0000\u0003\u0008\u1f25\u0001\u0008\u1e22\u0001\u0005\u0008\u1f25\u0002\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            11.toBigInteger().pow(10),
        ) {
            pillars(quantizeBits.random).apply {
                setKarel(0, 9, EAST)
            }
        }

        val addFast = Problem(
            "2.4.3",
            "",
            "addFast",
            Level.WHILE,
            "Karel adds two bytes from the\n1st and 2nd row and stores the\nsum in the 4th row. The 3rd row\nis reserved for the carry bits.\n(Does \"carry the 1\" ring a bell?)",
            "\u1a08\u0007\u1f13\u0001\u0007\u1f16\u0001\u0007\u1f19\u0001\u0004\u0001\u0004\u0001\u0001\u0001\u0003\u1b01\u0000\u0001\u0007\u1f24\u0001\u0007\u1f27\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0001\u0003\u1b01\u0000\u0001\u0007\u1f32\u0001\u0004\u0001\u0004\u0001\u0006\u0001\u0001\u0003\u1b01\u0000\u0001\u0006\u0004\u0001\u0004\u0001\u0006\u0001\u0001\u0003\u1b01\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0b1011,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), SOUTH)
        }

        val partyAgain = Problem(
            "3.1.1",
            "\uD83C\uDF88",
            "partyAgain",
            Level.RECURSION,
            "Karel is preparing the next big\nparty. Unfortunately, the floor\nis so soaked from the last party\nthat care must be taken not to\nbreak through into the cellar!",
            "\u1a09\u1c04\u0001\u1b01\u0002\u0005\u1c09\u0002\u0000\u000a\u1f0e\u0006\u0003\u0000\u0001\u1c09\u0001\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            val entropy = WorldEntropy(id)
            val builder = FloorPlan.trap.builder()

            for (x in 0..9) {
                builder.buildHorizontalWall(x, 1 + entropy.nextInt(3))
            }

            builder.world().apply {
                setBeepers(0x3ff0000, 0)
                setKarel(0, 8, EAST)
            }
        }

        val fetchTheStars = Problem(
            "3.1.2",
            "",
            "fetchTheStars",
            Level.RECURSION,
            "Karel arranges a romantic date\nwith Taylor on a frozen lake to\n\"fetch the stars from the sky\",\nwhich is German for \"goes to\nthe ends of the world and back\".",
            "\u1a09\u1c04\u0001\u1b01\u0002\u1c09\u0006\u0002\u0000\u000a\u1f0e\u0005\u0003\u0000\u0001\u1c09\u0001\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            val entropy = WorldEntropy(id)
            val builder = FloorPlan.trap.builder()

            builder.world().apply {
                for (x in 0..9) {
                    val y = 1 + entropy.nextInt(3)
                    builder.buildHorizontalWall(x, y)
                    dropBeeper(x, y)
                }
                setKarel(0, 8, EAST)
            }
        }

        val secureTheCave: Problem = Problem(
            "3.2.1",
            "",
            "secureTheCave",
            Level.RECURSION,
            "Karel the cave explorer earns a\nliving as a tourist guide. For\nsafety measures, Karel breaks all\nstalactites from the ceiling and\nre-erects them as stalagmites.",
            "\u1a09\u1c04\u0001\u1b01\u0002\u1c09\u1c0e\u0004\u0000\u0001\u000a\u1f09\u0003\u0000\u0007\u1e09\u0005\u0001\u1c0e\u0006\u0001\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            9.toBigInteger().pow(10),
        ) {
            val random = secureTheCave.random
            val builder = FloorPlan.empty.builder()

            builder.world().apply {
                for (x in 0..9) {
                    val y = 1 + random.nextInt(3)
                    builder.buildHorizontalWall(x, y)
                    for (a in y..y + random.nextInt(3)) {
                        dropBeeper(x, a)
                    }
                }
                setKarel(0, 9, EAST)
            }
        }

        val layAndRemoveTiles = Problem(
            "3.2.2",
            "",
            "layAndRemoveTiles",
            Level.RECURSION,
            "Karel tries a different set of\nflagstones. But again, Taylor\nis not enamored with the result.\nHence Karel immediately removes\nthe flagstones, in reverse order.",
            "\u0007\u1e04\u0003\u0000\u0006\u0008\u1f0e\u000a\u1e0e\u0001\u1c00\u0001\u0005\u0000\u0002\u0001\u1c00\u0001\u0004\u0005\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0,
            ONE,
        ) {
            FloorPlan.empty.world().apply {
                setKarel(0, 9, EAST)
            }
        }

        val findShelters: Problem = Problem(
            "3.3.1",
            "",
            "findShelters",
            Level.RECURSION,
            "Karel is part of an expedition to\nthe north pole. The first task is\nfinding storm-proof shelters.\nMark Karel's path with beepers,\nbut leave the shelters empty!",
            "\u1a04\u0008\u1f11\u000a\u1e11\u0001\u0009\u1f0c\u000a\u1f0c\u000b\u1e0e\u0006\u1c00\u0003\u0001\u0003\u0002\u1b01\u0000".toInstructions(),
            Check.EVERY_PICK_DROP_MOVE,
            0,
            UNKNOWN,
        ) {
            val random = findShelters.random
            val builder = FloorPlan.empty.builder()

            repeat(25) {
                builder.buildHorizontalWall(random.nextInt(10), 1 + random.nextInt(9))
                builder.buildVerticalWall(1 + random.nextInt(9), random.nextInt(10))
            }

            builder.world().apply {
                val x = random.nextInt(10)
                val y = random.nextInt(10)
                val dir = random.nextInt4()
                setKarel(x, y, dir)
            }
        }

        val addSmart = Problem(
            "3.3.2",
            "",
            "addSmart",
            Level.RECURSION,
            "Karel adds two bytes from the\n1st and 2nd row and stores the\nsum in the 3rd row. Dropping and\nchecking carry bits is no longer\nnecessary. What a smart robot!",
            "\u1d0c\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u000b\u1e29\u0007\u1f14\u0008\u1f01\u0004\u0001\u0002\u1d0a\u0008\u1e01\u0004\u0001\u0002\u000b\u1e29\u0007\u1e14\u0008\u1e16\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u1d19\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0b111,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), SOUTH)
        }

        val computeFibonacci = Problem(
            "3.3.3",
            "",
            "computeFibonacci",
            Level.RECURSION,
            "Given 2 Fibonacci numbers,\nKarel computes the next 8.\n\nen.wikipedia.org/wiki/Fibonacci_number\nde.wikipedia.org/wiki/Fibonacci-Folge",
            "\u1a08\u1d0d\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u000b\u1e2a\u0007\u1f15\u0008\u1f02\u0004\u0001\u0002\u1d0b\u0008\u1e02\u0004\u0001\u0002\u000b\u1e2a\u0007\u1e15\u0008\u1e17\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u1d1a\u0001\u0002\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0004\u1b0d\u0000".toInstructions(),
            Check.EVERY_PICK_DROP,
            0b1111111111,
            5.toBigInteger(),
        ) { id ->
            FloorPlan.binary.world().apply {
                when (id % 5) {
                    0 -> {
                        // 0 1
                        dropBeeper(9, 1)
                    }

                    1 -> {
                        // 1 1
                        dropBeeper(9, 0)
                        dropBeeper(9, 1)
                    }

                    2 -> {
                        // 1 2
                        dropBeeper(9, 0)
                        dropBeeper(8, 1)
                    }

                    3 -> {
                        // 2 3
                        dropBeeper(8, 0)
                        dropBeeper(8, 1)
                        dropBeeper(9, 1)
                    }

                    4 -> {
                        // 3 5
                        dropBeeper(8, 0)
                        dropBeeper(9, 0)
                        dropBeeper(7, 1)
                        dropBeeper(9, 1)
                    }

                    else -> error(id)
                }
                setKarel(9, 0, SOUTH)
            }
        }

        val problems: List<Problem> = listOf(
            introduction,

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

        val names: Set<String> = problems.mapTo(HashSet(), Problem::name)
    }

    fun dropsOptional256(): Boolean {
        return this === decrement || this === addSlow
    }
}
