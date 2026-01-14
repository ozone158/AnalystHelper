package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.model.SubmissionReview
import org.example.model.ReviewStatus
import org.example.model.ReviewNote
import org.example.service.AIService
import org.example.service.DatabaseService
import java.util.Date
import java.util.UUID

@Composable
fun SubmissionDetailView(
    submission: SubmissionReview,
    aiService: AIService,
    databaseService: DatabaseService,
    onBack: () -> Unit
) {
    var currentStatus by remember { mutableStateOf(submission.status) }
    var notes by remember { mutableStateOf(submission.notes) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    
    fun handleStatusChange(newStatus: ReviewStatus) {
        val result = databaseService.updateSubmissionStatusSync(submission.id, newStatus)
        if (result is org.example.service.DatabaseResult.Success) {
            currentStatus = newStatus
            showStatusDialog = false
            successMessage = "Status updated successfully"
            showSuccessMessage = true
        }
    }
    
    fun handleAddNote() {
        if (noteText.isNotBlank()) {
            val note = ReviewNote(
                id = UUID.randomUUID().toString(),
                content = noteText,
                createdAt = Date()
            )
            val result = databaseService.addNoteToSubmissionSync(submission.id, note)
            if (result is org.example.service.DatabaseResult.Success) {
                notes = notes + note
                noteText = ""
                showNoteDialog = false
                successMessage = "Note added successfully"
                showSuccessMessage = true
            }
        }
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
                    text = submission.submissionData.startupName,
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Submission Review",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showStatusDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Change Status")
            }
            Button(
                onClick = { showNoteDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Add Note")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current status
        StatusChip(status = currentStatus)
        
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
                    InfoRow("Startup Name", submission.submissionData.startupName)
                    InfoRow("Industry", submission.submissionData.industry)
                    InfoRow("Stage", submission.submissionData.stage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Problem Statement",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = submission.submissionData.problemStatement,
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
                        text = submission.submissionData.proposedSolution,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // AI Analysis
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI Analysis",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = aiService.formatAnalysisResult(submission.analysisResult),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            
            // Notes Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notes (${notes.size})",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (notes.isEmpty()) {
                        Text(
                            text = "No notes yet",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        notes.forEach { note ->
                            NoteItem(note)
                            if (notes.indexOf(note) < notes.size - 1) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Status change dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Status") },
            text = {
                Column {
                    ReviewStatus.values().forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = currentStatus == status,
                                onClick = { currentStatus = status }
                            )
                            Text(
                                text = status.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { handleStatusChange(currentStatus) }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Add note dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Add Note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            },
            confirmButton = {
                Button(onClick = { handleAddNote() }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Success message
    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = { showSuccessMessage = false },
            title = { Text("Success") },
            text = { Text(successMessage) },
            confirmButton = {
                Button(onClick = { showSuccessMessage = false }) {
                    Text("OK")
                }
            }
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
