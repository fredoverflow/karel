package syntax.parser

import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.program(): Program {
    if (current != VOID) token.error("expected void")

    return sema(Program(list1Until(END_OF_INPUT, ::command)))
}

fun Parser.command(): Command = when (current) {
    VOID -> sema(Command(accept(), expect(IDENTIFIER).emptyParens(), block()))

    CLOSING_BRACE -> token.error("too many closing braces")

    REPEAT, WHILE, IF -> token.error("$current belongs inside command.\nDid you close too many braces?")

    IDENTIFIER -> {
        val identifier = accept().emptyParens()
        when (current) {
            SEMICOLON -> identifier.error("Command calls belong inside command.\nDid you close too many braces?")

            else -> identifier.error("expected void")
        }
    }

    else -> token.error("expected void")
}

fun Parser.block(): Block {
    return Block(expect(OPENING_BRACE), list0Until(CLOSING_BRACE, ::statement), accept())
}

fun Parser.statement(): Statement = when (current) {
    IDENTIFIER -> sema(Call(accept().emptyParens()).semicolon())

    REPEAT -> Repeat(accept(), parenthesized { expect(NUMBER).toInt(2..4095) }, block(), optional(INTERSPERSE, ::block))

    WHILE -> While(accept(), parenthesized(::disjunction), block())

    IF -> ifThenElse()

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

fun Parser.ifThenElse(): IfThenElse {
    return IfThenElse(expect(IF), parenthesized(::disjunction), block(), optional(ELSE) {
        when (current) {
            OPENING_BRACE -> block()

            IF -> ifThenElse()

            else -> token.error("else must be followed by { or if")
        }
    })
}
