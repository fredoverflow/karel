package gui

import kotlin.math.min

private val command = Regex("""\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*\(\)""")
private val reverse = Regex("""\)?\(?\p{javaJavaIdentifierPart}*\p{javaJavaIdentifierStart}""")

fun completeCommand(sourceCode: String, lineUntilCursor: String): List<String> {
    val suffixes = fittingSuffixes(sourceCode, lineUntilCursor)
    return if (suffixes.isEmpty()) {
        emptyList()
    } else {
        val lcp = longestCommonPrefixOf(suffixes)
        if (lcp.isNotEmpty()) {
            listOf(lcp)
        } else {
            suffixes
        }
    }
}

private fun fittingSuffixes(sourceCode: String, lineUntilCursor: String): List<String> {
    val prefix = reverse.find(lineUntilCursor.reversed())?.value.orEmpty().reversed()
    val prefixLength = prefix.length

    val allCommands = command.findAll(sourceCode).map(MatchResult::value).toMutableList()
    allCommands.sort()

    val fittingCommands = allCommands.toSet().filter { (it.length > prefixLength) && it.startsWith(prefix) }
    return fittingCommands.map { it.substring(prefixLength) }
}

private fun longestCommonPrefixOf(commands: List<String>): String {
    return commands.fold(commands[0], ::longestCommonPrefix)
}

// TODO Is there an elegant AND EFFICIENT functional solution?
private fun longestCommonPrefix(a: String, b: String): String {
    val n = min(a.length, b.length)
    var i = 0
    while ((i < n) && (a[i] == b[i])) {
        ++i
    }
    return a.substring(0, i)
}
