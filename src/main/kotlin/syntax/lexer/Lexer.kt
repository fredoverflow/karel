package syntax.lexer

import freditor.persistent.StringedValueMap
import syntax.lexer.TokenKind.*

class Lexer(input: String) : LexerBase(input) {

    tailrec fun nextToken(): Token {
        startAtIndex()
        return when (current) {
            ' ', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d' -> {
                next()
                nextToken()
            }

            '/' -> when (next()) {
                '/' -> {
                    skipSingleLineComment()
                    nextToken()
                }
                '*' -> {
                    skipMultiLineComment()
                    nextToken()
                }
                else -> error("comments start with // or /*")
            }

            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number()

            '(' -> nextVerbatim(OPENING_PAREN)
            ')' -> nextVerbatim(CLOSING_PAREN)
            ';' -> nextVerbatim(SEMICOLON)
            '{' -> nextVerbatim(OPENING_BRACE)
            '}' -> nextVerbatim(CLOSING_BRACE)

            '!' -> nextVerbatim(BANG)

            '&' -> {
                if (next() != '&') error("logical and is &&")
                nextVerbatim(AMPERSAND_AMPERSAND)
            }

            '|' -> {
                if (next() != '|') error("logical or is ||")
                nextVerbatim(BAR_BAR)
            }

            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> identifierOrKeyword()

            EOF -> verbatim(END_OF_INPUT)

            else -> error("illegal character $current")
        }
    }

    private fun skipSingleLineComment() {
        while (next() != '\n') {
            if (current == EOF) return
        }
        next() // skip '\n'
    }

    private fun skipMultiLineComment() {
        next() // skip '*'
        do {
            if (current == EOF) return
        } while ((current != '*') or (next() != '/'))
        next() // skip '/'
    }

    private tailrec fun number(): Token = when (next()) {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number()

        else -> token(NUMBER)
    }

    private tailrec fun identifierOrKeyword(): Token = when (next()) {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> identifierOrKeyword()

        else -> {
            val lexeme = lexeme()
            when (val value: Any? = identifiersOrKeywords[lexeme]) {
                is TokenKind -> verbatim(value)
                is String -> token(IDENTIFIER, value)
                else -> {
                    identifiersOrKeywords = identifiersOrKeywords.put(lexeme)
                    token(IDENTIFIER, lexeme)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private var identifiersOrKeywords = keywords as StringedValueMap<Any>
}
