package util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Collections.emptyList
import java.util.Collections.singletonList

class AutoCompleterTest {
    @Test
    fun fullSuffix() {
        val actual = completeCommand("foo() bar() baz()", "f")
        assertEquals(singletonList("oo()"), actual)
    }

    @Test
    fun partialSuffix() {
        val actual = completeCommand("foo() bar() baz()", "b")
        assertEquals(singletonList("a"), actual)
    }

    @Test
    fun ambiguous() {
        val actual = completeCommand("foo() bar() baz()", "ba")
        assertEquals(listOf("r()", "z()"), actual)
    }

    @Test
    fun alreadyComplete() {
        val actual = completeCommand("foo() bar() baz()", "foo()")
        assertEquals(emptyList<String>(), actual)
    }
}
