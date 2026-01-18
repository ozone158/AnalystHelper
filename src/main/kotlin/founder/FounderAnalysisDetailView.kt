package org.example.founder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.model.SubmissionReview
import org.example.service.ai.AIService
import org.example.shared.StatusChip
import org.example.shared.InfoRow
import org.example.shared.NoteItem

@Composable
fun FounderAnalysisDetailView(
    analysis: SubmissionReview,
    aiService: AIService,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = analysis.submissionData.startupName,
                style = MaterialTheme.typography.h4
            )
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Current status (read-only for founders)
        StatusChip(status = analysis.status)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Submission Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Submission Information",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    InfoRow("Startup Name", analysis.submissionData.startupName)
                    InfoRow("Industry", analysis.submissionData.industry)
                    InfoRow("Stage", analysis.submissionData.stage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Problem Statement",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = analysis.submissionData.problemStatement,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Proposed Solution",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = analysis.submissionData.proposedSolution,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // AI Analysis Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Analysis Summary",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Overall Score:",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", analysis.analysisResult.overallScore)}/5",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recommendation:",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = analysis.analysisResult.recommendation.name,
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Full AI Analysis
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Full AI Analysis",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = aiService.formatAnalysisResult(analysis.analysisResult),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            
            // Bank Notes Section (read-only for founders)
            if (analysis.notes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Bank Notes (${analysis.notes.size})",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        analysis.notes.forEach { note ->
                            NoteItem(note)
                            if (analysis.notes.indexOf(note) < analysis.notes.size - 1) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
