package com.mario.calculator

import java.util.Locale
import kotlin.math.abs

class CalculatorEngine {

    var displayValue: String = "0"
        private set

    var memoryValue: Double = 0.0
        private set

    val history: MutableList<String> = mutableListOf()

    private val currentInput = StringBuilder()
    private var pendingOperator: String? = null
    private var previousValue: Double = 0.0
    private var justCalculated: Boolean = false
    private var hasDecimal: Boolean = false
    private var errorState: Boolean = false

    fun inputDigit(digit: String) {
        if (errorState) clearAll()
        if (justCalculated) {
            currentInput.clear()
            hasDecimal = false
            justCalculated = false
        }
        if (digit == "00") {
            if (currentInput.isEmpty() || currentInput.toString() == "0") return
            if (currentInput.length >= 11) return
            currentInput.append("00")
        } else if (digit == ".") {
            if (hasDecimal) return
            if (currentInput.isEmpty()) currentInput.append("0")
            currentInput.append(".")
            hasDecimal = true
        } else {
            if (currentInput.length >= 12) return
            if (currentInput.toString() == "0") currentInput.clear()
            currentInput.append(digit)
        }
        displayValue = currentInput.toString()
    }

    fun inputOperator(op: String) {
        if (errorState) return
        if (pendingOperator != null && currentInput.isNotEmpty() && !justCalculated) {
            val rhs = currentInputToDouble() ?: return
            val result = applyOp(previousValue, rhs, pendingOperator!!)
            if (result == null) {
                displayValue = "Error"
                errorState = true
                return
            }
            val entry = "${formatNum(previousValue)} $pendingOperator ${formatNum(rhs)} = ${formatNum(result)}"
            history.add(entry)
            previousValue = result
            displayValue = formatNum(result)
        } else if (currentInput.isNotEmpty()) {
            previousValue = currentInputToDouble() ?: return
        } else if (justCalculated) {
            // previousValue already set from last result
        } else {
            return
        }
        currentInput.clear()
        hasDecimal = false
        justCalculated = false
        pendingOperator = op
    }

    fun equals() {
        if (errorState || pendingOperator == null) return
        val rhs = if (currentInput.isNotEmpty()) currentInputToDouble() ?: return else previousValue
        val result = applyOp(previousValue, rhs, pendingOperator!!)
        if (result == null) {
            displayValue = "Error"
            errorState = true
            return
        }
        val entry = "${formatNum(previousValue)} $pendingOperator ${formatNum(rhs)} = ${formatNum(result)}"
        history.add(entry)
        displayValue = formatNum(result)
        previousValue = result
        currentInput.clear()
        hasDecimal = false
        justCalculated = true
        pendingOperator = null
    }

    fun percentage() {
        if (errorState) return
        val value = currentValue()
        val result = if (pendingOperator == "+" || pendingOperator == "−") {
            previousValue * value / 100.0
        } else {
            value / 100.0
        }
        displayValue = formatNum(result)
        currentInput.clear()
        currentInput.append(displayValue)
        hasDecimal = displayValue.contains(".")
    }

    fun clearEntry() {
        if (errorState) { clearAll(); return }
        currentInput.clear()
        hasDecimal = false
        justCalculated = false
        displayValue = "0"
    }

    fun clearAll() {
        currentInput.clear()
        hasDecimal = false
        pendingOperator = null
        previousValue = 0.0
        justCalculated = false
        errorState = false
        displayValue = "0"
    }

    fun memoryAdd() {
        val v = parseDisplay()
        memoryValue += v
    }

    fun memorySubtract() {
        val v = parseDisplay()
        memoryValue -= v
    }

    fun memoryRecall() {
        val formatted = formatNum(memoryValue)
        displayValue = formatted
        currentInput.clear()
        currentInput.append(formatted)
        hasDecimal = formatted.contains(".")
        justCalculated = false
        errorState = false
    }

    fun memoryClear() {
        memoryValue = 0.0
    }

    fun formatMemory(): String = formatNum(memoryValue)

    fun restoreState(display: String, memory: Double, historyList: List<String>) {
        displayValue = display
        memoryValue = memory
        history.clear()
        history.addAll(historyList)
        currentInput.clear()
        if (display != "0" && display != "Error") currentInput.append(display)
        hasDecimal = display.contains(".")
        errorState = display == "Error"
    }

    private fun currentValue(): Double =
        if (currentInput.isNotEmpty()) currentInput.toString().toDoubleOrNull() ?: 0.0 else previousValue

    private fun currentInputToDouble(): Double? {
        val v = currentInput.toString().toDoubleOrNull()
        if (v == null) {
            currentInput.clear()
            hasDecimal = false
            displayValue = "Error"
            errorState = true
        }
        return v
    }

    private fun parseDisplay(): Double =
        displayValue.toDoubleOrNull() ?: 0.0

    private fun applyOp(a: Double, b: Double, op: String): Double? = when (op) {
        "+" -> a + b
        "−" -> a - b
        "×" -> a * b
        "÷" -> if (b == 0.0) null else a / b
        else -> b
    }

    private fun formatNum(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val long = value.toLong()
        return if (value == long.toDouble() && abs(value) < 1e12) {
            long.toString()
        } else {
            val s = value.toBigDecimal().stripTrailingZeros().toPlainString()
            if (s.length > 12) String.format(Locale.US, "%.6G", value) else s
        }
    }
}
