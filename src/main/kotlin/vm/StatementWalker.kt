package vm

import syntax.tree.*

class StatementWalker(private val enter: (Node) -> Unit) {

    fun Node.walk() {
        enter(this)

        when (this) {
            is Program ->
                commands.forEach { it.walk() }

            is Command ->
                body.walk()

            is Block ->
                statements.forEach { it.walk() }

            is Repeat ->
                body.walk()

            is IfThenElse -> {
                th3n.walk()
                e1se?.walk()
            }

            is While ->
                body.walk()

            else -> {
            }
        }
    }
}
