package gui

import logic.Level
import javax.swing.*

class Snippet(private val label: String, val code: String) {
    override fun toString() = label
}

class SnippetPanel : JPanel() {

    val undo = JButton("undo").sansSerif()
    val redo = JButton("redo").sansSerif()

    val void = JButton("void").sansSerif().apply {
        toolTipText = "define command"
    }

    val builtinCommands = listOf(
        Snippet("F1 moveForward", "moveForward();"),
        Snippet("F2 turnLeft", "turnLeft();"),
        Snippet("F3 turnAround", "turnAround();"),
        Snippet("F4 turnRight", "turnRight();"),
        Snippet("F5 pickBeeper", "pickBeeper();"),
        Snippet("F6 dropBeeper", "dropBeeper();"),
    )
    val commands = JComboBox(
        builtinCommands.toTypedArray()
    ).sansSerif().apply {
        maximumSize = minimumSize
        maximumRowCount = 24
        toolTipText = "call command"
    }

    val repeats = JComboBox(
        arrayOf(
            Snippet("repeat 2", "repeat (2"),
            Snippet("repeat 3", "repeat (3"),
            Snippet("repeat 4", "repeat (4"),
            Snippet("repeat 5", "repeat (5"),
            Snippet("repeat 6", "repeat (6"),
            Snippet("repeat 8", "repeat (8"),
            Snippet("repeat 9", "repeat (9"),
            Snippet("repeat 10", "repeat (10"),
            Snippet("repeat 99", "repeat (99"),
            Snippet("repeat 100", "repeat (100"),
        )
    ).sansSerif().apply {
        maximumSize = minimumSize
        maximumRowCount = 10
    }

    val ifs = JComboBox(
        arrayOf(
            Snippet(
                "if",
                "\n}"
            ),
            Snippet(
                "if else",
                "\n}\nelse\n{\n\n}"
            ),
            Snippet(
                "if if",
                "\n}\nelse if ()\n{\n\n}"
            ),
            Snippet(
                "if if else",
                "\n}\nelse if ()\n{\n\n}\nelse\n{\n\n}"
            ),
            Snippet(
                "if if if",
                "\n}\nelse if ()\n{\n\n}\nelse if ()\n{\n\n}"
            ),
            Snippet(
                "if if if else",
                "\n}\nelse if ()\n{\n\n}\nelse if ()\n{\n\n}\nelse\n{\n\n}"
            ),
        )
    ).sansSerif().apply {
        maximumSize = minimumSize
        maximumRowCount = 6
    }

    val conditions = JComboBox(
        arrayOf(
            Snippet("not ...", "!"),
            Snippet("F7 onBeeper", "onBeeper()"),
            Snippet("F8 beeperAhead", "beeperAhead()"),
            Snippet("F9 leftIsClear", "leftIsClear()"),
            Snippet("F10 frontIsClear", "frontIsClear()"),
            Snippet("F11 rightIsClear", "rightIsClear()"),
            Snippet("... and ...", " && "),
            Snippet("... or ...", " || "),
        )
    ).sansSerif().apply {
        maximumSize = minimumSize
        maximumRowCount = 8
    }

    val `while` = JButton("while").sansSerif()

    private val snippets: Array<JComponent> = arrayOf(void, commands, repeats, ifs, conditions, `while`)

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(undo)
        add(redo)
        add(Box.createHorizontalGlue())
        snippets.forEach(::add)
        configureLevel(Level.REPEAT)
    }

    fun configureLevel(level: Level) {
        snippets.forEachIndexed { index, snippet ->
            snippet.isEnabled = (index < level.activeSnippetButtons)
        }
    }
}
