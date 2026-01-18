package org.example.founder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.model.SubmissionReview
import org.example.service.database.DatabaseService
import org.example.shared.StatusChip

@Composable
fun FounderAnalysisListView(
    databaseService: DatabaseService,
    onBack: () -> Unit,
    onSelectAnalysis: (SubmissionReview) -> Unit
) {
    var analyses by remember { mutableStateOf<List<SubmissionReview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        analyses = databaseService.fetchSubmissionsSync()
        isLoading = false
    }
    
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
            Column {
                Text(
                    text = "My Analyses",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BMO Startup Evaluations",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Analyses list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (analyses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No analyses available yet",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Submit your startup idea to get an analysis",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(analyses) { analysis ->
                    FounderAnalysisItem(
                        analysis = analysis,
                        onClick = { onSelectAnalysis(analysis) }
                    )
                }
            }
        }
    }
}

@Composable
fun FounderAnalysisItem(
    analysis: SubmissionReview,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = analysis.submissionData.startupName,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Industry: ${analysis.submissionData.industry}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Stage: ${analysis.submissionData.stage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Score: ${String.format("%.1f", analysis.analysisResult.overallScore)}/5",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Recommendation: ${analysis.analysisResult.recommendation.name}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            StatusChip(status = analysis.status)
        }
    }
}
