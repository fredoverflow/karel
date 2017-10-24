package parsing

val allLexemes = arrayOf(
        "beeperAhead",
        "else",
        "false",
        "frontIsClear",
        "if",
        "leftIsClear",
        "onBeeper",
        "repeat",
        "rightIsClear",
        "true",
        "void",
        "while",
        // Keywords come first and must be sorted for binary search!
        "!", "&&", "(", ")", ";", "{", "||", "}",
        "number", "identifier", "end of file"
)

const val KEYWORDS = 12

fun identifierOrKeyword(lexeme: String): TokenKind {
    val index = allLexemes.binarySearch(lexeme, 0, KEYWORDS)
    return if (index < 0) IDENTIFIER else index.toByte()
}
