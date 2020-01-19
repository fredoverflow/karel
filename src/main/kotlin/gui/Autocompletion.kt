package gui

private val command = Regex("""\bvoid\s+(\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*)""")
private val reverse = Regex(""";?\)?\(?\p{javaJavaIdentifierPart}*\p{javaJavaIdentifierStart}""")

fun autocompleteCall(sourceCode: String, lineBeforeSelection: String): List<String> {
    val suffixes = fittingSuffixes(sourceCode, lineBeforeSelection)
    val lcp = longestCommonPrefix(suffixes)
    return if (lcp.isEmpty()) {
        suffixes
    } else {
        listOf(lcp)
    }
}

private fun fittingSuffixes(sourceCode: String, lineBeforeSelection: String): List<String> {
    val prefix = reverse.find(lineBeforeSelection.reversed())?.value.orEmpty().reversed()
    val prefixLength = prefix.length

    return command
            .findAll(sourceCode)
            .map { it.groups[1]!!.value + "();" }
            .filter { it.length > prefixLength && it.startsWith(prefix) }
            .map { it.substring(prefixLength) }
            .toList()
}

private fun longestCommonPrefix(strings: List<String>): String {
    val shortestString = strings.minBy(String::length) ?: ""
    shortestString.forEachIndexed { index, ch ->
        if (!strings.all { command -> command[index] == ch }) {
            return shortestString.substring(0, index)
        }
    }
    return shortestString
}
