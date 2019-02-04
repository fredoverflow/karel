package parsing

import org.junit.Assert.*
import org.junit.Test

private val keywords = Regex("[A-Za-z]+")

class LexemesTest {
    @Test
    fun keywordsComeFirst() {
        for (lexeme in allLexemes.take(KEYWORDS)) {
            assertTrue(lexeme, keywords.matches(lexeme))
        }
    }

    @Test
    fun firstNonKeyword() {
        val lexeme = allLexemes[KEYWORDS]
        assertFalse(lexeme, keywords.matches(lexeme))
    }

    @Test
    fun keywordsAreSorted() {
        for (i in 1 until KEYWORDS) {
            val a = allLexemes[i - 1]
            val b = allLexemes[i]
            if (a >= b) {
                fail("keywords are not sorted: $a >= $b")
            }
        }
    }
}
