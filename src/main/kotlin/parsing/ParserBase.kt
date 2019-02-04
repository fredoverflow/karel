package parsing

abstract class ParserBase(private val lexer: Lexer) {

    private var previousToken: Token = Token(END_OF_INPUT, 0, "")
    protected var token: Token = lexer.nextToken()
    protected var current: TokenKind = token.kind

    protected fun next(): TokenKind {
        previousToken = token
        token = lexer.nextToken()
        current = token.kind
        return current
    }

    protected fun startLineWith(expected: TokenKind): Token {
        if (current != expected) throw Diagnostic(token.position, "missing ${expected.show()}")
        next()
        return previousToken
    }

    protected fun continueLineWith(expected: TokenKind): Token {
        if (current != expected) throw Diagnostic(previousToken.end, "missing ${expected.show()}")
        next()
        return previousToken
    }

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun <T> nextYield(result: T): T {
        next()
        return result
    }

    protected inline fun <T> parenthesized(parse: () -> T): T {
        continueLineWith(OPENING_PAREN)
        val result = parse()
        continueLineWith(CLOSING_PAREN)
        return result
    }

    protected inline fun <T> atLeastOneUntil(terminator: TokenKind, parse: () -> T): List<T> {
        val list = ArrayList<T>()
        do {
            list.add(parse())
        } while (current != terminator)
        return list
    }

    protected inline fun <T> zeroOrMoreUntil(terminator: TokenKind, parse: () -> T): List<T> {
        return if (current == terminator) {
            emptyList()
        } else {
            atLeastOneUntil(terminator, parse)
        }
    }
}
