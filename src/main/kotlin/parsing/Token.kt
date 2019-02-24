package parsing

import freditor.persistent.StringedValueMap

typealias TokenKind = Byte

class Token(val kind: TokenKind, val position: Int, val lexeme: String) {

    val end: Int
        get() = position + lexeme.length

    fun error(message: String): Nothing {
        throw Diagnostic(position, message)
    }

    override fun toString(): String = kind.show()

    fun toInt(range: IntRange): Int {
        try {
            val n = lexeme.toInt()
            if (n in range) return n
        } catch (_: NumberFormatException) {
            // intentional fallthrough
        }
        error("$lexeme out of range $range")
    }
}

val lexemePool = arrayOf(
        "beeperAhead",
        "else",
        "false",
        "frontIsClear",
        "if",
        "leftIsClear",
        "onBeeper",
        "repeat",
        "rightIsClear",
        "true",
        "void",
        "while",
        // keywords come first
        "!", "&&", "(", ")", ";", "{", "||", "}",
        "number", "identifier", "end of file"
)

const val NUM_KEYWORDS = 12

class Keyword(val kind: TokenKind, val lexeme: String) {
    override fun toString(): String = lexeme
}

val keywords: StringedValueMap<Keyword> = lexemePool.take(NUM_KEYWORDS).foldIndexed(StringedValueMap.empty()) { index, map, lexeme ->
    map.put(Keyword(index.toByte(), lexeme))
}

fun TokenKind.show(): String = lexemePool[+this]

const val BEEPER_AHEAD: TokenKind = 0
const val ELSE: TokenKind = 1
const val FALSE: TokenKind = 2
const val FRONT_IS_CLEAR: TokenKind = 3
const val IF: TokenKind = 4
const val LEFT_IS_CLEAR: TokenKind = 5
const val ON_BEEPER: TokenKind = 6
const val REPEAT: TokenKind = 7
const val RIGHT_IS_CLEAR: TokenKind = 8
const val TRUE: TokenKind = 9
const val VOID: TokenKind = 10
const val WHILE: TokenKind = 11

const val BANG: TokenKind = 12
const val AMPERSAND_AMPERSAND: TokenKind = 13
const val OPENING_PAREN: TokenKind = 14
const val CLOSING_PAREN: TokenKind = 15
const val SEMICOLON: TokenKind = 16
const val OPENING_BRACE: TokenKind = 17
const val BAR_BAR: TokenKind = 18
const val CLOSING_BRACE: TokenKind = 19

const val NUMBER: TokenKind = 20
const val IDENTIFIER: TokenKind = 21
const val END_OF_INPUT: TokenKind = 22
