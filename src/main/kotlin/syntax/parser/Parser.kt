package syntax.parser

import common.Diagnostic
import syntax.lexer.Lexer
import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.lexer.TokenKind.*

class Parser(private val lexer: Lexer) {
    private var previousEnd: Int = 0

    var token: Token = lexer.nextToken()
        private set

    var current: TokenKind = token.kind
        private set

    var lookahead: Token = lexer.nextToken()
        private set

    fun next(): TokenKind {
        previousEnd = token.end
        token = lookahead
        current = token.kind
        lookahead = lexer.nextToken()
        return current
    }

    fun accept(): Token {
        val result = token
        next()
        return result
    }

    fun expect(expected: TokenKind): Token {
        if (current != expected) throw Diagnostic(previousEnd, "missing $expected")
        return accept()
    }

    fun <T> T.emptyParens(): T {
        expect(OPENING_PAREN)
        expect(CLOSING_PAREN)
        return this
    }

    fun <T> T.semicolon(): T {
        expect(SEMICOLON)
        return this
    }

    fun illegalStartOf(rule: String): Nothing {
        token.error("illegal start of $rule")
    }

    inline fun <T> list1While(proceed: () -> Boolean, parse: () -> T): List<T> {
        val list = mutableListOf(parse())
        while (proceed()) {
            list.add(parse())
        }
        return list
    }

    inline fun <T> list0While(proceed: () -> Boolean, parse: () -> T): List<T> {
        return if (!proceed()) {
            emptyList()
        } else {
            list1While(proceed, parse)
        }
    }

    inline fun <T> list1Until(terminator: TokenKind, parse: () -> T): List<T> {
        return list1While({ current != terminator }, parse)
    }

    inline fun <T> list0Until(terminator: TokenKind, parse: () -> T): List<T> {
        return list0While({ current != terminator }, parse)
    }

    inline fun <T> parenthesized(parse: () -> T): T {
        expect(OPENING_PAREN)
        val result = parse()
        expect(CLOSING_PAREN)
        return result
    }

    inline fun <T> optional(indicator: TokenKind, parse: () -> T): T? {
        return if (current != indicator) {
            null
        } else {
            next()
            parse()
        }
    }

    val sema = Sema()
}
