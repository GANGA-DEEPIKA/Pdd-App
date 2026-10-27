package com.example.campusfix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PerformanceSummaryScreen(
    complaintList: List<ComplaintData> = emptyList(),
    onBackClick: () -> Unit
) {
    val total = complaintList.size
    val resolved = complaintList.count { it.status == "Resolved" }
    val pending = complaintList.count { it.status == "Pending" || it.status == "Assigned" || it.status.startsWith("Waiting") }
    val inProgress = complaintList.count { it.status == "In Progress" }
    val resolveRate = if (total > 0) ((resolved.toFloat() / total.toFloat()) * 100).toInt() else 0

    // Category Breakdown Calculation
    val categoryStats = complaintList
        .groupBy { if (it.category.isNotBlank()) it.category else "General Maintenance" }
        .map { (cat, list) ->
            CategoryStatData(
                category = cat,
                total = list.size,
                resolved = list.count { it.status == "Resolved" }
            )
        }
        .sortedByDescending { it.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // App Bar Header (Dark Blue)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF103480),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Performance Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Main Content Scroll Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Overview Header
            Text(
                text = "Overview",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Overview Metric Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricOverviewCard(
                    value = "$total",
                    label = "Total Complaints",
                    bgColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
                MetricOverviewCard(
                    value = "$resolved",
                    label = "Resolved",
                    bgColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricOverviewCard(
                    value = "$pending",
                    label = "Pending",
                    bgColor = Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
                MetricOverviewCard(
                    value = "$inProgress",
                    label = "In Progress",
                    bgColor = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resolution Rate Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resolution Rate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "$resolveRate%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (resolveRate >= 70) Color(0xFF2E7D32) else if (resolveRate >= 40) Color(0xFFD97706) else Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Track Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val fillFraction = (resolveRate.toFloat() / 100f).coerceIn(0f, 1f)
                            if (fillFraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fillFraction)
                                        .background(
                                            color = Color(0xFF4CAF50),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val rateCaption = when {
                        resolveRate >= 70 -> "🌟 Excellent performance!"
                        resolveRate >= 40 -> "📈 Good progress, keep going!"
                        else -> "⚠️ Needs improvement"
                    }

                    Text(
                        text = rateCaption,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Breakdown Card
            if (categoryStats.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Category Breakdown",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        categoryStats.forEachIndexed { index, stat ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stat.category,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "${stat.resolved}/${stat.total} resolved",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Correct Category Progress Bar
                                // Fill fraction is resolved / total so 2/3 resolved shows 67% filled
                                val categoryFillFraction = if (stat.total > 0) {
                                    (stat.resolved.toFloat() / stat.total.toFloat()).coerceIn(0f, 1f)
                                } else 0f

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF3F4F6)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (categoryFillFraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(categoryFillFraction)
                                                    .background(
                                                        color = Color(0xFF1565C0),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                            )
                                        }
                                    }
                                }

                                if (index < categoryStats.size - 1) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MetricOverviewCard(
    value: String,
    label: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(85.dp),
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

data class CategoryStatData(
    val category: String,
    val total: Int,
    val resolved: Int
)
