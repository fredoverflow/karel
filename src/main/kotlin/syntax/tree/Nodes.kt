package syntax.tree

import syntax.lexer.Token

sealed class Node


data class Program(val commands: List<Command>) : Node()

data class Command(val void: Token, val identifier: Token, val body: Block) : Node()

data class Block(val openingBrace: Token, val statements: List<Statement>, val closingBrace: Token) : Statement()


sealed class Statement : Node()

data class Call(val target: Token) : Statement()

data class Repeat(val repeat: Token, val times: Int, val body: Block) : Statement()

// e1se is a Statement? instead of a Block? in order to support else-if (see Parser.statement)
data class IfThenElse(val iF: Token, val condition: Condition, val th3n: Block, val e1se: Statement?) : Statement()

data class While(val whi1e: Token, val condition: Condition, val body: Block) : Statement()


sealed class Condition : Node()

data class OnBeeper(val onBeeper: Token) : Condition()

data class BeeperAhead(val beeperAhead: Token) : Condition()

data class LeftIsClear(val leftIsClear: Token) : Condition()

data class FrontIsClear(val frontIsClear: Token) : Condition()

data class RightIsClear(val rightIsClear: Token) : Condition()

data class Not(val not: Token, val p: Condition) : Condition()

data class Conjunction(val p: Condition, val and: Token, val q: Condition) : Condition()

data class Disjunction(val p: Condition, val or: Token, val q: Condition) : Condition()
