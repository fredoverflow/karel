package vm

import common.Diagnostic

typealias Instructions = Array<Instruction>

class Instruction(var operation: Byte, var operand: Int, val position: Int) {

    var label: Label? = null

    fun resolveLabel() {
        label?.run {
            operand = address
            label = null
        }
    }

    val compiledFromSource: Boolean
        get() = position > 0

    fun shouldPause(): Boolean = when (+operation) {

        +MOVE_FORWARD, +TURN_LEFT, +TURN_AROUND, +TURN_RIGHT, +PICK_BEEPER, +DROP_BEEPER -> true

        +ON_BEEPER, +BEEPER_AHEAD, +LEFT_IS_CLEAR, +FRONT_IS_CLEAR, +RIGHT_IS_CLEAR,
        +PUSH, +LOOP, +CALL, +RETURN -> compiledFromSource

        +JUMP, +ELSE, +THEN -> false

        else -> throw IllegalOperation(operation)
    }

    override fun toString(): String = when (+operation) {
        +RETURN -> "RET"

        +MOVE_FORWARD -> "MOVE"
        +TURN_LEFT -> "TRNL"
        +TURN_AROUND -> "TRNA"
        +TURN_RIGHT -> "TRNR"
        +PICK_BEEPER -> "PICK"
        +DROP_BEEPER -> "DROP"

        +ON_BEEPER, +ON_BEEPER_INSTRUMENT, +ON_BEEPER_FALSE, +ON_BEEPER_TRUE -> "BEEP"
        +BEEPER_AHEAD, +BEEPER_AHEAD_INSTRUMENT, +BEEPER_AHEAD_FALSE, +BEEPER_AHEAD_TRUE -> "HEAD"
        +LEFT_IS_CLEAR, +LEFT_IS_CLEAR_INSTRUMENT, +LEFT_IS_CLEAR_FALSE, +LEFT_IS_CLEAR_TRUE -> "LCLR"
        +FRONT_IS_CLEAR, +FRONT_IS_CLEAR_INSTRUMENT, +FRONT_IS_CLEAR_FALSE, +FRONT_IS_CLEAR_TRUE -> "FCLR"
        +RIGHT_IS_CLEAR, +RIGHT_IS_CLEAR_INSTRUMENT, +RIGHT_IS_CLEAR_FALSE, +RIGHT_IS_CLEAR_TRUE -> "RCLR"

        +PUSH -> "PUSH %02x".format(operand)
        +LOOP -> "LOOP %02x".format(operand)
        +CALL -> "CALL %02x".format(operand)
        +JUMP -> "JUMP %02x".format(operand)
        +ELSE -> "ELSE %02x".format(operand)
        +THEN -> "THEN %02x".format(operand)

        else -> throw IllegalOperation(operation)
    }
}

const val RETURN: Byte = 0x00

const val MOVE_FORWARD: Byte = 0x01
const val TURN_LEFT: Byte = 0x02
const val TURN_AROUND: Byte = 0x03
const val TURN_RIGHT: Byte = 0x04
const val PICK_BEEPER: Byte = 0x05
const val DROP_BEEPER: Byte = 0x06

const val ON_BEEPER: Byte = 0x07
const val BEEPER_AHEAD: Byte = 0x08
const val LEFT_IS_CLEAR: Byte = 0x09
const val FRONT_IS_CLEAR: Byte = 0x0a
const val RIGHT_IS_CLEAR: Byte = 0x0b

const val PUSH: Byte = 0x1a
const val LOOP: Byte = 0x1b
const val CALL: Byte = 0x1c
const val JUMP: Byte = 0x1d
const val ELSE: Byte = 0x1e
const val THEN: Byte = 0x1f

const val ON_BEEPER_INSTRUMENT: Byte = 0x20
const val BEEPER_AHEAD_INSTRUMENT: Byte = 0x21
const val LEFT_IS_CLEAR_INSTRUMENT: Byte = 0x22
const val FRONT_IS_CLEAR_INSTRUMENT: Byte = 0x23
const val RIGHT_IS_CLEAR_INSTRUMENT: Byte = 0x24

const val ON_BEEPER_FALSE: Byte = 0x25
const val BEEPER_AHEAD_FALSE: Byte = 0x26
const val LEFT_IS_CLEAR_FALSE: Byte = 0x27
const val FRONT_IS_CLEAR_FALSE: Byte = 0x28
const val RIGHT_IS_CLEAR_FALSE: Byte = 0x29

const val ON_BEEPER_TRUE: Byte = 0x2a
const val BEEPER_AHEAD_TRUE: Byte = 0x2b
const val LEFT_IS_CLEAR_TRUE: Byte = 0x2c
const val FRONT_IS_CLEAR_TRUE: Byte = 0x2d
const val RIGHT_IS_CLEAR_TRUE: Byte = 0x2e

val builtinCommands = mapOf(
    "moveForward" to MOVE_FORWARD,
    "turnLeft" to TURN_LEFT,
    "turnAround" to TURN_AROUND,
    "turnRight" to TURN_RIGHT,
    "pickBeeper" to PICK_BEEPER,
    "dropBeeper" to DROP_BEEPER,
)

fun String.toInstructions(): Instructions {
    return Instructions(this.length) { index ->
        val char = this[index].code
        val hi = char ushr 8
        val lo = char and 255
        if (hi != 0) {
            Instruction(hi.toByte(), lo, 0)
        } else {
            Instruction(lo.toByte(), 0, 0)
        }
    }
}

fun Instructions.toBytecode(): ByteArray {
    val bytecode = ByteArray(size * 2)
    var index = 0
    for (instruction in this) {
        val operation = instruction.operation
        bytecode[index++] = operation
        if (operation >= PUSH) {
            bytecode[index++] = instruction.operand.toByte()
        }
    }
    return bytecode.copyOf(index)
}

fun Instruction.error(message: String) {
    throw Diagnostic(position, message)
}
