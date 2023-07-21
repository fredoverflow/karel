package gui

import freditor.Fronts
import logic.Problem
import javax.swing.*

fun <T : JComponent> T.sansSerif(): T {
    this.font = Fronts.sansSerif
    return this
}

class ControlPanel(problems: List<Problem>) : JPanel() {

    val randomize = JButton("\uD83C\uDFB2").sansSerif().apply {
        isEnabled = false
    }

    val goal = JButton("goal").sansSerif()

    val problemPicker = JComboBox(problems.toTypedArray()).sansSerif().apply {
        maximumSize = minimumSize
    }

    val startStopReset = JButton("start").sansSerif()

    val check = JButton("\uD83D\uDC1C").sansSerif().apply {
        toolTipText = problems[0].checkAfter.toolTipText
    }

    val stepInto = JButton("step into (F12)").sansSerif()
    val stepOver = JButton("step over").sansSerif()
    val stepReturn = JButton("step return").sansSerif()

    private fun setEnabledStepButtons(enabled: Boolean) {
        stepInto.isEnabled = enabled
        stepOver.isEnabled = enabled
        stepReturn.isEnabled = enabled
    }

    val slider = JSlider(0, 11, 2).sansSerif()

    fun delayLogarithm(): Int {
        return if (slider.value == 0) -1 else slider.maximum - slider.value
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        setEnabledStepButtons(false)
        add(horizontalBoxPanel(randomize, goal, problemPicker, startStopReset, check))
        add(Box.createVerticalStrut(16))
        add(horizontalBoxPanel(stepInto, stepOver, stepReturn))
        add(Box.createVerticalStrut(16))
        add(slider)
    }

    fun executionStarted() {
        randomize.isEnabled = false
        goal.isEnabled = false
        problemPicker.isEnabled = false
        startStopReset.text = "stop"
        check.isEnabled = false
        setEnabledStepButtons(true)
    }

    fun executionFinished(isRandom: Boolean) {
        randomize.isEnabled = isRandom
        goal.isEnabled = true
        problemPicker.isEnabled = true
        startStopReset.text = "reset"
        check.isEnabled = true
        setEnabledStepButtons(false)
    }

    fun checkStarted() {
        randomize.isEnabled = false
        goal.isEnabled = false
        problemPicker.isEnabled = false
        startStopReset.isEnabled = false
        check.isEnabled = false
    }

    fun checkFinished(isRandom: Boolean) {
        randomize.isEnabled = isRandom
        goal.isEnabled = true
        problemPicker.isEnabled = true
        startStopReset.isEnabled = true
        check.isEnabled = true
    }

    fun isRunning(): Boolean {
        return startStopReset.text === "stop"
    }
}
