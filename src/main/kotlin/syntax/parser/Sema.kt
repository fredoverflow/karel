package syntax.parser

import freditor.Levenshtein
import syntax.tree.Call
import syntax.tree.Command
import syntax.tree.Program

val BUILTIN_COMMANDS = setOf("moveForward", "turnLeft", "turnAround", "turnRight", "pickBeeper", "dropBeeper")

class Sema {
    private val commands = LinkedHashMap<String, Command>()
    private val calls = ArrayList<Call>()

    fun previousCommandName(): String = commands.keys.last()

    fun command(name: String): Command? = commands[name]

    operator fun invoke(command: Command): Command {
        if (commands.containsKey(command.identifier.lexeme)) {
            command.identifier.error("duplicate command ${command.identifier.lexeme}")
        }
        if (BUILTIN_COMMANDS.contains(command.identifier.lexeme)) {
            command.identifier.error("cannot redefine builtin command ${command.identifier.lexeme}")
        }
        commands[command.identifier.lexeme] = command
        return command
    }

    operator fun invoke(call: Call): Call {
        if (!BUILTIN_COMMANDS.contains(call.target.lexeme)) {
            calls.add(call)
        }
        return call
    }

    operator fun invoke(program: Program): Program {
        for (call in calls) {
            if (!commands.containsKey(call.target.lexeme)) {
                val bestMatches = Levenshtein.bestMatches(call.target.lexeme, commands.keys + BUILTIN_COMMANDS)
                if (bestMatches.size == 1) {
                    val bestMatch = bestMatches.first()
                    val prefix = bestMatch.commonPrefixWith(call.target.lexeme)
                    call.target.error("Did you mean $bestMatch? Otherwise: click VOID button", prefix.length)
                } else {
                    val commaSeparated = bestMatches.joinToString(", ")
                    call.target.error("Did you mean $commaSeparated? Otherwise: click VOID button")
                }
            }
        }
        return program
    }
}
