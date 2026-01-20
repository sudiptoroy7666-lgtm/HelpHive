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
import com.example.helphive.core.utils.DateUtils
import kotlinx.coroutines.launch
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.animation.Easing
import com.example.helphive.data.model.Mood

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
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)

            // Configure X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.BLACK
                // Value formatter will be set in updateChart
            }

            // Configure Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.GRAY
                textColor = Color.BLACK
            }
            axisRight.isEnabled = false // Disable right Y-axis

            // General settings
            legend.isEnabled = false // We use a custom legend
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.moods.isNotEmpty()) {
                        updateChart(state.moods)
                    } else {
                        // Handle empty state if needed
                        binding.barChart.clear()
                        binding.barChart.setNoDataText("No mood data available")
                        binding.legendContainer.removeAllViews()
                        binding.tvTotalMoods.text = "Total Moods Logged: 0"
                        binding.tvMostCommonMood.text = "Most Common Mood: N/A"
                    }
                }
            }
        }
    }

    private fun updateSummaryStats(moods: List<Mood>) {
        val totalMoods = moods.size
        binding.tvTotalMoods.text = "Total Moods Logged: $totalMoods"

        if (moods.isNotEmpty()) {
            val moodCounts = moods.groupingBy { it.emoji }.eachCount()
            val mostCommonMoodEntry = moodCounts.maxByOrNull { it.value }
            if (mostCommonMoodEntry != null) {
                binding.tvMostCommonMood.text = "Most Common Mood: ${mostCommonMoodEntry.key} (Count: ${mostCommonMoodEntry.value})"
            } else {
                binding.tvMostCommonMood.text = "Most Common Mood: N/A"
            }

            // Optional: Add date range
            val sortedMoods = moods.sortedBy { it.timestamp }
            if (sortedMoods.size >= 2) {
                val startDate = DateUtils.formatTimestamp(sortedMoods.first().timestamp)
                val endDate = DateUtils.formatTimestamp(sortedMoods.last().timestamp)
                // Add another TextView or append to existing one if desired
                // e.g., binding.tvDateRange.text = "Date Range: $startDate - $endDate"
            }
        } else {
            binding.tvMostCommonMood.text = "Most Common Mood: N/A"
        }
    }

    private fun updateChart(moods: List<Mood>) {
        updateSummaryStats(moods) // Call summary first

        // Count moods by emoji across all users
        val moodCountMap = mutableMapOf<String, Int>()
        moods.forEach { mood ->
            val count = moodCountMap[mood.emoji] ?: 0
            moodCountMap[mood.emoji] = count + 1
        }

        if (moodCountMap.isEmpty()) {
            binding.barChart.clear()
            binding.barChart.setNoDataText("No mood data available")
            binding.legendContainer.removeAllViews()
            return
        }

        // Sort by count descending for better visualization
        val sortedMoodCounts = moodCountMap.toList().sortedByDescending { it.second }

        // Create entries for the chart
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        var colors = mutableListOf<Int>()

        var index = 0f
        sortedMoodCounts.forEach { (emoji, count) ->
            entries.add(BarEntry(index, count.toFloat()))
            labels.add(emoji)
            val colorIndex = (index % ColorTemplate.COLORFUL_COLORS.size).toInt()
            colors.add(ColorTemplate.COLORFUL_COLORS[colorIndex])
            index++
        }

        val dataSet = BarDataSet(entries, "Mood Count")
        dataSet.apply {
            colors = colors
            valueTextSize = 14f
            valueTextColor = Color.BLACK
            // setDrawValues(false) // Uncomment if you don't want count numbers on bars
        }

        // Set x-axis labels
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < labels.size) labels[index] else ""
            }
        }

        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.animateY(2000, Easing.EaseInOutCubic)
        binding.barChart.invalidate()

        // Update custom legend
        setupLegend(sortedMoodCounts.toMap(), colors) // Pass the sorted map and colors
    }

    private fun setupLegend(moodCountMap: Map<String, Int>, colors: List<Int>) {
        binding.legendContainer.removeAllViews()

        var index = 0
        // Iterate through the sorted map to ensure legend order matches chart
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
                setTextColor(Color.BLACK)
            }

            legendItem.addView(colorBox)
            legendItem.addView(text)
            binding.legendContainer.addView(legendItem)
            index++
        }
    }
}