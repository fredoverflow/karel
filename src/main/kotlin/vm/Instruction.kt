package vm

import freditor.persistent.ChampMap

class Instruction(var bytecode: Int, val position: Int) {

    val category: Int
        get() = bytecode.and(0xf000)

    val target: Int
        get() = bytecode.and(0x0fff)

    var label: Label? = null

    fun resolveLabel() {
        label?.apply {
            bytecode = category + address
            label = null
        }
    }

    private val compiledFromSource: Boolean
        get() = position > 0

    fun shouldPause(): Boolean {
        return when (bytecode) {
            RETURN -> compiledFromSource

            MOVE_FORWARD, TURN_LEFT, TURN_AROUND, TURN_RIGHT, PICK_BEEPER, DROP_BEEPER -> true

            ON_BEEPER, BEEPER_AHEAD, LEFT_IS_CLEAR, FRONT_IS_CLEAR, RIGHT_IS_CLEAR -> compiledFromSource

            else -> compiledFromSource && (category < JUMP)
        }
    }

    fun mnemonic(): String {
        return when (bytecode) {
            RETURN -> "RET"

            MOVE_FORWARD -> "MOVE"
            TURN_LEFT -> "TRNL"
            TURN_AROUND -> "TRNA"
            TURN_RIGHT -> "TRNR"
            PICK_BEEPER -> "PICK"
            DROP_BEEPER -> "DROP"

            ON_BEEPER -> "BEEP"
            BEEPER_AHEAD -> "HEAD"
            LEFT_IS_CLEAR -> "LCLR"
            FRONT_IS_CLEAR -> "FCLR"
            RIGHT_IS_CLEAR -> "RCLR"

            else -> when (category) {
                PUSH -> "PUSH %03x".format(target)
                LOOP -> "LOOP %03x".format(target)
                CALL -> "CALL %03x".format(target)

                JUMP -> "JUMP %03x".format(target)
                ELSE -> "ELSE %03x".format(target)
                THEN -> "THEN %03x".format(target)

                else -> throw IllegalBytecode(bytecode)
            }
        }
    }
}

const val RETURN = 0x0000

const val MOVE_FORWARD = 0x0001
const val TURN_LEFT = 0x0002
const val TURN_AROUND = 0x0003
const val TURN_RIGHT = 0x0004
const val PICK_BEEPER = 0x0005
const val DROP_BEEPER = 0x0006

const val ON_BEEPER = 0x0007
const val BEEPER_AHEAD = 0x0008
const val LEFT_IS_CLEAR = 0x0009
const val FRONT_IS_CLEAR = 0x000a
const val RIGHT_IS_CLEAR = 0x000b

const val NORM = 0x0000

const val PUSH = 0x8000
const val LOOP = 0x9000
const val CALL = 0xa000

const val JUMP = 0xb000
const val ELSE = 0xc000
const val THEN = 0xd000

val builtinCommands: ChampMap<String, Int> = ChampMap.of(
    "moveForward", MOVE_FORWARD,
    "turnLeft", TURN_LEFT,
    "turnAround", TURN_AROUND,
    "turnRight", TURN_RIGHT,
    "pickBeeper", PICK_BEEPER,
    "dropBeeper", DROP_BEEPER,
)

private val basicGoalInstructions = Array(RIGHT_IS_CLEAR + 1) { Instruction(it, 0) }

fun createInstructionBuffer(): MutableList<Instruction> {
    return MutableList(ENTRY_POINT) { basicGoalInstructions[RETURN] }
}

fun createGoalInstructions(goal: String): List<Instruction> {
    return goal.mapTo(createInstructionBuffer()) { char ->
        basicGoalInstructions.getOrElse(char.code) { code ->
            Instruction(code, 0)
        }
    }
}
