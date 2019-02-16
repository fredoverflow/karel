package parsing

import org.junit.Assert.*
import org.junit.Test

private val keywords = Regex("[A-Za-z]+")

class LexemePoolTest {
    @Test
    fun keywordsComeFirst() {
        for (lexeme in lexemePool.take(NUM_KEYWORDS)) {
            assertTrue(lexeme, keywords.matches(lexeme))
        }
    }

    @Test
    fun firstNonKeyword() {
        val lexeme = lexemePool[NUM_KEYWORDS]
        assertFalse(lexeme, keywords.matches(lexeme))
    }

    @Test
    fun keywordsAreSorted() {
        for (i in 1 until NUM_KEYWORDS) {
            val a = lexemePool[i - 1]
            val b = lexemePool[i]
            if (a >= b) {
                fail("keywords are not sorted: $a >= $b")
            }
        }
    }
}
