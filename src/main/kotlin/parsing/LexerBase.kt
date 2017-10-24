package parsing

abstract class LexerBase(private val input: String) {

    protected var start: Int = -1
    protected var index: Int = -1
    protected var current: Char = next()

    protected fun next(): Char = nextOr('\u007f')

    protected fun nextOr(end: Char): Char {
        ++index
        current = if (index < input.length) input[index] else end
        return current
    }

    protected fun lexeme(): String {
        return input.substring(start, index)
    }

    protected fun token(kind: TokenKind): Token {
        return token(kind, lexeme())
    }

    protected fun token(kind: TokenKind, lexeme: String): Token {
        return Token(kind, start, lexeme)
    }

    protected fun pooled(kind: TokenKind): Token {
        return token(kind, kind.show())
    }

    protected fun nextPooled(kind: TokenKind): Token {
        next()
        return pooled(kind)
    }

    protected fun error(message: String): Nothing {
        throw Diagnostic(index, message)
    }
}
