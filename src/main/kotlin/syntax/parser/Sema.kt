package syntax.parser

import freditor.Levenshtein
import syntax.lexer.Token
import syntax.lexer.TokenKind.VOID
import syntax.tree.Block
import syntax.tree.Call
import syntax.tree.Command
import syntax.tree.Program

private val TOKEN = Token(VOID, 0, "")
private val BLOCK = Block(TOKEN, emptyList(), TOKEN)
private val BUILTIN = Command(TOKEN, TOKEN, emptyList(), BLOCK)

class Sema {
    private val commands = mutableMapOf(
        "moveForward" to BUILTIN,
        "turnLeft" to BUILTIN,
        "turnAround" to BUILTIN,
        "turnRight" to BUILTIN,
        "pickBeeper" to BUILTIN,
        "dropBeeper" to BUILTIN
    )

    private var callsInCurrentCommand = ArrayList<Call>()
    private val callsByCommand = LinkedHashMap<Command, ArrayList<Call>>()

    fun command(name: String): Command? = commands[name]

    operator fun invoke(command: Command): Command {
        if (commands.containsKey(command.identifier.lexeme)) {
            command.identifier.error("duplicate command ${command.identifier.lexeme}")
        }
        commands[command.identifier.lexeme] = command
        callsByCommand[command] = callsInCurrentCommand
        callsInCurrentCommand = ArrayList()
        return command
    }

    operator fun invoke(call: Call): Call {
        callsInCurrentCommand.add(call)
        return call
    }

    operator fun invoke(program: Program): Program {
        for ((caller, calls) in callsByCommand) {
            val previous = LinkedHashMap<String, Command?>()
            for (parameter in caller.parameters) {
                if (previous.put(parameter.lexeme, commands.put(parameter.lexeme, BUILTIN)) != null) {
                    parameter.error("duplicate parameter ${parameter.lexeme}")
                }
            }

            for (call in calls) {
                val callee = command(call.target)

                val parameterCount = callee.parameters.size
                val argumentCount = call.arguments.size
                if (parameterCount != argumentCount) {
                    call.target.error("${call.target.lexeme} takes $parameterCount arguments, not $argumentCount")
                }

                for (argument in call.arguments) {
                    if (command(argument).parameters.isNotEmpty()) {
                        argument.error("cannot pass higher-order commands")
                    }
                }
            }
            for ((name, command) in previous) {
                if (command != null) {
                    commands[name] = command
                } else {
                    commands.remove(name)
                }
            }
        }
        return program
    }

    private fun command(target: Token): Command {
        val result = commands[target.lexeme]
        if (result != null) return result

        val bestMatches = Levenshtein.bestMatches(target.lexeme, commands.keys)
        if (bestMatches.size == 1) {
            val bestMatch = bestMatches.first()
            val prefix = bestMatch.commonPrefixWith(target.lexeme)
            target.error("Did you mean $bestMatch?", prefix.length)
        } else {
            val commaSeparated = bestMatches.joinToString(", ")
            target.error("Did you mean $commaSeparated?")
        }
    }
}
