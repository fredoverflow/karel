package gui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AutocompletionTest {
    @Test
    fun fullSuffix() {
        val actual = autocompleteCall("void foo() void bar() void baz()", "f")
        assertEquals(listOf("oo();"), actual)
    }

    @Test
    fun partialSuffix() {
        val actual = autocompleteCall("void foo() void bar() void baz()", "b")
        assertEquals(listOf("a"), actual)
    }

    @Test
    fun ambiguous() {
        val actual = autocompleteCall("void foo() void bar() void baz()", "ba")
        assertEquals(listOf("r();", "z();"), actual)
    }

    @Test
    fun alreadyComplete() {
        val actual = autocompleteCall("void foo() void bar() void baz()", "foo();")
        assertEquals(emptyList<String>(), actual)
    }
}
