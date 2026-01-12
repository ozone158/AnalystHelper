package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.example.service.AIService
import org.example.service.DatabaseService
import org.example.service.DatabaseResult
import java.io.File

data class StartupSubmissionData(
    val startupName: String,
    val industry: String,
    val problemStatement: String,
    val proposedSolution: String,
    val stage: StartupStage,
    val files: List<FileEntry>
)

@Composable
fun AnalysisView(
    submissionData: StartupSubmissionData,
    onBack: () -> Unit,
    aiService: AIService,
    databaseService: DatabaseService,
    onSubmitToBMO: (DatabaseResult) -> Unit = {}
) {
    var showExportSuccess by remember { mutableStateOf(false) }
    var showSubmitSuccess by remember { mutableStateOf(false) }
    var submitMessage by remember { mutableStateOf("") }
    
    val analysisResult = remember(submissionData) {
        aiService.analyzeSubmissionSync(submissionData)
    }
    
    val analysisText = remember(analysisResult) {
        aiService.formatAnalysisResult(analysisResult)
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
                text = "Analysis Results",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Analysis content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Startup name
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Startup: ${submissionData.startupName}",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "Industry: ${submissionData.industry}",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Stage: ${submissionData.stage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // AI Analysis section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI Analysis",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = analysisText,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Export PDF button
                Button(
                    onClick = {
                        exportToPDF(submissionData, aiService)
                        showExportSuccess = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Export as PDF")
                }
                
                // Submit to BMO button
                Button(
                    onClick = {
                        val result = databaseService.submitToDatabaseSync(submissionData)
                        submitMessage = when (result) {
                            is DatabaseResult.Success -> result.message
                            is DatabaseResult.Error -> result.message
                        }
                        onSubmitToBMO(result)
                        showSubmitSuccess = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Submit to BMO")
                }
            }
        }
    }
    
    // Export success dialog
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Export Successful") },
            text = { Text("Analysis has been exported as PDF successfully.") },
            confirmButton = {
                Button(onClick = { showExportSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Submit success dialog
    if (showSubmitSuccess) {
        AlertDialog(
            onDismissRequest = { showSubmitSuccess = false },
            title = { 
                Text(if (submitMessage.contains("Error", ignoreCase = true) || submitMessage.contains("Failed", ignoreCase = true)) 
                    "Submission Error" 
                else 
                    "Submission Successful")
            },
            text = { Text(submitMessage.ifEmpty { "Your submission has been sent to BMO for review." }) },
            confirmButton = {
                Button(onClick = { showSubmitSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }
}


// Export analysis to PDF
fun exportToPDF(data: StartupSubmissionData, aiService: AIService) {
    val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Save PDF", java.awt.FileDialog.SAVE)
    fileDialog.file = "${data.startupName.replace(" ", "_")}_Analysis.pdf"
    fileDialog.isVisible = true
    
    val fileName = fileDialog.file
    val directory = fileDialog.directory
    
    if (fileName != null && directory != null) {
        val file = File(directory, fileName)
        
        try {
            val document = PDDocument()
            val page = PDPage()
            document.addPage(page)
            
            var contentStream = PDPageContentStream(document, page)
            val font = PDType1Font(Standard14Fonts.FontName.HELVETICA)
            val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
            
            var yPosition = 750f
            val margin = 50f
            val lineHeight = 15f
            
            fun addText(text: String, isBold: Boolean = false) {
                if (yPosition < 50) {
                    contentStream.close()
                    val newPage = PDPage()
                    document.addPage(newPage)
                    contentStream = PDPageContentStream(document, newPage)
                    yPosition = 750f
                }
                contentStream.beginText()
                contentStream.setFont(if (isBold) boldFont else font, 12f)
                contentStream.newLineAtOffset(margin, yPosition)
                contentStream.showText(text)
                contentStream.endText()
                yPosition -= lineHeight
            }
            
            // Title
            contentStream.beginText()
            contentStream.setFont(boldFont, 18f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText("Startup Analysis Report")
            contentStream.endText()
            yPosition -= 30f
            
            // Startup Information
            addText("Startup Information", true)
            yPosition -= 5f
            addText("Startup Name: ${data.startupName}")
            addText("Industry: ${data.industry}")
            addText("Stage: ${data.stage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }}")
            yPosition -= 10f
            
            // Problem Statement
            addText("Problem Statement", true)
            yPosition -= 5f
            data.problemStatement.split("\n").forEach { line ->
                addText(line)
            }
            yPosition -= 10f
            
            // Proposed Solution
            addText("Proposed Solution", true)
            yPosition -= 5f
            data.proposedSolution.split("\n").forEach { line ->
                addText(line)
            }
            yPosition -= 10f
            
            // AI Analysis
            addText("AI Analysis", true)
            yPosition -= 5f
            val analysisResult = aiService.analyzeSubmissionSync(data)
            val analysisText = aiService.formatAnalysisResult(analysisResult)
            analysisText.split("\n").forEach { line ->
                if (line.isNotBlank()) {
                    addText(line)
                }
            }
            
            contentStream.close()
            document.save(file)
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

