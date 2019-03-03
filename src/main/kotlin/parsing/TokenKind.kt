package parsing

import freditor.persistent.StringedValueMap

enum class TokenKind(val lexeme: String) {
    VOID("void"),
    REPEAT("repeat"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),

    FALSE("false"),
    TRUE("true"),
    ON_BEEPER("onBeeper"),
    BEEPER_AHEAD("beeperAhead"),
    LEFT_IS_CLEAR("leftIsClear"),
    FRONT_IS_CLEAR("frontIsClear"),
    RIGHT_IS_CLEAR("rightIsClear"),

    OPENING_PAREN("("),
    CLOSING_PAREN(")"),
    OPENING_BRACE("{"),
    CLOSING_BRACE("}"),
    SEMICOLON(";"),
    BANG("!"),
    AMPERSAND_AMPERSAND("&&"),
    BAR_BAR("||"),

    NUMBER("NUMBER"),
    IDENTIFIER("IDENTIFIER"),
    END_OF_INPUT("END OF INPUT");

    override fun toString(): String = lexeme

    val isKeyword: Boolean
        get() = lexeme.first().isLowerCase()
}

val keywords: StringedValueMap<TokenKind> = TokenKind.values().filter(TokenKind::isKeyword)
        .fold(StringedValueMap.empty(), StringedValueMap<TokenKind>::put)
