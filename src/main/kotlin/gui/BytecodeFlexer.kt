package gui

import common.puts
import freditor.FlexerState
import freditor.FlexerState.EMPTY
import freditor.FlexerState.THIS
import freditor.FlexerStateBuilder

object BytecodeFlexer : freditor.Flexer() {
    private val NUMBER_TAIL = FlexerState("09af", THIS)
    private val NUMBER_HEAD = NUMBER_TAIL.head()

    private val START = FlexerStateBuilder()
        .set('\n', NEWLINE)
        .set(' ', SPACE_HEAD)
        .set("09af", NUMBER_HEAD)
        .build()
        .verbatim(
            EMPTY, "@", "CODE", "MNEMONIC",
            "RET",
            "MOVE", "TRNL", "TRNA", "TRNR", "PICK", "DROP",
            "BEEP", "HEAD", "LCLR", "FCLR", "RCLR",
            "PUSH", "LOOP", "CALL", "JUMP", "ELSE", "THEN",
        )
        .setDefault(ERROR)

    override fun start(): FlexerState = START

    override fun pickColorForLexeme(previousState: FlexerState, endState: FlexerState): Int {
        val colors = if (previousState === NEWLINE) afterNewline else lexemeColors
        return colors[endState] ?: 0x000000
    }

    private val lexemeColors = hashMapOf(ERROR to 0x808080)
        .puts(NUMBER_HEAD, NUMBER_TAIL, 0x6400c8)
        .puts(START.read("@", "CODE", "MNEMONIC"), 0x808080)
        .puts(START.read("BEEP", "HEAD", "LCLR", "FCLR", "RCLR", "PUSH"), 0x000080)
        .puts(START.read("RET", "LOOP", "CALL", "JUMP", "ELSE", "THEN"), 0x400000)

    private val afterNewline = HashMap(lexemeColors)
        .puts(NUMBER_HEAD, NUMBER_TAIL, 0x808080)
}
