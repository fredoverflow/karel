package parsing

class Token(val kind: TokenKind, val position: Int, val lexeme: String) {

    val end: Int
        get() = position + lexeme.length

    fun error(message: String): Nothing {
        throw Diagnostic(position, message)
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
