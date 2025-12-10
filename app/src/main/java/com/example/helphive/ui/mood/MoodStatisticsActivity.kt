package com.example.helphive.ui.mood

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.databinding.ActivityMoodStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.helphive.data.model.Mood
import java.util.*

@AndroidEntryPoint
class MoodStatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodStatisticsBinding
    private val viewModel: MoodStatisticsViewModel by viewModels()

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChart()

        // Load all users' moods
        viewModel.loadAllUsersMoods()

        observeViewModel()
    }

    private fun setupChart() {
        binding.barChart.description.isEnabled = false
        binding.barChart.setDrawGridBackground(false)
        binding.barChart.setTouchEnabled(true)
        binding.barChart.isDragEnabled = true
        binding.barChart.setScaleEnabled(true)
        binding.barChart.setPinchZoom(false)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.moods.isNotEmpty()) {
                        updateChart(state.moods)
                    }
                }
            }
        }
    }

    private fun updateChart(moods: List<Mood>) {
        // Count moods by emoji across all users
        val moodCountMap = mutableMapOf<String, Int>()
        moods.forEach { mood ->
            val count = moodCountMap[mood.emoji] ?: 0
            moodCountMap[mood.emoji] = count + 1
        }

        // Create entries for the chart
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val colors = mutableListOf<Int>()

        var index = 0f
        moodCountMap.forEach { (emoji, count) ->
            entries.add(BarEntry(index, count.toFloat()))
            labels.add(emoji) // Store emoji for x-axis labels
            // Use a specific color from the template based on index
            val colorIndex = index.toInt() % ColorTemplate.COLORFUL_COLORS.size
            colors.add(ColorTemplate.COLORFUL_COLORS[colorIndex])
            index++
        }

        val dataSet = BarDataSet(entries, "Mood Count")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f

        // Set x-axis labels
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels) {
            override fun getFormattedValue(value: Float): String {
                return if (value >= 0 && value < labels.size) labels[value.toInt()] else ""
            }
        }
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.invalidate()

        // Add legend showing emoji-color mapping
        setupLegend(moodCountMap, colors)
    }

    private fun setupLegend(moodCountMap: Map<String, Int>, colors: List<Int>) {
        // Clear previous legend items
        binding.legendContainer.removeAllViews()

        var index = 0
        moodCountMap.forEach { (emoji, count) ->
            val legendItem = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 4, 8, 4)
            }

            val colorBox = View(this).apply {
                val layoutParams = LinearLayout.LayoutParams(30, 30).apply {
                    setMargins(0, 0, 16, 0)
                }
                this.layoutParams = layoutParams
                setBackgroundColor(colors[index])
            }

            val text = TextView(this).apply {
                text = "$emoji - Count: $count"
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
            }

            legendItem.addView(colorBox)
            legendItem.addView(text)
            binding.legendContainer.addView(legendItem)
            index++
        }
    }
}