package syntax.tree

import syntax.lexer.Token


sealed interface ElseBranch


class Program(val commands: List<Command>)

class Command(val void: Token, val identifier: Token, val body: Block)

class Block(val openingBrace: Token, val statements: List<Statement>, val closingBrace: Token) :
    ElseBranch


sealed class Statement

class Call(val target: Token) : Statement()

class Repeat(val repeat: Token, val times: Int, val body: Block) : Statement()

class IfThenElse(val iF: Token, val condition: Condition, val th3n: Block, val e1se: ElseBranch?) : Statement(),
    ElseBranch

class While(val whi1e: Token, val condition: Condition, val body: Block) : Statement()


sealed class Condition

class OnBeeper(val onBeeper: Token) : Condition()

class BeeperAhead(val beeperAhead: Token) : Condition()

class LeftIsClear(val leftIsClear: Token) : Condition()

class FrontIsClear(val frontIsClear: Token) : Condition()

class RightIsClear(val rightIsClear: Token) : Condition()

class Not(val not: Token, val p: Condition) : Condition()

class Conjunction(val p: Condition, val and: Token, val q: Condition) : Condition()

class Disjunction(val p: Condition, val or: Token, val q: Condition) : Condition()
