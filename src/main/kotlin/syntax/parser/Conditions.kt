package syntax.parser

import freditor.Levenshtein
import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.disjunction(): Condition {
    val left = conjunction()
    return if (current != BAR_BAR) {
        left
    } else {
        Disjunction(left, accept(), disjunction())
    }
}

fun Parser.conjunction(): Condition {
    val left = primaryCondition()
    return if (current != AMPERSAND_AMPERSAND) {
        left
    } else {
        Conjunction(left, accept(), conjunction())
    }
}

val PREDICATES = listOf("false", "true", "onBeeper", "beeperAhead", "leftIsClear", "frontIsClear", "rightIsClear")

fun Parser.primaryCondition(): Condition = when (current) {
    IDENTIFIER -> when (token.lexeme) {
        "false" -> False(accept())
        "true" -> True(accept())

        "onBeeper" -> OnBeeper(accept().emptyParens())
        "beeperAhead" -> BeeperAhead(accept().emptyParens())
        "leftIsClear" -> LeftIsClear(accept().emptyParens())
        "frontIsClear" -> FrontIsClear(accept().emptyParens())
        "rightIsClear" -> RightIsClear(accept().emptyParens())

        else -> {
            val bestMatches = Levenshtein.bestMatches(token.lexeme, PREDICATES)
            if (bestMatches.size == 1) {
                val bestMatch = bestMatches.first()
                val prefix = bestMatch.commonPrefixWith(token.lexeme)
                token.error("Did you mean $bestMatch?", prefix.length)
            } else {
                val commaSeparated = bestMatches.joinToString(", ")
                token.error("Did you mean $commaSeparated?")
            }
        }
    }

    BANG -> Not(accept(), primaryCondition())

    OPENING_PAREN -> parenthesized(::disjunction)

    else -> illegalStartOf("condition")
}
