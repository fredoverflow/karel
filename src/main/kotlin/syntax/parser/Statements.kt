package syntax.parser

import syntax.lexer.TokenKind.*
import syntax.tree.*

const val EXPECTED_VOID = """Command definitions look like this:

void commandNameHere()
{
    // your code here

}"""

private var currentCommandName = ""

fun Parser.program(): Program {
    if (current != VOID) token.error(EXPECTED_VOID)

    return sema(Program(list1Until(END_OF_INPUT, ::command)))
}

fun Parser.command(): Command {
    val previousClosingBrace = previous

    return when (current) {
        VOID -> {
            val void = accept()
            val identifier = expect(IDENTIFIER).emptyParens()
            currentCommandName = identifier.lexeme
            sema(Command(void, identifier, block()))
        }

        CLOSING_BRACE -> token.error("remove }\n\nThis closing brace has no opening partner")

        REPEAT, WHILE, IF -> previousClosingBrace.error("|\n|\n|\nremove }\n\nThe command ${sema.previousCommandName()}() ends here,\nbut the following $current-statement\nstill belongs inside a command")

        IDENTIFIER -> {
            val identifier = accept().emptyParens()
            when (current) {
                SEMICOLON -> previousClosingBrace.error("|\n|\n|\nremove }\n\nThe command ${sema.previousCommandName()}() ends here,\nbut the following call ${identifier.lexeme}();\nstill belongs inside a command")

                else -> identifier.error(EXPECTED_VOID)
            }
        }

        else -> token.error(EXPECTED_VOID)
    }
}

fun Parser.block(): Block {
    return Block(expect(OPENING_BRACE), list0Until(CLOSING_BRACE, ::statement), accept())
}

fun Parser.statement(): Statement = when (current) {
    IDENTIFIER -> sema(Call(accept().emptyParens()).semicolon())

    REPEAT -> Repeat(accept(), parenthesized { expect(NUMBER).toInt(2..4095) }, block())

    WHILE -> While(accept(), parenthesized(::disjunction), block())

    IF -> ifThenElse()

    VOID -> {
        val void = accept()
        val identifier = expect(IDENTIFIER).emptyParens()
        when (current) {
            OPENING_BRACE -> void.error("missing }\n\nCannot define command ${identifier.lexeme}()\nINSIDE command $currentCommandName()\n\nCommand definitions do not nest")

            else -> void.error("remove void\n\nYou want to CALL ${identifier.lexeme}(), not DEFINE it, right?")
        }
    }

    END_OF_INPUT -> token.error("missing }")

    else -> illegalStartOf("statement")
}

fun Parser.ifThenElse(): IfThenElse {
    return IfThenElse(expect(IF), parenthesized(::disjunction), block(), optional(ELSE) {
        when (current) {
            OPENING_BRACE -> block()

            IF -> ifThenElse()

            else -> previous.error("else must be followed by { or if")
        }
    })
}
