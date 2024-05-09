package gui

import freditor.FlexerState
import freditor.FlexerState.EMPTY
import freditor.FlexerState.THIS
import freditor.FlexerStateBuilder
import freditor.persistent.ChampMap

object Flexer : freditor.Flexer() {
    private val SLASH_SLASH = FlexerState('\n', null).setDefault(THIS)
    private val SLASH_ASTERISK___ASTERISK_SLASH = EMPTY.tail()
    private val SLASH_ASTERISK___ASTERISK = FlexerState('*', THIS, '/', SLASH_ASTERISK___ASTERISK_SLASH)
    private val SLASH_ASTERISK = FlexerState('*', SLASH_ASTERISK___ASTERISK).setDefault(THIS)

    init {
        SLASH_ASTERISK___ASTERISK.setDefault(SLASH_ASTERISK)
    }

    private val NUMBER_TAIL = FlexerState("09", THIS)
    private val NUMBER_HEAD = NUMBER_TAIL.head()

    val IDENTIFIER_TAIL = FlexerState("09AZ__az", THIS)
    private val IDENTIFIER_HEAD = IDENTIFIER_TAIL.head()

    private val START = FlexerStateBuilder()
        .set('(', OPENING_PAREN)
        .set(')', CLOSING_PAREN)
        .set('{', OPENING_BRACE)
        .set('}', CLOSING_BRACE)
        .set('\n', NEWLINE)
        .set(' ', SPACE_HEAD)
        .set('/', FlexerState('*', SLASH_ASTERISK, '/', SLASH_SLASH).head())
        .set("09", NUMBER_HEAD)
        .set("AZ__az", IDENTIFIER_HEAD)
        .build()
        .verbatim(IDENTIFIER_TAIL, "else", "intersperse", "if", "repeat", "void", "while")
        .verbatim(EMPTY, "!", "&&", ";", "||")
        .setDefault(ERROR)

    override fun start(): FlexerState = START

    override fun pickColorForLexeme(previousState: FlexerState, endState: FlexerState): Int {
        return lexemeColors[endState] ?: 0x000000
    }

    private val lexemeColors = ChampMap.of(ERROR, 0x808080)
        .put(START.read("/", "&", "|"), 0x808080)
        .put(SLASH_SLASH, SLASH_ASTERISK, SLASH_ASTERISK___ASTERISK, SLASH_ASTERISK___ASTERISK_SLASH, 0x008000)
        .put(NUMBER_HEAD, NUMBER_TAIL, 0x6400c8)
        .put(START.read("else", "intersperse", "if", "repeat", "while"), 0x0000ff)
        .put(START.read("void"), 0x008080)
        .put(START.read("(", ")", "{", "}"), 0xff0000)
        .put(START.read("!", "&&", "||"), 0x804040)
}
