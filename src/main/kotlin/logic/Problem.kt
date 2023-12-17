package logic

import java.math.BigInteger
import kotlin.random.Random

val UNKNOWN = 0.toBigInteger()
val ONE = 1.toBigInteger()
val TWO = 2.toBigInteger()
val SHUFFLE = TWO..65536.toBigInteger()

class Problem(
    val index: String,
    val name: String,
    val story: String,
    val goal: String,
    val checkAfter: CheckAfter,
    val binaryLines: Int,
    val numWorlds: BigInteger,
    val createWorld: (id: Int) -> World
) {
    override fun toString(): String = "$index $name"

    val isRandom: Boolean
        get() = numWorlds != ONE

    fun randomWorld(): World {
        return createWorld(Random.nextInt().ushr(1))
    }

    fun randomWorlds(): Sequence<World> = when (numWorlds) {
        ONE -> sequenceOf(createWorld(0))

        in SHUFFLE -> (0 until numWorlds.toInt()).asSequence().shuffled().map(createWorld)

        else -> generateSequence { createWorld(0) }
    }

    companion object {
        const val EAST = 0
        const val NORTH = 1
        const val WEST = 2
        const val SOUTH = 3

        private fun pillars(): World {
            val builder = fenced()

            for (x in 0..9) {
                for (y in Random.nextInt(11)..9) {
                    builder.drop(x, y)
                }
            }

            return builder.placeKarel()
        }

        private fun randomByte(rng: WorldEntropy): World {
            return binary()
                .drop(2, 0, 9, 0, rng::nextBoolean)
                .placeKarel(9, 0, WEST)
        }

        private fun randomBytes(rng: WorldEntropy, direction: Int): World {
            return binary()
                .drop(2, 0, 9, 1, rng::nextBoolean)
                .placeKarel(9, 0, direction)
        }

        private fun party(rng: WorldEntropy, y: Int): World {
            val builder = fenced()

            for (x in 0..9) {
                builder.spawn(x, 1 + rng.nextInt(3)).east(1)
            }

            return builder
                .drop(0, y, 9, y) { -> true }
                .placeKarel(0, y)
        }

        val karelsFirstProgram = Problem(
            "0.0.1",
            "karelsFirstProgram",
            "Click the GOAL button (top left)\nand watch Karel go. Drag slider\nto adjust animation speed.\nCan you program Karel to perform\nthe same steps? Test with START!",
            "\u0001\u0005\u0001\u0002\u0001\u0004\u0001\u0006\u0001\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            WorldBuilder()
                .east(3)
                .north(1)
                .east(2)
                .north(2)
                .west(5)
                .south(3)
                .drop(1, 9)
                .placeKarel()
        }

        val obtainArtifact = Problem(
            "1.1.1",
            "obtainArtifact",
            "Karel auditions for the new Indy\nmovie. To demonstrate talent,\nKarel re-enacts the classic scene\nwhere Indy saves some valuable\nartifact from an ancient temple.",
            "\u0004\ua106\u0005\ua106\u0006\u0000\u0001\u0002\u0001\u0001\u0001\u0002\u0001\u0000",
            CheckAfter.BEEPER,
            0,
            ONE,
        ) {
            fenced()
                .spawn(5, 5)
                .south(1)
                .drop(6, 5)
                .placeKarel(3, 5)
        }

        val defuseOneBomb = Problem(
            "1.1.2",
            "defuseOneBomb",
            "Karel the demolition expert\ndefuses a bomb at the other end\nof the room and returns filled\nwith pride and self-confidence.\nHave you learned repeat (n) yet?",
            "\ua106\u0005\u0003\ua106\u0003\u0000\u8009\u0001\u9107\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            fenced()
                .drop(9, 9)
                .placeKarel()
        }

        val defuseTwoBombs = Problem(
            "1.1.3",
            "defuseTwoBombs",
            "One bomb is no problem for Karel.\nLet's spice up the challenge!\nShouldn't this be rather simple,\ngiven that Karel already knows\nhow to defuse one single bomb?",
            "\ua102\u0002\ua108\u0005\u0003\ua108\u0003\u0000\u8009\u0001\u9109\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            fenced()
                .drop(0, 0)
                .drop(9, 9)
                .placeKarel()
        }

        val practiceHomeRun = Problem(
            "1.1.4",
            "practiceHomeRun",
            "Karel's heart burns for baseball,\nbut merely watching does not cut\nit anymore. Tonight, let's sneak\ninto the stadium and perform our\nfirst home run. Adrenaline rush!",
            "\u8004\u8009\u0001\u9102\u0005\u0002\u9101\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            fenced()
                .drop(0, 0)
                .drop(9, 0)
                .drop(0, 9)
                .drop(9, 9)
                .placeKarel()
        }

        val climbTheStairs = Problem(
            "1.2.1",
            "climbTheStairs",
            "The elevator seems to be\nout of service as of late...\nBut Karel is still pumped from\nthat home run and full of energy!",
            "\u0001\u8006\u0002\u0001\u0004\u0001\u9102\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            WorldBuilder()
                .east(2)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .south(6).east(2)
                .north(10)
                .west(10)
                .south(10)
                .placeKarel()
        }

        val fillTheHoles = Problem(
            "1.2.2",
            "fillTheHoles",
            "Karel considers a career in den-\ndistry. The local dental school\nhas Open House day. Coincidence?\nKarel gets to fill 4 carious\nteeth with dental amalgam. Ouch!",
            "\u8004\u0001\u0004\u0001\u0006\u0003\u0001\u0004\u0001\u9101\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            WorldBuilder().spawn(0, 8)
                .east(1).south(1).east(1)
                .south(1).east(1).north(1).east(1)
                .south(1).east(1).north(1).east(1)
                .south(1).east(1).north(1).east(1)
                .south(1).east(1).north(1).east(1)
                .north(9)
                .west(10)
                .south(8)
                .placeKarel(1, 8)
        }

        val saveTheFlower = Problem(
            "1.2.3",
            "saveTheFlower",
            "During a vacation in the alps,\nKarel discovers a rare flower\nwhich has trouble blooming\nat such low altitude...\nIt's a long way to the top!",
            "\u0001\u0005\u8004\u0002\u0001\u0001\u0004\u0001\u9103\u0006\u8004\u0001\u0004\u0001\u0001\u0002\u910b\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            WorldBuilder()
                .east(2)
                .drop()
                .north(2).east(1)
                .north(2).east(1)
                .north(2).east(1)
                .north(2).east(1)
                .south(2).east(1)
                .south(2).east(1)
                .south(2).east(1)
                .south(2).east(1)
                .north(10)
                .west(10)
                .south(10)
                .placeKarel()
        }

        val mowTheLawn = Problem(
            "1.2.4",
            "mowTheLawn",
            "Karel promised Granger to help in\nthe garden. Granger has already\npulled up the weeds, so Karel\ncan focus on mowing the lawn.",
            "\u8002\ua106\u0004\u0001\u0004\u9101\ua10a\u0002\u0001\u0002\u8006\u0001\u0005\u910b\u0001\u0000",
            CheckAfter.BEEPER,
            0,
            ONE,
        ) {
            fenced()
                .drop(2, 2, 7, 7) { -> true }
                .placeKarel(1, 7)
        }

        val harvestTheField = Problem(
            "1.3.1",
            "harvestTheField",
            "Granger is an agricult -- erm...\nfarmer. After mowing the lawn,\nKarel can't reject the desperate\nplea for help on the farm.\nThe wheat is already overripe!",
            "\ua105\u0004\u0001\u0004\u0001\ua10a\u0001\u0002\u0001\u0002\u8003\u0005\u0004\u0001\u0002\u0001\u910b\u0005\u0000",
            CheckAfter.BEEPER,
            0,
            ONE,
        ) {
            fenced()
                .drop(5, 1)
                .drop(4, 2).drop(6, 2)
                .drop(3, 3).drop(5, 3).drop(7, 3)
                .drop(2, 4).drop(4, 4).drop(6, 4).drop(8, 4)
                .drop(3, 5).drop(5, 5).drop(7, 5)
                .drop(4, 6).drop(6, 6)
                .drop(5, 7)
                .placeKarel(5, 7, NORTH)
        }

        val repairTheStreet = Problem(
            "1.3.2",
            "repairTheStreet",
            "Click the DICE button. Notice\nsomething? Not all streets are\ncreated equal! Have you learned\nabout the if/else statement yet?\nF7..F11 are Karel's conditions.",
            "\u8009\ua104\u0001\u9101\u000b\uc10c\u0004\u0001\u0006\u0003\u0001\u0004\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            TWO.pow(10),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = WorldBuilder().spawn(0, 9)

            for (x in 0..9) {
                if (rng.nextBoolean()) {
                    builder.east(1)
                } else {
                    builder.south(1).east(1).north(1)
                }
            }

            builder
                .north(9)
                .west(10)
                .south(9)
                .placeKarel(0, 8)
        }

        val cleanTheRoom = Problem(
            "1.3.3",
            "cleanTheRoom",
            "Granger is paying Karel a surprise\nvisit. But Karel's apartment\nis *really* out of shape :(\nThe chaos is almost overwhelming.\nCan Karel clean up in time?",
            "\u8004\ua106\u0004\u0001\u0004\u9101\ua10a\u0002\u0001\u0002\u8009\u0007\uc10e\u0005\u0001\u910b\u0007\uc113\u0005\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            TWO.pow(100),
        ) {
            fenced()
                .drop(0, 0, 9, 9, Random::nextBoolean)
                .placeKarel()
        }

        val tileTheFloor = Problem(
            "1.3.4",
            "tileTheFloor",
            "During a routine visit to the\nhardware store, Karel can't\nresist buying some flagstones.\nThey seem to be a perfect fit\nfor the luxurious bathroom!",
            "\u8064\u0006\u0008\ud106\u000a\ud107\u0002\u0001\u9101\u0000",
            CheckAfter.BEEPER,
            0,
            ONE,
        ) {
            fenced()
                .placeKarel()
        }

        val stealOlympicFire = Problem(
            "1.4.1",
            "stealOlympicFire",
            "Karel is mad with olympic fever\nand somehow comes to believe\nit would be a good idea to\nsteal the olympic fire O_o\nLet's hope nobody will notice...",
            "\u0001\u8006\u0002\u0001\u0004\u0001\u9102\u0005\u0001\u0004\u8006\u0001\u910b\u0002\u0001\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            WorldBuilder()
                .east(2)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .north(1).east(1)
                .south(6).east(2)
                .north(10)
                .west(10)
                .south(10)
                .drop(7, 3)
                .placeKarel()
        }

        val removeTheTiles = Problem(
            "1.4.2",
            "removeTheTiles",
            "The flagstones were supposed to\nbe a surprise for Karel's new\nsweetheart, Taylor. Too bad green\nis not Taylor's favourite color.\nOh well, back to square one...",
            "\u8064\u0005\u0008\ud105\u0002\u0001\u9101\u0000",
            CheckAfter.BEEPER,
            0,
            ONE,
        ) {
            fenced()
                .drop(0, 0, 9, 9) { -> true }
                .placeKarel()
        }

        val walkTheLabyrinth = Problem(
            "1.4.3",
            "walkTheLabyrinth",
            "Click DICE several times.\nNote how the generated labyrinths\nare rather simple? They contain\nneither crossroads nor dead ends.\nExactly one path to the beeper!",
            "\u8063\u000a\ud10a\u0009\uc109\u0002\u0001\u9101\u0000\u0004\u0001\u9101\u0000",
            CheckAfter.FINISH,
            0,
            UNKNOWN,
        ) {
            TODO()
        }

        val hangTheLampions = Problem(
            "2.1.1",
            "hangTheLampions",
            "Karel was assembled 10 years ago!\nTo celebrate this anniversary,\nKarel bought 10 lampions. Now all\nthat's left to do is hang them\nfrom the (irregular) ceiling.",
            "\u8009\ua104\u0001\u9101\u0002\u0005\u0001\u000a\ud106\u0006\u0003\u0001\u000a\ud10b\u0002\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            party(WorldEntropy(id), 9)
        }

        val followTheSeeds = Problem(
            "2.1.2",
            "followTheSeeds",
            "Karel had insomnia and decided\nto take a walk in the forest.\nFortunately, Karel was smart\nenough to leave a trail of seeds\nto find the way back...",
            "\u0001\u0005\u0008\ud100\u0002\u0008\ud100\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            ONE,
        ) {
            val world = fenced().placeKarel(5, 4, WEST)

            for (n in arrayOf(1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9)) {
                repeat(n) {
                    world.moveForward()
                    world.dropBeeper()
                }
                world.turnLeft()
            }
            repeat(5) {
                world.moveForward()
            }
            world.turnLeft()
            repeat(4) {
                world.moveForward()
            }

            world
        }

        val cleanTheTunnels = Problem(
            "2.1.3",
            "cleanTheTunnels",
            "Karel the coal miner discovers\nten tunnels of varying lengths\nfilled with valuable coal.\n(Does your solution work for\ntunnels of length 0 and 10?)",
            "\u8009\ua104\u0001\u9101\u0007\uc113\u0002\u0005\u0008\uc114\u0001\u0005\u0008\ud10a\u0003\u0001\u000a\ud10f\u0002\u0000\u0004\u0000",
            CheckAfter.BEEPER,
            0,
            11.toBigInteger().pow(10),
        ) {
            pillars()
        }

        val increment = Problem(
            "2.2.1",
            "increment",
            "Do you know binary numbers?\nen.wikipedia.org/wiki/Binary_number\nde.wikipedia.org/wiki/Dualsystem\nKarel wants to add 1 to a byte.\nThis is almost trivial in binary.",
            "\u0007\uc106\u0005\u0001\u0007\ud102\u0006\u0000",
            CheckAfter.FINISH,
            0b1,
            TWO.pow(8),
        ) { id ->
            randomByte(WorldEntropy(id))
        }

        val decrement = Problem(
            "2.2.2",
            "decrement",
            "Karel wants to subtract 1 from\na byte. Notice any similarity\nto increment? (What happens if\nKarel decrements the byte zero?\nYou can click in Karel's world!)",
            "\u0007\ud108\u0006\u000a\uc108\u0001\u0007\uc102\u0005\u0000",
            CheckAfter.FINISH,
            0b1,
            TWO.pow(8),
        ) { id ->
            randomByte(WorldEntropy(id))
        }

        val addSlow = Problem(
            "2.2.3",
            "addSlow",
            "Welcome to the slowest adding\nmachine in the world! Karel just\ndecrements the first byte\nand increments the second byte\nuntil the first byte is zero.",
            "\u0007\ud120\u0006\u000a\uc126\u0001\u0007\uc102\u0005\u0003\u0001\u000a\ud10a\u0004\u0001\u0004\u0007\uc123\u0005\u0001\u0007\ud112\u0006\u0003\u0001\u000a\ud118\u0002\u0001\u0002\u0007\uc102\u0005\u0003\ub10d\u0006\u0003\ub11b\u0005\u0000",
            CheckAfter.FINISH,
            0b11,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), WEST)
        }

        val saveTheFlowers = Problem(
            "2.3.1",
            "saveTheFlowers",
            "Karel climbs Mt. Everest. On the\nway up, Karel collects 4 flowers\nthat do not get enough sunlight\non the west side of the mountain.\nEast is where the sun comes up!",
            "\u8005\ub103\u0005\u0002\u0001\u000b\uc104\u0004\u0001\u9102\u8004\u0006\u0001\u0004\u0001\u000a\ud10e\u0002\u910b\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            3920.toBigInteger(),
        ) { id ->
            val rng = WorldEntropy(id)
            val ups =
                "11115111241113311142111511121411223112321124111313113221133111412114211151112114121231213212141122131222212231123121232112411131131312213131132121322113311141121412114211151112111421123211322114121213212222123121312213212141122113221222213122212222212231123112231212321124111311133112231131312123122131311321123212132211331114111241121412114211151111"
            val up = rng.nextInt(ups.length / 5) * 5
            val downs =
                "11161125113411431152121512241233124212511314132313321341141314221431151215212115212421332142215122142223223222412313232223312412242125113114312331323141321332223231331233213411411341224131421242214311511251215211"
            val down = rng.nextInt(downs.length / 4) * 4

            WorldBuilder()
                .east(1)
                .north(ups[up + 0] - '0').east(1).drop()
                .north(ups[up + 1] - '0').east(1).drop()
                .north(ups[up + 2] - '0').east(1).drop()
                .north(ups[up + 3] - '0').east(1).drop()
                .north(ups[up + 4] - '0').east(1)
                .south(downs[down + 0] - '0').east(1)
                .south(downs[down + 1] - '0').east(1)
                .south(downs[down + 2] - '0').east(1)
                .south(downs[down + 3] - '0').east(1)
                .north(10)
                .west(10)
                .south(10)
                .placeKarel()
        }

        val findTeddyBear = Problem(
            "2.3.2",
            "findTeddyBear",
            "In the middle of the night, Karel\nawakens from a terrible dream.\nThe teddy bear will provide\ncomfort. It should lay somewhere\nnear the edge of the bed...",
            "\u0007\ud108\u000a\uc106\u0001\ub100\u0002\ub100\u0000",
            CheckAfter.FINISH,
            0,
            16000.toBigInteger(),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = fenced()

            val xy = rng.nextInt(10)
            when (rng.nextInt(4)) {
                EAST -> builder.drop(9, xy)
                WEST -> builder.drop(0, xy)
                NORTH -> builder.drop(xy, 0)
                SOUTH -> builder.drop(xy, 9)
            }
            val x = rng.nextInt(10)
            val y = rng.nextInt(10)
            val dir = rng.nextInt(4)

            builder
                .placeKarel(x, y, dir)
        }

        val jumpTheHurdles = Problem(
            "2.3.3",
            "jumpTheHurdles",
            "Karel signs up for the Olympics\nand is allowed to participate\nin the hurdle runs. After jumping\nall the hurdles, Karel receives a\nspecial medal made of copper!",
            "\u0007\ud113\u000a\uc106\u0001\ub100\u0002\u0001\u000b\uc107\u0004\u0001\u0004\u0001\u000a\ud10d\u0002\u0007\uc102\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            1111100000.toBigInteger(),
        ) {
            val xBeeper = 5 + Random.nextInt(5)
            val builder = fenced()

            for (x in 1..xBeeper) {
                builder.spawn(x, 10).north(Random.nextInt(10))
            }

            builder
                .drop(xBeeper, 9)
                .placeKarel()
        }

        val solveTheMaze = Problem(
            "2.4.1",
            "solveTheMaze",
            "Study the random mazes carefully.\nThey contain both crossroads and\ndead ends, but no loops. Maintain\ncontact with Karel's left wall\nand you should find the beeper!",
            "\u0007\ud11a\u0009\uc109\u0002\u0001\u0007\uc102\u0000\u000a\uc10f\u0001\u0007\uc102\u0000\u000b\uc116\u0004\u0001\u0007\uc102\u0000\u0003\u0001\u0007\uc102\u0000",
            CheckAfter.FINISH,
            0,
            UNKNOWN,
        ) {
            TODO()
        }

        val quantizeBits = Problem(
            "2.4.2",
            "quantizeBits",
            "Karel the hacker is eavesdropping\non an analog communications line\nand writes down 10 bits encoded\nas 0..5 (0) or 6..10 (1). Convert\nto always 0 (0) or always 10 (1).",
            "\u8009\ua104\u0001\u9101\u0007\uc12a\u0002\u0001\u0001\u0001\u0001\u0001\u0007\uc11f\u0008\uc115\u0001\u0008\ud110\u000a\uc119\u0001\u0006\u000a\ud115\u0003\u0001\u000a\ud11a\u0002\u0000\u0003\u0008\ud125\u0001\u0008\uc122\u0001\u0005\u0008\ud125\u0002\u0000",
            CheckAfter.BEEPER,
            0,
            11.toBigInteger().pow(10),
        ) {
            pillars()
        }

        val addFast = Problem(
            "2.4.3",
            "addFast",
            "Karel adds two bytes from the\n1st and 2nd row and stores the\nsum in the 4th row. The 3rd row\nis reserved for the carry bits.\n(Does \"carry the 1\" ring a bell?)",
            "\u8008\u0007\u0001\u0007\u0001\u0007\u0001\u0004\ud114\ud115\uc10c\u0006\u0001\u0004\u0001\u0001\u0001\u0003\u9101\u0000\ud117\ud119\ub10b\uc119\u0006\u0001\u0004\u0001\u0006\u0001\u0001\u0003\u9101\u0000",
            CheckAfter.BEEPER,
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
            CheckAfter.BEEPER_MOVE,
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            party(WorldEntropy(id), 8)
        }

        val fetchTheStars = Problem(
            "3.1.2",
            "fetchTheStars",
            "Karel arranges a romantic date\nwith Taylor on a frozen lake to\n\"fetch the stars from the sky\",\nwhich is German for \"goes to\nthe ends of the world and back\".",
            "\u8009\ua104\u0001\u9101\u0002\ua109\u0006\u0002\u0000\u000a\ud10e\u0005\u0003\u0000\u0001\ua109\u0001\u0000",
            CheckAfter.BEEPER_MOVE,
            0,
            3.toBigInteger().pow(10),
        ) { id ->
            val rng = WorldEntropy(id)
            val builder = fenced().cellSouthWest()

            for (x in 0..9) {
                val y = 1 + rng.nextInt(3)
                builder.spawn(x, y).east(1).drop()
            }

            builder
                .placeKarel(0, 8)
        }

        val secureTheCave = Problem(
            "3.2.1",
            "secureTheCave",
            "Karel the cave explorer earns a\nliving as a tourist guide. For\nsafety measures, Karel breaks all\nstalactites from the ceiling and\nre-erects them as stalagmites.",
            "\u8009\ua104\u0001\u9101\u0002\ua109\ua10e\u0004\u0000\u0001\u000a\ud109\u0003\u0000\u0007\uc109\u0005\u0001\ua10e\u0006\u0001\u0000",
            CheckAfter.BEEPER,
            0,
            9.toBigInteger().pow(10),
        ) {
            val builder = fenced().cellSouthEast()

            for (x in 0..9) {
                val y = 1 + Random.nextInt(3)
                val n = 1 + Random.nextInt(3)
                builder.spawn(x, y).east(1)
                repeat(n) {
                    builder.spawn(x, y + it).drop()
                }
            }

            builder
                .placeKarel()
        }

        val layAndRemoveTiles = Problem(
            "3.2.2",
            "layAndRemoveTiles",
            "Karel tries a different set of\nflagstones. But again, Taylor\nis not enamored with the result.\nHence Karel immediately removes\nthe flagstones, in reverse order.",
            "\u0007\uc104\u0003\u0000\u0006\u0008\ud10e\u000a\uc10e\u0001\ua100\u0001\u0005\u0000\u0002\u0001\ua100\u0001\u0004\u0005\u0000",
            CheckAfter.BEEPER,
            0,
            ONE,
        ) {
            fenced()
                .placeKarel()
        }

        val findShelters = Problem(
            "3.3.1",
            "findShelters",
            "Karel is part of an expedition to\nthe north pole. The first task is\nfinding storm-proof shelters.\nMark Karel's path with beepers,\nbut leave the shelters empty!",
            "\u8004\u0008\ud111\u000a\uc111\u0001\u0009\ud10c\u000a\ud10c\u000b\uc10e\u0006\ua100\u0003\u0001\u0003\u0002\u9101\u0000",
            CheckAfter.FINISH,
            0,
            UNKNOWN,
        ) {
            val builder = fenced()

            repeat(25) {
                builder.spawn(Random.nextInt(10), 1 + Random.nextInt(9)).east(1)
                builder.spawn(1 + Random.nextInt(9), Random.nextInt(10)).south(1)
            }
            val x = Random.nextInt(10)
            val y = Random.nextInt(10)
            val dir = Random.nextInt(4)

            builder
                .placeKarel(x, y, dir)
        }

        val addSmart = Problem(
            "3.3.2",
            "addSmart",
            "Karel adds two bytes from the\n1st and 2nd row and stores the\nsum in the 3rd row. Dropping and\nchecking carry bits is no longer\nnecessary. What a smart robot!",
            "\ub10c\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u000b\uc129\u0007\ud114\u0008\ud101\u0004\u0001\u0002\ub10a\u0008\uc101\u0004\u0001\u0002\u000b\uc129\u0007\uc114\u0008\uc116\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\ub119\u0000",
            CheckAfter.BEEPER,
            0b111,
            TWO.pow(16),
        ) { id ->
            randomBytes(WorldEntropy(id), SOUTH)
        }

        val computeFibonacci = Problem(
            "3.3.3",
            "computeFibonacci",
            "Given 2 Fibonacci numbers,\nKarel computes the next 8.\n\nen.wikipedia.org/wiki/Fibonacci_number\nde.wikipedia.org/wiki/Fibonacci-Folge",
            "\u8008\ub10d\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\u000b\uc12a\u0007\ud115\u0008\ud102\u0004\u0001\u0002\ub10b\u0008\uc102\u0004\u0001\u0002\u000b\uc12a\u0007\uc115\u0008\uc117\u0001\u0001\u0006\u0004\u0001\u0004\u0001\u0001\u0003\ub11a\u0001\u0002\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0004\u910d\u0000",
            CheckAfter.BEEPER,
            0b1111111111,
            5.toBigInteger(),
        ) { id ->
            val builder = binary()

            when (id % 5) {
                0 -> builder.drop(9, 1) // 0 1
                1 -> builder.drop(9, 0).drop(9, 1) // 1 1
                2 -> builder.drop(9, 0).drop(8, 1) // 1 2
                3 -> builder.drop(8, 0).drop(8, 1).drop(9, 1) // 2 3
                4 -> builder.drop(8, 0).drop(9, 0).drop(7, 1).drop(9, 1) // 3 5

                else -> error(id)
            }

            builder
                .placeKarel(9, 0, SOUTH)
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
