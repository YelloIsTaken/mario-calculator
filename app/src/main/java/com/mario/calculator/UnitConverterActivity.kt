package com.mario.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class UnitConverterActivity : AppCompatActivity() {

    private data class Unit(val name: String, val toBase: Double)

    private val categories = listOf(
        "Length", "Weight / Mass", "Temperature",
        "Area", "Volume", "Speed"
    )

    private val units: Map<String, List<Unit>> = mapOf(
        "Length" to listOf(
            Unit("mm", 0.001),
            Unit("cm", 0.01),
            Unit("m", 1.0),
            Unit("km", 1000.0),
            Unit("inch", 0.0254),
            Unit("foot", 0.3048),
            Unit("yard", 0.9144),
            Unit("mile", 1609.344)
        ),
        "Weight / Mass" to listOf(
            Unit("mg", 0.000001),
            Unit("g", 0.001),
            Unit("kg", 1.0),
            Unit("oz", 0.0283495),
            Unit("lb", 0.453592),
            Unit("ton (metric)", 1000.0),
            Unit("ton (US)", 907.185)
        ),
        "Temperature" to listOf(
            Unit("°C", 1.0),
            Unit("°F", 1.0),
            Unit("K", 1.0)
        ),
        "Area" to listOf(
            Unit("mm²", 0.000001),
            Unit("cm²", 0.0001),
            Unit("m²", 1.0),
            Unit("km²", 1_000_000.0),
            Unit("inch²", 0.00064516),
            Unit("foot²", 0.092903),
            Unit("acre", 4046.86),
            Unit("hectare", 10000.0)
        ),
        "Volume" to listOf(
            Unit("mL", 0.001),
            Unit("L", 1.0),
            Unit("m³", 1000.0),
            Unit("fl oz (US)", 0.0295735),
            Unit("cup (US)", 0.236588),
            Unit("pint (US)", 0.473176),
            Unit("gallon (US)", 3.78541),
            Unit("inch³", 0.0163871),
            Unit("foot³", 28.3168)
        ),
        "Speed" to listOf(
            Unit("m/s", 1.0),
            Unit("km/h", 0.277778),
            Unit("mph", 0.44704),
            Unit("knot", 0.514444),
            Unit("ft/s", 0.3048)
        )
    )

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerFromUnit: Spinner
    private lateinit var spinnerToUnit: Spinner
    private lateinit var etFromValue: EditText
    private lateinit var tvToValue: TextView

    private var currentCategory = "Length"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_converter)

        spinnerCategory = findViewById(R.id.spinner_category)
        spinnerFromUnit = findViewById(R.id.spinner_from_unit)
        spinnerToUnit = findViewById(R.id.spinner_to_unit)
        etFromValue = findViewById(R.id.et_from_value)
        tvToValue = findViewById(R.id.tv_to_value)

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }

        setupCategorySpinner()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                currentCategory = categories[pos]
                setupUnitSpinners(currentCategory)
                recalculate()
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }

        setupUnitSpinners(currentCategory)

        etFromValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) { recalculate() }
        })
    }

    private fun setupUnitSpinners(category: String) {
        val unitList = units[category] ?: return
        val names = unitList.map { it.name }

        val fromAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFromUnit.adapter = fromAdapter

        val toAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerToUnit.adapter = toAdapter

        // Default: first unit from, second unit to
        if (names.size > 1) spinnerToUnit.setSelection(1)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) { recalculate() }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }
        spinnerFromUnit.onItemSelectedListener = listener
        spinnerToUnit.onItemSelectedListener = listener
    }

    private fun recalculate() {
        val input = etFromValue.text.toString().toDoubleOrNull()
        if (input == null) { tvToValue.text = "—"; return }

        val unitList = units[currentCategory] ?: return
        val fromIdx = spinnerFromUnit.selectedItemPosition
        val toIdx = spinnerToUnit.selectedItemPosition
        if (fromIdx < 0 || toIdx < 0 || fromIdx >= unitList.size || toIdx >= unitList.size) return

        val fromUnit = unitList[fromIdx]
        val toUnit = unitList[toIdx]

        val result = if (currentCategory == "Temperature") {
            convertTemperature(input, fromUnit.name, toUnit.name)
        } else {
            val base = input * fromUnit.toBase
            base / toUnit.toBase
        }

        tvToValue.text = formatResult(result)
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        val celsius = when (from) {
            "°C" -> value
            "°F" -> (value - 32) * 5.0 / 9.0
            "K"  -> value - 273.15
            else -> value
        }
        return when (to) {
            "°C" -> celsius
            "°F" -> celsius * 9.0 / 5.0 + 32
            "K"  -> celsius + 273.15
            else -> celsius
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val long = value.toLong()
        return if (value == long.toDouble() && kotlin.math.abs(value) < 1e12) {
            long.toString()
        } else {
            val formatted = "%.8f".format(value).trimEnd('0').trimEnd('.')
            if (formatted.length > 14) "%.6E".format(value) else formatted
        }
    }
}
