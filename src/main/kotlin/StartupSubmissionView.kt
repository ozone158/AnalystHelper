package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

enum class StartupStage {
    IDEA,
    MVP,
    EARLY_REVENUE
}

data class FileEntry(
    val id: Int,
    var usage: String = "",
    var file: File? = null
)

@Composable
fun StartupSubmissionView(onBack: () -> Unit, onSubmit: (StartupSubmissionData) -> Unit = {}) {
    var startupName by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var problemStatement by remember { mutableStateOf("") }
    var proposedSolution by remember { mutableStateOf("") }
    var selectedStage by remember { mutableStateOf<StartupStage?>(null) }
    var stageExpanded by remember { mutableStateOf(false) }
    var fileEntries by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var nextFileId by remember { mutableStateOf(0) }
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()
        
        if (startupName.isBlank()) {
            errors.add("Startup name")
        }
        if (industry.isBlank()) {
            errors.add("Industry")
        }
        if (problemStatement.isBlank()) {
            errors.add("Problem statement")
        }
        if (proposedSolution.isBlank()) {
            errors.add("Proposed solution")
        }
        if (selectedStage == null) {
            errors.add("Stage")
        }
        // Validate file entries if any exist
        fileEntries.forEachIndexed { index, entry ->
            if (entry.usage.isBlank() && entry.file != null) {
                errors.add("Usage for file ${index + 1}")
            }
            if (entry.file == null && entry.usage.isNotBlank()) {
                errors.add("File upload for file ${index + 1}")
            }
        }
        
        if (errors.isNotEmpty()) {
            validationErrorMessage = "Please fill in the following fields:\n" + errors.joinToString("\n")
            showValidationError = true
            return false
        }
        return true
    }
    
    fun handleSubmit() {
        if (validateForm()) {
            showConfirmDialog = true
        }
    }
    
    fun confirmSubmit() {
        if (selectedStage != null) {
            val submissionData = StartupSubmissionData(
                startupName = startupName,
                industry = industry,
                problemStatement = problemStatement,
                proposedSolution = proposedSolution,
                stage = selectedStage!!,
                files = fileEntries.filter { it.file != null }
            )
            onSubmit(submissionData)
            showConfirmDialog = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        // Header with Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Startup Submission",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Form content with scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Startup name
            OutlinedTextField(
                value = startupName,
                onValueChange = { startupName = it },
                label = { Text("Startup name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Industry
            OutlinedTextField(
                value = industry,
                onValueChange = { industry = it },
                label = { Text("Industry") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Problem statement
            OutlinedTextField(
                value = problemStatement,
                onValueChange = { problemStatement = it },
                label = { Text("Problem statement") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Proposed solution
            OutlinedTextField(
                value = proposedSolution,
                onValueChange = { proposedSolution = it },
                label = { Text("Proposed solution") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Stage dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Stage",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box {
                    OutlinedButton(
                        onClick = { stageExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedStage?.let { 
                                it.name.lowercase().replace("_", " ").replaceFirstChar { char -> char.uppercase() }
                            } ?: "Select stage"
                        )
                    }
                    DropdownMenu(
                        expanded = stageExpanded,
                        onDismissRequest = { stageExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StartupStage.values().forEach { stage ->
                            DropdownMenuItem(onClick = {
                                selectedStage = stage
                                stageExpanded = false
                            }) {
                                Text(
                                    text = stage.name.lowercase().replace("_", " ").replaceFirstChar { char -> char.uppercase() }
                                )
                            }
                        }
                    }
                }
            }
            
            // File upload section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add new file button
                Button(
                    onClick = {
                        fileEntries = fileEntries + FileEntry(id = nextFileId)
                        nextFileId++
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add new file")
                }
                
                // List of file entries
                fileEntries.forEachIndexed { index, entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // First segment: Usage name input
                            OutlinedTextField(
                                value = entry.usage,
                                onValueChange = { newUsage ->
                                    fileEntries = fileEntries.mapIndexed { i, e ->
                                        if (i == index) e.copy(usage = newUsage) else e
                                    }
                                },
                                label = { Text("Usage") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            // Second segment: File upload button
                            Button(
                                onClick = {
                                    val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Select File", java.awt.FileDialog.LOAD)
                                    fileDialog.isVisible = true
                                    val file = fileDialog.file
                                    if (file != null) {
                                        val selectedFile = File(fileDialog.directory, file)
                                        fileEntries = fileEntries.mapIndexed { i, e ->
                                            if (i == index) e.copy(file = selectedFile) else e
                                        }
                                    }
                                },
                                modifier = Modifier.widthIn(min = 120.dp)
                            ) {
                                Text(
                                    text = if (entry.file != null) {
                                        val name = entry.file!!.name
                                        if (name.length > 20) name.take(17) + "..." else name
                                    } else "Upload file",
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit button
            Button(
                onClick = { handleSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Text("Submit for evaluation", style = MaterialTheme.typography.body1)
            }
        }
    }
    
    // Validation error dialog
    if (showValidationError) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            title = {
                Text("Validation Error")
            },
            text = {
                Text(validationErrorMessage)
            },
            confirmButton = {
                Button(onClick = { showValidationError = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text("Confirm Submission")
            },
            text = {
                Text("Are you sure you want to submit your startup idea for AI analysis?")
            },
            confirmButton = {
                Button(onClick = { confirmSubmit() }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
