package org.example

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
import org.example.service.DatabaseService

@Composable
fun SubmissionReviewListView(
    databaseService: DatabaseService,
    onBack: () -> Unit,
    onSelectSubmission: (SubmissionReview) -> Unit
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
            Text(
                text = "Submission Reviews",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = onBack) {
                Text("Back")
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

@Composable
fun StatusChip(status: ReviewStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        ReviewStatus.PENDING -> Triple(MaterialTheme.colors.secondary, MaterialTheme.colors.onSecondary, "Pending")
        ReviewStatus.IN_REVIEW -> Triple(MaterialTheme.colors.primary.copy(alpha = 0.7f), MaterialTheme.colors.onPrimary, "In Review")
        ReviewStatus.APPROVED -> Triple(MaterialTheme.colors.primary, MaterialTheme.colors.onPrimary, "Approved")
        ReviewStatus.PARTIAL -> Triple(MaterialTheme.colors.primaryVariant, MaterialTheme.colors.onPrimary, "Partial")
        ReviewStatus.DECLINED -> Triple(MaterialTheme.colors.error, MaterialTheme.colors.onError, "Declined")
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium
        )
    }
}
