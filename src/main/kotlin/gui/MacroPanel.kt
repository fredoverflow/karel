package gui

import logic.Level
import javax.swing.*

class Macro(private val label: String, val code: String) {
    override fun toString() = label
}

class MacroPanel : JPanel() {

    val undo = JButton("undo").sansSerif()
    val redo = JButton("redo").sansSerif()

    val void = JButton("void").sansSerif().apply {
        toolTipText = "define command"
    }

    val builtinCommands = listOf(
        Macro("F1 moveForward", "moveForward();"),
        Macro("F2 turnLeft", "turnLeft();"),
        Macro("F3 turnAround", "turnAround();"),
        Macro("F4 turnRight", "turnRight();"),
        Macro("F5 pickBeeper", "pickBeeper();"),
        Macro("F6 dropBeeper", "dropBeeper();"),
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
            Macro("repeat 2", "repeat (2"),
            Macro("repeat 3", "repeat (3"),
            Macro("repeat 4", "repeat (4"),
            Macro("repeat 5", "repeat (5"),
            Macro("repeat 6", "repeat (6"),
            Macro("repeat 8", "repeat (8"),
            Macro("repeat 9", "repeat (9"),
            Macro("repeat 10", "repeat (10"),
            Macro("repeat 99", "repeat (99"),
            Macro("repeat 100", "repeat (100"),
        )
    ).sansSerif().apply {
        maximumSize = minimumSize
        maximumRowCount = 10
    }

    val ifs = JComboBox(
        arrayOf(
            Macro(
                "if",
                "\n}"
            ),
            Macro(
                "if else",
                "\n}\nelse\n{\n\n}"
            ),
            Macro(
                "if if",
                "\n}\nelse if ()\n{\n\n}"
            ),
            Macro(
                "if if else",
                "\n}\nelse if ()\n{\n\n}\nelse\n{\n\n}"
            ),
            Macro(
                "if if if",
                "\n}\nelse if ()\n{\n\n}\nelse if ()\n{\n\n}"
            ),
            Macro(
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
            Macro("not ...", "!"),
            Macro("F7 onBeeper", "onBeeper()"),
            Macro("F8 beeperAhead", "beeperAhead()"),
            Macro("F9 leftIsClear", "leftIsClear()"),
            Macro("F10 frontIsClear", "frontIsClear()"),
            Macro("F11 rightIsClear", "rightIsClear()"),
            Macro("... and ...", " && "),
            Macro("... or ...", " || "),
        )
    ).sansSerif().apply {
        maximumSize = minimumSize
        maximumRowCount = 8
    }

    val `while` = JButton("while").sansSerif()

    private val macros: Array<JComponent> = arrayOf(void, commands, repeats, ifs, conditions, `while`)

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(undo)
        add(redo)
        add(Box.createHorizontalGlue())
        macros.forEach(::add)
        configureLevel(Level.REPEAT)
    }

    fun configureLevel(level: Level) {
        macros.forEachIndexed { index, macro ->
            macro.isEnabled = (index < level.activeMacroButtons)
        }
    }
}
