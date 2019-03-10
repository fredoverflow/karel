package syntax.lexer

import common.Diagnostic

class Token(val kind: TokenKind, val start: Int, val lexeme: String) {

    val end: Int
        get() = start + lexeme.length

    fun error(message: String): Nothing {
        throw Diagnostic(start, message)
    }

    override fun toString(): String = kind.toString()

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
