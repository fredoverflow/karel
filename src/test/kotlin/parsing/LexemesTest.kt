package parsing

import org.junit.Assert.*
import org.junit.Test
import java.util.regex.Pattern

private val keywords = Pattern.compile("[A-Za-z]+")

class LexemesTest {
    @Test
    fun keywordsComeFirst() {
        for (lexeme in allLexemes.take(KEYWORDS)) {
            assertTrue(lexeme, keywords.matcher(lexeme).matches())
        }
    }

    @Test
    fun firstNonKeyword() {
        val lexeme = allLexemes[KEYWORDS]
        assertFalse(lexeme, keywords.matcher(lexeme).matches())
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
