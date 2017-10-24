package logic

fun storyFor(problemName: String): String {
    return when (problemName) {
        "karelsFirstProgram" -> "Click the GOAL button (top left)\nand watch Karel go. Drag slider\nto adjust animation speed.\nCan you program Karel to perform\nthe same steps? Test with START!"

        "obtainArtefact" -> "Karel auditions for the new Indy\nmovie. To demonstrate his talent,\nKarel re-enacts the classic scene\nwhere Indy saves some valuable\nartefact from an ancient temple."
        "defuseOneBomb" -> "Karel the demolition expert\ndefuses a bomb at the other end\nof the room and returns filled\nwith pride and self-confidence:\nHe did it without duplicate code!"
        "defuseTwoBombs" -> "One bomb is no problem for Karel.\nLet's spice up the challenge!\nShouldn't this be rather simple,\ngiven that Karel already knows\nhow to defuse one single bomb?"
        "practiceHomerun" -> "Karel's heart burns for baseball,\nbut he has become bored of just\nwatching. Tonight, he will sneak\ninto the stadium and perform his\nfirst homerun. Adrenaline rush!"

        "climbTheStairs" -> "Karel's elevator seems to be\nout of service as of late...\nBut Karel is still pumped from\nthe homerun and full of energy!"
        "fillTheHoles" -> "Karel considers a career in den-\ndistry. The local dental school\nhas Open House day. Coincidence?\nKarel gets to fill 4 carious\nteeth with dental amalgam. Ouch!"
        "saveTheFlower" -> "During a vacation in the alps,\nKarel discovers a rare flower\nwhich has trouble blooming\nat such low altitude...\nIt's a long way to the top!"
        "mowTheLawn" -> "Karel promised his aunt to help\nin the garden. She has already\npulled up the weeds, so Karel\ncan focus on mowing the lawn."

        "harvestTheField" -> "Karel's uncle is an agricult --\nerm... farmer. Having helped his\naunt, Karel can't reject the des-\nperate plea for help on the farm.\nThe wheat is already overripe!"
        "repairTheStreet" -> "Load this problem again. Notice\nsomething? Not all streets are\ncreated equal! Have you learned\nabout the if/else statement yet?\nF7..F11 are Karel's conditions."
        "cleanTheRoom" -> "Karel's parents are paying him\na surprise visit. His apartment\nis *really* out of shape :(\nThe chaos is almost overwhelming.\nCan you help him clean up?"
        "tileTheFloor" -> "During his routine visit to the\nhardware store, Karel can't\nresist buying some flagstones.\nThey seem to be the perfect fit\nfor his luxurious bathroom!"

        "stealOlympicFire" -> "Karel is mad with olympic fever\nand somehow his CPU decided\nit would be a good idea to\nsteal the olympic fire O_o\nLet's hope nobody will notice..."
        "removeTheTiles" -> "The flagstones were supposed to\nbe a surprise for Karel's new\nsweetheart, Karoline. Too bad\ngreen is not her favourite color.\nOh well, back to square one..."
        "walkTheLabyrinth" -> "Load this problem several times.\nNote how the generated labyrinths\nare rather simple? They contain\nneither crossroads nor dead ends.\nExactly one path to the beeper!"

        "hangTheLampions" -> "Today is Karel's birthday! To\ncelebrate this special occasion,\nKarel bought 10 lampions. Now all\nthat's left to do is hang them\nfrom his (irregular) ceiling."
        "followTheSeeds" -> "Karel had insomnia and decided\nto take a walk in the forest.\nBeing the smart robot that he is,\nhe always leaves a trail of seeds\nso he can find his way back..."
        "cleanTheTunnels" -> "Karel the coal miner discovers\nten tunnels of varying lengths\nfilled with valuable coal.\n(Does your solution work for\ntunnels of length 0 and 10?)"

        "increment" -> "128  64  32  16   8   4   2   1\n\nDo you know binary numbers?\nKarel wants to add 1 to a number.\nThis is almost trivial in binary."
        "decrement" -> "Karel wants to subtract 1 from\na number. Notice any similarity\nto increment? (What happens if\nKarel decrements the number zero?\nYou can click in Karel's word!)"
        "addSlow" -> "Welcome to the slowest adding\nmachine in the world! Karel just\ndecrements the first number\nand increments the second number\nuntil the first number is zero."

        "saveTheFlowers" -> "Karel climbs Mt. Everest. On his\nway up, he collects four flowers\nthat do not get enough sunlight\non the west side of the mountain.\nEast is where the sun comes up!"
        "findTeddyBear" -> "In the middle of the night, Karel\nawakens from a terrible dream.\nHis teddy bear will give him\ncomfort. It should lay somewhere\nnear the edge of the bed..."
        "jumpTheHurdles" -> "Karel signs up for the Olympics\nand is allowed to participate\nin the hurdle runs. After jumping\nall the hurdles, he receives a\nspecial medal made of copper!"

        "solveTheMaze" -> "Study the random mazes carefully.\nThey contain crossroads and dead\nends, but no loops. So \"always\nkeep left\" or \"always keep right\"\nshould lead Karel to the beeper!"
        "quantize" -> "Karel the hacker is eavesdropping\non an analog communications line\nand writes down 10 bits encoded\nas 0..5 (0) or 6..10 (1). Convert\nto always 0 (0) or always 10 (1)."
        "addFast" -> "Karel adds two numbers from the\n1st and 2nd row and stores the\nsum in the 4th row. The 3rd row\nis reserved for the carry bits.\n(Does \"carry the 1\" ring a bell?)"

        "partyAgain" -> "Karel is preparing the next big\nparty. Unfortunately, the floor\nis so soaked from the last party\nthat he must be careful not to\nbreak through into the cellar!"
        "fetchTheStars" -> "Karel arranges a romantic date\nwith Karoline on a frozen lake\nwhere he \"fetches the stars from\nthe sky\" (German for \"goes to the\nends of the world and back\")."

        "secureTheCave" -> "Karel the cave explorer earns a\nliving as a tourist guide. For\nsafety measures, he breaks all\nstalactites from the ceiling and\nre-erects them as stalagmites."
        "layAndRemoveTiles" -> "Karel tries a different set of\nflagstones. But again, Karoline\nis not enamored with the outcome.\nThis time he immediately removes\nthe flagstones, in reverse order."

        "findShelters" -> "Karel is part of an expedition to\nthe north pole. His first task is\nfinding storm-proof shelters.\nMark Karel's path with beepers,\nbut leave the shelters empty!"

        else -> "The story for this problem\nhas not been translated yet.\n\nLook at the problem title\nand use your imagination!"
    }
}
