package syntax.parser

import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.program(): Program {
    return Program(list1Until(END_OF_INPUT, ::command))
}

fun Parser.command(): Command {
    if (current == CLOSING_BRACE) token.error("Too many closing braces.\nDid you forget a { somewhere?")
    if (current != VOID) illegalStartOf("command definition")

    return Command(accept(), expect(IDENTIFIER).emptyParens(), block())
}

fun Parser.block(): Block {
    return Block(expect(OPENING_BRACE), list0Until(CLOSING_BRACE, ::statement), accept())
}

fun Parser.statement(): Statement = when (current) {
    IDENTIFIER -> Call(accept().emptyParens()).semicolon()

    REPEAT -> Repeat(accept(), parenthesized { expect(NUMBER).toInt(2..4095) }, block())
    WHILE -> While(accept(), parenthesized(::disjunction), block())
    IF -> IfThenElse(accept(), parenthesized(::disjunction), block(), optional(ELSE) {
        when (current) {
            OPENING_BRACE -> block()
            IF -> statement()
            else -> token.error("else must be followed by { or if")
        }
    })

    VOID -> {
        val void = accept()
        expect(IDENTIFIER).emptyParens()
        when (current) {
            OPENING_BRACE -> void.error("Command definitions cannot be nested.\nDid you forget a } somewhere?")
            else -> void.error("Command calls have no void before the command name")
        }
    }
    END_OF_INPUT -> token.error("End of file encountered in an unclosed block.\nDid you forget a } somewhere?")
    else -> illegalStartOf("statement")
}
