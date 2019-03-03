package parsing

import parsing.TokenKind.*

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

fun Parser.primaryCondition(): Condition = when (current) {
    FALSE -> False(accept())
    TRUE -> True(accept())

    ON_BEEPER -> OnBeeper(accept().emptyParens())
    BEEPER_AHEAD -> BeeperAhead(accept().emptyParens())
    LEFT_IS_CLEAR -> LeftIsClear(accept().emptyParens())
    FRONT_IS_CLEAR -> FrontIsClear(accept().emptyParens())
    RIGHT_IS_CLEAR -> RightIsClear(accept().emptyParens())

    BANG -> Not(accept(), primaryCondition())

    OPENING_PAREN -> parenthesized(::disjunction)

    else -> illegalStartOf("condition")
}
