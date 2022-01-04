package vm

import common.Diagnostic
import freditor.Levenshtein
import logic.Problem
import syntax.tree.*

val BUILTIN_COMMANDS = listOf("moveForward", "turnLeft", "turnAround", "turnRight", "pickBeeper", "dropBeeper")

class KarelSemantics(val program: Program, entryPoint: String = program.commands.first().identifier.lexeme) {
    companion object {
        val whileEnablers: List<String> = Problem.problems.filter { it.level >= 2 }.map { it.name }
        val firstWhileEnabler: Problem = Problem.problems.first { it.level >= 2 }

        val recursionEnablers: List<String> = Problem.problems.filter { it.level >= 3 }.map { it.name }
        val firstRecursionEnabler: Problem = Problem.problems.first { it.level >= 3 }
    }

    val commands: Map<String, Command> = program.commands.associateBy { it.identifier.lexeme }

    val calleesOf: Map<Command, List<Command>> = program.commands.associateWith { commandsCalledBy(it) }

    private fun commandsCalledBy(command: Command): List<Command> {
        return callsInside(command).mapNotNull { (target) -> commands[target.lexeme] }
    }

    private fun callsInside(commandOrProgram: Node): List<Call> {
        val calls = ArrayList<Call>()
        val walker = StatementWalker { node ->
            if (node is Call) {
                calls.add(node)
            }
        }
        with(walker) {
            commandOrProgram.walk()
        }
        return calls
    }

    val reachableCommands: List<Command> = commandsReachableFrom(commands[entryPoint])

    private fun commandsReachableFrom(start: Command?): List<Command> {
        if (start == null) return emptyList()

        // The entry point must come first for code generation,
        // hence the LinkedHashSet which maintains order.
        val visited = LinkedHashSet<Command>()

        fun visit(current: Command) {
            visited.add(current)
            calleesOf[current]!!
                    .filterNot { visited.contains(it) }
                    .forEach { visit(it) }
        }

        visit(start)
        return visited.toList()
    }

    fun errors(): List<Diagnostic> {
        val errors = ArrayList<Diagnostic>()

        errors.addAll(duplicateCommands())
        errors.addAll(undefinedCommands())
        errors.addAll(illegalWhileLoops())
        errors.addAll(illegalRecursion())

        errors.sortBy(Diagnostic::position)
        return errors
    }

    private fun duplicateCommands(): List<Diagnostic> {
        val grouped = program.commands.groupBy { it.identifier.lexeme }
        val duplicates = grouped.values.filter { it.size >= 2 }
        return duplicates.map { command ->
            val name = command[0].identifier.lexeme
            val first = command[0].identifier.start
            val second = command[1].identifier.start
            Diagnostic(second, "duplicate command $name was already defined at $first")
        }
    }

    private fun undefinedCommands(): List<Diagnostic> {
        return callsInside(program)
            .filter(this::targetIsUnknown)
            .map { call ->
                val bestMatches = Levenshtein.bestMatches(call.target.lexeme, commands.keys + BUILTIN_COMMANDS)
                if (bestMatches.size == 1) {
                    val bestMatch = bestMatches.first()
                    val prefix = bestMatch.commonPrefixWith(call.target.lexeme)
                    Diagnostic(call.target.start + prefix.length, "Did you mean $bestMatch?")
                } else {
                    val commaSeparated = bestMatches.joinToString(", ")
                    Diagnostic(call.target.start, "Did you mean $commaSeparated?")
                }
            }
    }

    private fun targetIsUnknown(call: Call): Boolean {
        return !commands.contains(call.target.lexeme) && !builtinCommands.contains(call.target.lexeme)
    }

    private fun illegalWhileLoops(): List<Diagnostic> {
        return if (whileEnablers.none(commands::containsKey)) {
            whileLoops().map { Diagnostic(it.whi1e.start, "while loops are not allowed until $firstWhileEnabler") }
        } else {
            emptyList()
        }
    }

    private fun whileLoops(): List<While> {
        val whiles = ArrayList<While>()
        val walker = StatementWalker { node ->
            if (node is While) {
                whiles.add(node)
            }
        }
        with(walker) {
            reachableCommands.forEach { it.walk() }
        }
        return whiles
    }

    private fun illegalRecursion(): List<Diagnostic> {
        return if (recursionEnablers.none(commands::containsKey)) {
            recursiveCommands().map { Diagnostic(it.identifier.start, "recursion is not allowed until $firstRecursionEnabler") }
        } else {
            emptyList()
        }
    }

    private fun recursiveCommands(): List<Command> {
        return reachableCommands.filter { start ->
            calleesOf[start]!!.flatMap(this::commandsReachableFrom).contains(start)
        }
    }
}
