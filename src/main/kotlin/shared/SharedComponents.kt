package org.example.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.model.ReviewStatus
import org.example.model.ReviewNote

@Composable
fun StatusChip(status: ReviewStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        ReviewStatus.PENDING -> Triple(
            org.example.theme.BMOColors.BMOInfo.copy(alpha = 0.1f),
            org.example.theme.BMOColors.BMOInfo,
            "Pending"
        )
        ReviewStatus.IN_REVIEW -> Triple(
            org.example.theme.BMOColors.BMOWarning.copy(alpha = 0.1f),
            org.example.theme.BMOColors.BMOWarning,
            "In Review"
        )
        ReviewStatus.APPROVED -> Triple(
            org.example.theme.BMOColors.BMOSuccess.copy(alpha = 0.1f),
            org.example.theme.BMOColors.BMOSuccess,
            "Approved"
        )
        ReviewStatus.PARTIAL -> Triple(
            org.example.theme.BMOColors.BMOAccentBlue.copy(alpha = 0.1f),
            org.example.theme.BMOColors.BMOAccentBlue,
            "Partial"
        )
        ReviewStatus.DECLINED -> Triple(
            org.example.theme.BMOColors.BMOError.copy(alpha = 0.1f),
            org.example.theme.BMOColors.BMOError,
            "Declined"
        )
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

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
fun NoteItem(note: ReviewNote) {
    Column {
        Text(
            text = note.content,
            style = MaterialTheme.typography.body2
        )
        Text(
            text = "${note.createdBy} - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(note.createdAt)}",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
