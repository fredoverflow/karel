package syntax.lexer

import org.junit.Assert.assertEquals
import org.junit.Test
import syntax.lexer.TokenKind.*

class LexerTest {
    private var lexer = Lexer("")

    private fun assertToken(expected: TokenKind) {
        val actualToken = lexer.nextToken()
        assertEquals(expected, actualToken.kind)
    }

    private fun assertIdentifier(expected: String) {
        val actualToken = lexer.nextToken()
        assertEquals(IDENTIFIER, actualToken.kind)
        assertEquals(expected, actualToken.lexeme)
    }

    private fun assertNumber(expected: String) {
        val actualToken = lexer.nextToken()
        assertEquals(NUMBER, actualToken.kind)
        assertEquals(expected, actualToken.lexeme)
    }

    @Test
    fun emptyString() {
        lexer = Lexer("")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun singleLineComments() {
        lexer = Lexer("""
        // comment #1
        a
        // comment #2
        // comment #3
        b
        c // comment #4
        d// comment #5
        e//
        """)

        assertIdentifier("a")
        assertIdentifier("b")
        assertIdentifier("c")
        assertIdentifier("d")
        assertIdentifier("e")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun openSingleLineComment() {
        lexer = Lexer("//")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun multiLineComments() {
        lexer = Lexer("""
        /*
        comment #1
        */
        a   /* comment #2 */
        b  /*/ comment #3*/
        c /**/
        d/***/
        e /* / ** / *** /*/
        f
        """)

        assertIdentifier("a")
        assertIdentifier("b")
        assertIdentifier("c")
        assertIdentifier("d")
        assertIdentifier("e")
        assertIdentifier("f")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun digits() {
        lexer = Lexer("0 1 2 3 4 5 6 7 8 9")
        assertNumber("0")
        assertNumber("1")
        assertNumber("2")
        assertNumber("3")
        assertNumber("4")
        assertNumber("5")
        assertNumber("6")
        assertNumber("7")
        assertNumber("8")
        assertNumber("9")
    }

    @Test
    fun numbers() {
        lexer = Lexer("10 42 97 1234567890")
        assertNumber("10")
        assertNumber("42")
        assertNumber("97")
        assertNumber("1234567890")
    }

    @Test
    fun separators() {
        lexer = Lexer("();{}")
        assertToken(OPENING_PAREN)
        assertToken(CLOSING_PAREN)
        assertToken(SEMICOLON)
        assertToken(OPENING_BRACE)
        assertToken(CLOSING_BRACE)
    }

    @Test
    fun operators() {
        lexer = Lexer("!&&||")
        assertToken(BANG)
        assertToken(AMPERSAND_AMPERSAND)
        assertToken(BAR_BAR)
    }

    @Test
    fun identifiers() {
        lexer = Lexer("a z a0 z9 a_z foo _bar the_quick_brown_fox_jumps_over_the_lazy_dog THE_QUICK_BROWN_FOX_JUMPS_OVER_THE_LAZY_DOG")

        assertIdentifier("a")
        assertIdentifier("z")
        assertIdentifier("a0")
        assertIdentifier("z9")
        assertIdentifier("a_z")
        assertIdentifier("foo")
        assertIdentifier("_bar")
        assertIdentifier("the_quick_brown_fox_jumps_over_the_lazy_dog")
        assertIdentifier("THE_QUICK_BROWN_FOX_JUMPS_OVER_THE_LAZY_DOG")
    }

    @Test
    fun keywords() {
        lexer = Lexer("if else false repeat true void while")

        assertToken(IF)
        assertToken(ELSE)
        assertToken(FALSE)
        assertToken(REPEAT)
        assertToken(TRUE)
        assertToken(VOID)
        assertToken(WHILE)
    }

    @Test
    fun predicates() {
        lexer = Lexer("onBeeper beeperAhead leftIsClear frontIsClear rightIsClear")

        assertToken(ON_BEEPER)
        assertToken(BEEPER_AHEAD)
        assertToken(LEFT_IS_CLEAR)
        assertToken(FRONT_IS_CLEAR)
        assertToken(RIGHT_IS_CLEAR)
    }
}
