package parsing

class Parser(lexer: Lexer) : ParserBase(lexer) {

    fun program(): Program {
        val commands = atLeastOneUntil(END_OF_INPUT, this::command)
        return Program(commands)
    }

    fun command(): Command {
        if (current == CLOSING_BRACE) token.error("Too many closing braces.\nDid you forget a { somewhere?")
        val void = startLineWith(VOID)
        val commandName = continueLineWith(IDENTIFIER)
        continueLineWith(OPENING_PAREN)
        continueLineWith(CLOSING_PAREN)
        val commandBody = block()
        return Command(void, commandName, commandBody)
    }

    fun block(): Block {
        val openingBrace = startLineWith(OPENING_BRACE)
        val statements = zeroOrMoreUntil(CLOSING_BRACE, this::statement)
        val closingBrace = startLineWith(CLOSING_BRACE)
        return Block(openingBrace, statements, closingBrace)
    }

    fun statement(): Statement {
        return when (current) {
            IDENTIFIER -> callStatement()
            REPEAT -> repeatStatement()
            IF -> ifStatement()
            WHILE -> whileStatement()
            VOID -> token.error("Commands cannot be nested.\nDid you forget a } somewhere?")
            END_OF_INPUT -> token.error("End of file encountered in an unclosed block.\nDid you forget a } somewhere?")
            else -> token.error("Illegal start of statement")
        }
    }

    fun callStatement(): Statement {
        val target = startLineWith(IDENTIFIER)
        continueLineWith(OPENING_PAREN)
        continueLineWith(CLOSING_PAREN)
        continueLineWith(SEMICOLON)
        return Call(target)
    }

    fun repeatStatement(): Statement {
        val repeat = startLineWith(REPEAT)
        continueLineWith(OPENING_PAREN)
        val str = continueLineWith(NUMBER).lexeme
        try {
            val times = Integer.parseInt(str, 10)
            if (times < 2) token.error("$str < 2")
            if (times > 4095) token.error("$str > 4095")
            continueLineWith(CLOSING_PAREN)
            val body = block()
            return Repeat(repeat, times, body)
        } catch (ex: NumberFormatException) {
            token.error("$str > 4095")
        }
    }

    fun ifStatement(): Statement {
        val iF = startLineWith(IF)
        val condition = parenthesized(this::disjunction)
        val th3n = block()
        return if (current != ELSE) {
            IfThen(iF, condition, th3n)
        } else {
            val e1se = when (next()) {
                OPENING_BRACE -> block()
                IF -> ifStatement()
                else -> token.error("else must be followed by { or if")
            }
            IfThenElse(iF, condition, th3n, e1se)
        }
    }

    fun whileStatement(): Statement {
        val whi1e = startLineWith(WHILE)
        val condition = parenthesized(this::disjunction)
        val body = block()
        return While(whi1e, condition, body)
    }

    fun disjunction(): Condition {
        val left = conjunction()
        return if (current != BAR_BAR) {
            left
        } else {
            val or = continueLineWith(BAR_BAR)
            val right = disjunction()
            Disjunction(left, or, right)
        }
    }

    fun conjunction(): Condition {
        val left = primaryCondition()
        return if (current != AMPERSAND_AMPERSAND) {
            left
        } else {
            val and = continueLineWith(AMPERSAND_AMPERSAND)
            val right = conjunction()
            Conjunction(left, and, right)
        }
    }

    fun primaryCondition(): Condition {
        return when (current) {
            FALSE -> nextYield(False(token))
            TRUE -> nextYield(True(token))

            ON_BEEPER -> followedByParens(OnBeeper(token))
            BEEPER_AHEAD -> followedByParens(BeeperAhead(token))
            LEFT_IS_CLEAR -> followedByParens(LeftIsClear(token))
            FRONT_IS_CLEAR -> followedByParens(FrontIsClear(token))
            RIGHT_IS_CLEAR -> followedByParens(RightIsClear(token))

            BANG -> Not(continueLineWith(BANG), primaryCondition())

            OPENING_PAREN -> parenthesized(this::disjunction)

            else -> token.error("Illegal start of condition")
        }
    }

    private fun followedByParens(condition: Condition): Condition {
        next()
        continueLineWith(OPENING_PAREN)
        continueLineWith(CLOSING_PAREN)
        return condition
    }
}
