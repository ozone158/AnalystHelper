package org.example.bank

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.model.IndustryFile
import org.example.service.database.DatabaseService
import org.example.service.database.DatabaseResult
import java.io.File

@Composable
fun IndustryFileManagementView(
    databaseService: DatabaseService,
    onBack: () -> Unit
) {
    var selectedIndustry by remember { mutableStateOf<String?>(null) }
    var industryExpanded by remember { mutableStateOf(false) }
    var industryFiles by remember { mutableStateOf<List<IndustryFile>>(emptyList()) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var fileDescription by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    val industries = listOf("Tech", "Energy")
    
    LaunchedEffect(selectedIndustry) {
        if (selectedIndustry != null) {
            industryFiles = databaseService.getIndustryFilesSync(selectedIndustry!!)
        } else {
            industryFiles = emptyList()
        }
    }
    
    fun handleFileUpload() {
        if (selectedFile != null && selectedIndustry != null) {
            val result = databaseService.uploadIndustryFileSync(
                selectedIndustry!!,
                selectedFile!!,
                fileDescription.ifBlank { null }
            )
            successMessage = when (result) {
                is DatabaseResult.Success -> result.message
                is DatabaseResult.Error -> result.message
            }
            showSuccessMessage = true
            showUploadDialog = false
            fileDescription = ""
            selectedFile = null
            // Refresh file list
            industryFiles = databaseService.getIndustryFilesSync(selectedIndustry!!)
        }
    }
    
    fun handleDeleteFile(fileId: String) {
        val result = databaseService.deleteIndustryFileSync(fileId)
        successMessage = when (result) {
            is DatabaseResult.Success -> result.message
            is DatabaseResult.Error -> result.message
        }
        showSuccessMessage = true
        // Refresh file list
        if (selectedIndustry != null) {
            industryFiles = databaseService.getIndustryFilesSync(selectedIndustry!!)
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
                    text = "Industry File Management",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Upload statistics and data files for AI analysis",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Industry selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Industry",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box {
                    OutlinedButton(
                        onClick = { industryExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedIndustry ?: "Select industry"
                        )
                    }
                    DropdownMenu(
                        expanded = industryExpanded,
                        onDismissRequest = { industryExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        industries.forEach { industry ->
                            DropdownMenuItem(onClick = {
                                selectedIndustry = industry
                                industryExpanded = false
                            }) {
                                Text(industry)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Upload button
        if (selectedIndustry != null) {
            Button(
                onClick = { showUploadDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Text(
                    "Upload File for $selectedIndustry Industry",
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Files list
            if (industryFiles.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No files uploaded yet",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Upload CSV or TXT files to provide industry statistics for AI analysis",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Uploaded Files (${industryFiles.size})",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(industryFiles) { file ->
                        IndustryFileItem(
                            file = file,
                            onDelete = { handleDeleteFile(file.id) }
                        )
                    }
                }
            }
        }
    }
    
    // Upload dialog
    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload Industry File") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select a CSV or TXT file to upload for ${selectedIndustry ?: "selected industry"} industry.",
                        style = MaterialTheme.typography.body2
                    )
                    
                    OutlinedTextField(
                        value = fileDescription,
                        onValueChange = { fileDescription = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Market statistics, Industry benchmarks") }
                    )
                    
                    Button(
                        onClick = {
                            val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Select File", java.awt.FileDialog.LOAD)
                            fileDialog.isVisible = true
                            val file = fileDialog.file
                            if (file != null) {
                                selectedFile = File(fileDialog.directory, file)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedFile != null) {
                                "Selected: ${selectedFile!!.name}"
                            } else {
                                "Select CSV or TXT File"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { handleFileUpload() },
                    enabled = selectedFile != null
                ) {
                    Text("Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Success message
    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = { showSuccessMessage = false },
            title = { 
                Text(
                    if (successMessage.contains("Error", ignoreCase = true) || successMessage.contains("Failed", ignoreCase = true)) 
                        "Error" 
                    else 
                        "Success"
                )
            },
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
fun IndustryFileItem(
    file: IndustryFile,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = file.fileName,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium
                )
                if (file.description != null) {
                    Text(
                        text = file.description,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = "Uploaded: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(file.uploadedAt)}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            IconButton(onClick = onDelete) {
                Text("🗑️", style = MaterialTheme.typography.body1)
            }
        }
    }
}
