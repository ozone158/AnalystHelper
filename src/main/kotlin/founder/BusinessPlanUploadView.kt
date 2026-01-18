package org.example.founder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.service.ai.AIService
import org.example.service.DocumentProcessor
import java.io.File

/**
 * View for uploading business plan document and detecting industry
 */
@Composable
fun BusinessPlanUploadView(
    onBack: () -> Unit,
    aiService: AIService,
    onIndustryDetected: (String, File, org.example.model.ExtractedStartupInfo) -> Unit
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var detectedIndustry by remember { mutableStateOf<String?>(null) }
    var extractedStartupInfo by remember { mutableStateOf<org.example.model.ExtractedStartupInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var extractedText by remember { mutableStateOf<String?>(null) }
    
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
            Column {
                Text(
                    text = "Upload Business Plan",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Step 1: Upload your business plan document",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Instructions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📄 Instructions",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please upload your business plan document. Our AI will automatically detect which industry your startup belongs to based on the content.",
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Supported formats: PDF, TXT",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // File upload section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedFile == null) {
                        Text(
                            text = "Select Business Plan Document",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Button(
                            onClick = {
                                val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Business Plan Document", java.awt.FileDialog.LOAD)
                                fileDialog.isVisible = true
                                val file = fileDialog.file
                                if (file != null) {
                                    val selectedFileObj = File(fileDialog.directory, file)
                                    if (DocumentProcessor.isSupportedFormat(selectedFileObj)) {
                                        selectedFile = selectedFileObj
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Unsupported file format. Please upload a PDF or TXT file."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Choose File")
                        }
                    } else {
                        // File selected
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Selected File:",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedFile!!.name,
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            TextButton(onClick = {
                                selectedFile = null
                                detectedIndustry = null
                                extractedText = null
                                errorMessage = null
                            }) {
                                Text("Change")
                            }
                        }
                        
                        if (!isProcessing && detectedIndustry == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isProcessing = true
                                    errorMessage = null
                                    
                                    // Extract text from document
                                    val text = selectedFile?.let { DocumentProcessor.extractText(it) }
                                    if (text == null || text.isBlank()) {
                                        errorMessage = "Failed to extract text from the document. Please ensure the file is not corrupted and try again."
                                        isProcessing = false
                                    } else {
                                        extractedText = text
                                        
                                        // Detect industry and extract startup info using AI
                                        try {
                                            val industry = aiService.detectIndustrySync(text)
                                            detectedIndustry = industry
                                            
                                            // Extract startup information
                                            val startupInfo = aiService.extractStartupInfoSync(text)
                                            extractedStartupInfo = startupInfo
                                            
                                            if (industry == null) {
                                                errorMessage = "Unable to determine industry from the document. Please select the industry manually."
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error analyzing document: ${e.message}"
                                        } finally {
                                            isProcessing = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = !isProcessing
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                } else {
                                    Text("Detect Industry")
                                }
                            }
                        }
                    }
                }
            }
            
            // Processing indicator
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "Analyzing your business plan...",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Detecting industry and extracting key information from document",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Error",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "An error occurred",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
            
            // Industry detected result
            if (detectedIndustry != null && !isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    backgroundColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "✓ Industry Detected",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "Your startup has been classified as:",
                            style = MaterialTheme.typography.body1
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 2.dp,
                            backgroundColor = MaterialTheme.colors.primary
                        ) {
                            Text(
                                text = detectedIndustry ?: "Unknown",
                                style = MaterialTheme.typography.h4,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Click Continue to proceed with industry-specific evaluation questions",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                        Button(
                            onClick = {
                                if (selectedFile != null && detectedIndustry != null) {
                                    onIndustryDetected(
                                        detectedIndustry!!, 
                                        selectedFile!!,
                                        extractedStartupInfo ?: org.example.model.ExtractedStartupInfo()
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Continue to Questions")
                        }
                    }
                }
            }
        }
    }
}
