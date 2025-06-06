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
        maximumRowCount = 20
    }

    val startStopReset = JButton("start").sansSerif()

    val check = JButton("\uD83D\uDC1C").sansSerif().apply {
        toolTipText = "check every ${problems[0].check.singular}"
    }

    val stepInto = JButton("step into (F12)").sansSerif()
    val stepOver = JButton("step over").sansSerif()
    val stepReturn = JButton("step return").sansSerif()

    private fun setEnabledStepButtons(enabled: Boolean) {
        stepInto.isEnabled = enabled
        stepOver.isEnabled = enabled
        stepReturn.isEnabled = enabled
    }

    val pause = JButton("\u23F8").sansSerif()
    val slider = JSlider(0, 11, 2).sansSerif()
    val fast = JButton("\u23E9").sansSerif()

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
        add(horizontalBoxPanel(pause, slider, fast))
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

    fun isRunning(): Boolean {
        return startStopReset.text === "stop"
    }
}
