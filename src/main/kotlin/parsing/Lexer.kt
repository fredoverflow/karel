package parsing

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
                    continueAfter('\n')
                    nextToken()
                }
                '*' -> {
                    continueAfter('*', '/')
                    nextToken()
                }
                else -> error("comments start with // or /*")
            }

            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number()

            '(' -> nextPooled(OPENING_PAREN)
            ')' -> nextPooled(CLOSING_PAREN)
            ';' -> nextPooled(SEMICOLON)
            '{' -> nextPooled(OPENING_BRACE)
            '}' -> nextPooled(CLOSING_BRACE)

            '!' -> nextPooled(BANG)

            '&' -> {
                if (next() != '&') error("logical and is &&")
                nextPooled(AMPERSAND_AMPERSAND)
            }

            '|' -> {
                if (next() != '|') error("logical or is ||")
                nextPooled(BAR_BAR)
            }

            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> identifierOrKeyword()

            EOF -> pooled(END_OF_INPUT)

            else -> error("illegal character $current")
        }
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
            val keyword: TokenKind? = keywords[lexeme]
            when {
                keyword != null -> pooled(keyword)
                else -> token(IDENTIFIER, lexeme.intern())
            }
        }
    }
}
