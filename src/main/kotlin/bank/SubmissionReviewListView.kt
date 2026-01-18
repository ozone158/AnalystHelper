package org.example.bank

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
import org.example.model.ReviewStatus
import org.example.service.database.DatabaseService
import org.example.shared.StatusChip

@Composable
fun SubmissionReviewListView(
    databaseService: DatabaseService,
    onBack: () -> Unit,
    onSelectSubmission: (SubmissionReview) -> Unit,
    onManageIndustryFiles: () -> Unit = {},
    onManageCriteria: () -> Unit = {}
) {
    var submissions by remember { mutableStateOf<List<SubmissionReview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        submissions = databaseService.fetchSubmissionsSync()
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
                    text = "Submission Reviews",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bank Officer Portal",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onManageCriteria) {
                    Text("Manage Criteria")
                }
                OutlinedButton(onClick = onManageIndustryFiles) {
                    Text("Manage Industry Files")
                }
                Button(onClick = onBack) {
                    Text("Back")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submissions list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (submissions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No submissions available",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(submissions) { submission ->
                    SubmissionReviewItem(
                        submission = submission,
                        onClick = { onSelectSubmission(submission) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubmissionReviewItem(
    submission: SubmissionReview,
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
                    text = submission.submissionData.startupName,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Industry: ${submission.submissionData.industry}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Stage: ${submission.submissionData.stage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.body2
                )
            }
            
            StatusChip(status = submission.status)
        }
    }
}
