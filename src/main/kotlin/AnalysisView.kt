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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.example.service.ai.AIService
import org.example.service.database.DatabaseService
import org.example.service.database.DatabaseResult
import org.example.model.StartupSubmissionData
import java.io.File

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
    
    // Track expanded state for categories and criteria
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedCriteria by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedRisks by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showRecommendationReasoning by remember { mutableStateOf(false) }
    
    // Loading and analysis state
    var isLoading by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<org.example.model.AnalysisResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    
    // Load analysis asynchronously
    LaunchedEffect(submissionData, retryTrigger) {
        if (retryTrigger == 0 || retryTrigger > 0) {
            isLoading = true
            errorMessage = null
            try {
                // Get industry files for this submission's industry
                val industryFiles = databaseService.getIndustryFilesSync(submissionData.industry)
                
                val result = aiService.analyzeSubmission(submissionData, industryFiles)
                // Automatically save analysis to local storage
                databaseService.saveAnalysisSync(submissionData, result)
                analysisResult = result
            } catch (e: Exception) {
                errorMessage = "Failed to analyze submission: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
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
            Column {
                Text(
                    text = "Analysis Results",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BMO AI Evaluation",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Show loading, error, or analysis content
        when {
            isLoading -> {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Analyzing your submission...",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "This may take a few moments",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            errorMessage != null -> {
                // Error message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        elevation = 4.dp,
                        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "⚠️ Analysis Error",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.error
                            )
                            Text(
                                text = errorMessage ?: "Unknown error occurred",
                                style = MaterialTheme.typography.body1
                            )
                            Button(
                                onClick = {
                                    retryTrigger++
                                }
                            ) {
                                Text("Retry")
                            }
                            
                            LaunchedEffect(retryTrigger) {
                                if (retryTrigger > 0) {
                                    try {
                                        val industryFiles = databaseService.getIndustryFilesSync(submissionData.industry)
                                        val result = aiService.analyzeSubmission(submissionData, industryFiles)
                                        databaseService.saveAnalysisSync(submissionData, result)
                                        analysisResult = result
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to analyze submission: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
            analysisResult != null -> {
                // Analysis content
                AnalysisContent(
                    submissionData = submissionData,
                    analysisResult = analysisResult!!,
                    showRecommendationReasoning = showRecommendationReasoning,
                    onToggleRecommendationReasoning = { showRecommendationReasoning = !showRecommendationReasoning },
                    onExportPDF = {
                        exportToPDF(submissionData, aiService)
                        showExportSuccess = true
                    },
                    onSubmitToBMO = {
                        val result = databaseService.submitToDatabaseSync(submissionData)
                        submitMessage = when (result) {
                            is DatabaseResult.Success -> result.message
                            is DatabaseResult.Error -> result.message
                        }
                        onSubmitToBMO(result)
                        showSubmitSuccess = true
                    }
                )
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

@Composable
private fun AnalysisContent(
    submissionData: StartupSubmissionData,
    analysisResult: org.example.model.AnalysisResult,
    showRecommendationReasoning: Boolean,
    onToggleRecommendationReasoning: () -> Unit,
    onExportPDF: () -> Unit,
    onSubmitToBMO: () -> Unit
) {
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
            
            // Data Completeness Assessment Card - AI's assessment of information sufficiency
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 3.dp,
                backgroundColor = when (analysisResult.dataQuality.completeness) {
                    org.example.model.Completeness.COMPLETE -> MaterialTheme.colors.primaryVariant.copy(alpha = 0.1f)
                    org.example.model.Completeness.PARTIAL -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    org.example.model.Completeness.INCOMPLETE -> MaterialTheme.colors.error.copy(alpha = 0.1f)
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📊 Data Completeness Assessment",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            // Status badge
                            Card(
                                backgroundColor = when (analysisResult.dataQuality.completeness) {
                                    org.example.model.Completeness.COMPLETE -> MaterialTheme.colors.primaryVariant
                                    org.example.model.Completeness.PARTIAL -> MaterialTheme.colors.primary
                                    org.example.model.Completeness.INCOMPLETE -> MaterialTheme.colors.error
                                },
                                elevation = 0.dp
                            ) {
                                Text(
                                    text = analysisResult.dataQuality.completeness.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Impact on analysis
                    Text(
                        text = "Impact on Analysis:",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium,
                        color = when (analysisResult.dataQuality.completeness) {
                            org.example.model.Completeness.COMPLETE -> MaterialTheme.colors.primaryVariant
                            org.example.model.Completeness.PARTIAL -> MaterialTheme.colors.primary
                            org.example.model.Completeness.INCOMPLETE -> MaterialTheme.colors.error
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = analysisResult.dataQuality.impactOnAnalysis,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    // Show gaps if any
                    if (analysisResult.dataQuality.gaps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Information Gaps Identified:",
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Medium,
                            color = when (analysisResult.dataQuality.completeness) {
                                org.example.model.Completeness.COMPLETE -> MaterialTheme.colors.primaryVariant
                                org.example.model.Completeness.PARTIAL -> MaterialTheme.colors.primary
                                org.example.model.Completeness.INCOMPLETE -> MaterialTheme.colors.error
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        analysisResult.dataQuality.gaps.forEach { gap ->
                            Text(
                                text = "• $gap",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Check for insufficient data
            val hasInsufficientData = analysisResult.categoryScores.any { category ->
                category.criteriaScores.any { it.score == 0 && it.insufficientData }
            }
            val missingDataCriteria = analysisResult.categoryScores.flatMap { category ->
                category.criteriaScores.filter { it.score == 0 && it.insufficientData }
            }
            
            // Overall Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Score",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", analysisResult.overallScore)}/5",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = if (hasInsufficientData && analysisResult.overallScore == 0.0) 
                                MaterialTheme.colors.error 
                            else 
                                MaterialTheme.colors.primary
                        )
                    }
                    
                    // Show warning if insufficient data
                    if (hasInsufficientData) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                            elevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "⚠️ Incomplete Analysis",
                                    style = MaterialTheme.typography.body2,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Some criteria could not be evaluated due to insufficient information. Please submit additional files to get a complete analysis.",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.error
                                )
                                if (missingDataCriteria.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Missing information for:",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colors.error
                                    )
                                    missingDataCriteria.forEach { criterion ->
                                        if (!criterion.dataRequired.isNullOrBlank()) {
                                            Text(
                                                text = "• ${criterion.criterionName}: ${criterion.dataRequired}",
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                                                color = MaterialTheme.colors.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommendation: ${analysisResult.recommendation.name}",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(onClick = onToggleRecommendationReasoning) {
                            Text(if (showRecommendationReasoning) "▲" else "▼")
                        }
                    }
                    if (showRecommendationReasoning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = analysisResult.recommendationReasoning,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // Category Scores
            Text(
                text = "Category Scores",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            analysisResult.categoryScores.forEach { category ->
                var isExpanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.categoryName,
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Weight: ${String.format("%.0f", category.categoryWeight * 100)}%",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", category.categoryScore)}/5",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.primary
                                )
                                TextButton(onClick = { isExpanded = !isExpanded }) {
                                    Text(if (isExpanded) "▲" else "▼")
                                }
                            }
                        }
                        
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Category Reasoning
                            Text(
                                text = "Reasoning:",
                                style = MaterialTheme.typography.body2,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = category.categoryReasoning,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                            )
                            
                            // Criteria Scores
                            Text(
                                text = "Criteria:",
                                style = MaterialTheme.typography.body2,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            category.criteriaScores.forEach { criterion ->
                                var criterionExpanded by remember { mutableStateOf(false) }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    elevation = 1.dp,
                                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = criterion.criterionName,
                                                style = MaterialTheme.typography.body2,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "${criterion.score}/5",
                                                    style = MaterialTheme.typography.body1,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (criterion.score == 0 && criterion.insufficientData) 
                                                        MaterialTheme.colors.error 
                                                    else 
                                                        MaterialTheme.colors.primary
                                                )
                                                TextButton(
                                                    onClick = { criterionExpanded = !criterionExpanded }
                                                ) {
                                                    Text(
                                                        text = if (criterionExpanded) "▲" else "▼",
                                                        style = MaterialTheme.typography.caption
                                                    )
                                                }
                                            }
                                        }
                                        
                                        if (criterionExpanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            // Show insufficient data warning if applicable
                                            if (criterion.score == 0 && criterion.insufficientData) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 8.dp),
                                                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                                                    elevation = 0.dp
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp)
                                                    ) {
                                                        Text(
                                                            text = "⚠️ Insufficient Information",
                                                            style = MaterialTheme.typography.body2,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colors.error
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Score set to 0 due to missing information. Please submit additional files to enable proper evaluation.",
                                                            style = MaterialTheme.typography.caption,
                                                            color = MaterialTheme.colors.error
                                                        )
                                                        if (!criterion.dataRequired.isNullOrBlank()) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Required: ${criterion.dataRequired}",
                                                                style = MaterialTheme.typography.caption,
                                                                fontWeight = FontWeight.Medium,
                                                                color = MaterialTheme.colors.error
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            Text(
                                                text = criterion.reasoning,
                                                style = MaterialTheme.typography.body2,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                            if (criterion.supportingEvidence.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Supporting Evidence:",
                                                    style = MaterialTheme.typography.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                                )
                                                criterion.supportingEvidence.forEach { evidence ->
                                                    Text(
                                                        text = "• $evidence",
                                                        style = MaterialTheme.typography.caption,
                                                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Risk Assessment
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Risk Assessment",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            
            val risks = listOf(
                "Privacy/Security" to analysisResult.riskAssessment.privacySecurity,
                "Compliance" to analysisResult.riskAssessment.compliance,
                "Market" to analysisResult.riskAssessment.market,
                "Technical" to analysisResult.riskAssessment.technical
            )
            
            risks.forEach { (name, risk) ->
                var isExpanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = risk.level.name,
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium,
                                    color = when (risk.level) {
                                        org.example.model.RiskLevel.HIGH -> MaterialTheme.colors.error
                                        org.example.model.RiskLevel.MEDIUM -> MaterialTheme.colors.primary
                                        org.example.model.RiskLevel.LOW -> MaterialTheme.colors.primaryVariant
                                    }
                                )
                                TextButton(onClick = { isExpanded = !isExpanded }) {
                                    Text(if (isExpanded) "▲" else "▼")
                                }
                            }
                        }
                        
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = risk.description,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            if (risk.concerns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Concerns:",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                                risk.concerns.forEach { concern ->
                                    Text(
                                        text = "• $concern",
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Key Strengths
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Key Strengths",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    analysisResult.keyStrengths.forEach { strength ->
                        Text(
                            text = "• $strength",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }
            
            // Key Concerns
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Key Concerns",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    analysisResult.keyConcerns.forEach { concern ->
                        Text(
                            text = "• $concern",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }
            
            // Qualitative Forecast
            if (analysisResult.qualitativeForecast != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 3.dp,
                    backgroundColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.05f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔮 Qualitative Forecast",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Short-term Outlook
                        Text(
                            text = "Short-term Outlook (6-12 months)",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analysisResult.qualitativeForecast.shortTermOutlook,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Medium-term Prospects
                        Text(
                            text = "Medium-term Prospects (1-3 years)",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analysisResult.qualitativeForecast.mediumTermProspects,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Long-term Potential
                        Text(
                            text = "Long-term Potential (3-5+ years)",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analysisResult.qualitativeForecast.longTermPotential,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Key Success Factors
                        Text(
                            text = "Key Success Factors",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        analysisResult.qualitativeForecast.keySuccessFactors.forEach { factor ->
                            Text(
                                text = "✓ $factor",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Potential Challenges
                        Text(
                            text = "Potential Challenges",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        analysisResult.qualitativeForecast.potentialChallenges.forEach { challenge ->
                            Text(
                                text = "⚠ $challenge",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Market Trends Impact
                        Text(
                            text = "Market Trends Impact",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analysisResult.qualitativeForecast.marketTrendsImpact,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
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
                    onClick = onExportPDF,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Export as PDF")
                }
                
                // Submit to BMO button
                Button(
                    onClick = onSubmitToBMO,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Submit to BMO")
                }
            }
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
            // Note: For PDF export, we don't have access to industry files here
            // This is a limitation - in production, you might want to store industry files with the analysis
            val analysisResult = aiService.analyzeSubmissionSync(data, emptyList())
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

