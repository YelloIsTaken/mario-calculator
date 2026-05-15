package com.mario.calculator

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val engine = CalculatorEngine()

    private lateinit var tvDisplay: TextView
    private lateinit var tvMemoryValue: TextView
    private lateinit var tvHistory: TextView
    private lateinit var svHistory: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tv_display)
        tvMemoryValue = findViewById(R.id.tv_memory_value)
        tvHistory = findViewById(R.id.tv_history)
        svHistory = findViewById(R.id.sv_history)

        bindButtons()
    }

    private fun bindButtons() {
        // Digit buttons
        mapOf(
            R.id.btn_0 to "0", R.id.btn_1 to "1", R.id.btn_2 to "2",
            R.id.btn_3 to "3", R.id.btn_4 to "4", R.id.btn_5 to "5",
            R.id.btn_6 to "6", R.id.btn_7 to "7", R.id.btn_8 to "8",
            R.id.btn_9 to "9", R.id.btn_00 to "00", R.id.btn_dot to "."
        ).forEach { (id, digit) ->
            findViewById<Button>(id).setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                engine.inputDigit(digit)
                updateDisplay()
            }
        }

        // Operator buttons
        mapOf(
            R.id.btn_add to "+", R.id.btn_subtract to "−",
            R.id.btn_multiply to "×", R.id.btn_divide to "÷"
        ).forEach { (id, op) ->
            findViewById<Button>(id).setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                engine.inputOperator(op)
                updateDisplay()
            }
        }

        // Equals
        findViewById<Button>(R.id.btn_equals).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.equals()
            updateDisplay()
            updateHistory()
        }

        // Percentage
        findViewById<Button>(R.id.btn_percent).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.percentage()
            updateDisplay()
        }

        // Clear Entry
        findViewById<Button>(R.id.btn_ce).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.clearEntry()
            updateDisplay()
        }

        // Clear All
        findViewById<Button>(R.id.btn_c).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.clearAll()
            updateDisplay()
        }

        // Memory buttons
        findViewById<Button>(R.id.btn_mc).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.memoryClear()
            updateMemory()
        }
        findViewById<Button>(R.id.btn_mr).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.memoryRecall()
            updateDisplay()
        }
        findViewById<Button>(R.id.btn_mminus).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.memorySubtract()
            updateMemory()
        }
        findViewById<Button>(R.id.btn_mplus).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            engine.memoryAdd()
            updateMemory()
        }

        // Navigation
        findViewById<Button>(R.id.btn_exit).setOnClickListener {
            showExitDialog()
        }
        findViewById<Button>(R.id.btn_unit_converter).setOnClickListener {
            startActivity(Intent(this, UnitConverterActivity::class.java))
        }
        findViewById<Button>(R.id.btn_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    private fun updateDisplay() {
        tvDisplay.text = engine.displayValue
    }

    private fun updateMemory() {
        tvMemoryValue.text = engine.formatMemory()
    }

    private fun updateHistory() {
        val lines = engine.history.takeLast(8).joinToString("\n")
        tvHistory.text = lines
        svHistory.post { svHistory.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.exit_title))
            .setMessage(getString(R.string.exit_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> finishAffinity() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
