package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.service.AIService
import org.example.service.DatabaseService
import org.example.service.DefaultDatabaseService
import org.example.service.AIServiceFactory
import org.example.theme.BMTheme

enum class ViewType {
    MAIN,
    BANK,
    FOUNDER,
    STARTUP_SUBMISSION,
    ANALYSIS,
    SUBMISSION_DETAIL,
    FOUNDER_ANALYSIS_LIST,
    FOUNDER_ANALYSIS_DETAIL
}

@Composable
fun MainView(onNavigateToBank: () -> Unit, onNavigateToFounder: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // BMO Header
        Text(
            text = "BMO",
            style = MaterialTheme.typography.h3,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Startup Analyst",
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bank View Button
        Button(
            onClick = onNavigateToBank,
            modifier = Modifier
                .width(240.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            ),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                "Bank Officer Portal",
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Founder View Button
        OutlinedButton(
            onClick = onNavigateToFounder,
            modifier = Modifier
                .width(240.dp)
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.primary
            ),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colors.primary
            )
        ) {
            Text(
                "Founder Portal",
                style = MaterialTheme.typography.button,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Footer text
        Text(
            text = "Bank of Montreal",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
        )
    }
}

fun main() = application {
    var currentView by remember { mutableStateOf(ViewType.MAIN) }
    var submissionData by remember { mutableStateOf<StartupSubmissionData?>(null) }
    var selectedSubmissionReview by remember { mutableStateOf<org.example.model.SubmissionReview?>(null) }
    
    // Initialize services from configuration
    // Edit AIConfig.kt to configure AI provider and API key
    val aiService: AIService = remember { 
        AIServiceFactory.createFromConfig()
    }
    val databaseService: DatabaseService = remember { DefaultDatabaseService() }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "BMO Startup Analyst",
        state = rememberWindowState(width = 1000.dp, height = 700.dp)
    ) {
        BMTheme {
            when (currentView) {
                ViewType.MAIN -> MainView(
                    onNavigateToBank = { currentView = ViewType.BANK },
                    onNavigateToFounder = { currentView = ViewType.FOUNDER }
                )
                ViewType.BANK -> SubmissionReviewListView(
                    databaseService = databaseService,
                    onBack = { currentView = ViewType.MAIN },
                    onSelectSubmission = { submission ->
                        selectedSubmissionReview = submission
                        currentView = ViewType.SUBMISSION_DETAIL
                    }
                )
                ViewType.SUBMISSION_DETAIL -> {
                    selectedSubmissionReview?.let { submission ->
                        SubmissionDetailView(
                            submission = submission,
                            aiService = aiService,
                            databaseService = databaseService,
                            onBack = { currentView = ViewType.BANK }
                        )
                    } ?: run {
                        // Fallback if no submission selected
                        currentView = ViewType.BANK
                    }
                }
                ViewType.FOUNDER -> FounderView(
                    onBack = { currentView = ViewType.MAIN },
                    onEnterProcess = { currentView = ViewType.STARTUP_SUBMISSION },
                    onViewAnalyses = { currentView = ViewType.FOUNDER_ANALYSIS_LIST }
                )
                ViewType.FOUNDER_ANALYSIS_LIST -> FounderAnalysisListView(
                    databaseService = databaseService,
                    onBack = { currentView = ViewType.FOUNDER },
                    onSelectAnalysis = { analysis ->
                        selectedSubmissionReview = analysis
                        currentView = ViewType.FOUNDER_ANALYSIS_DETAIL
                    }
                )
                ViewType.FOUNDER_ANALYSIS_DETAIL -> {
                    selectedSubmissionReview?.let { analysis ->
                        FounderAnalysisDetailView(
                            analysis = analysis,
                            aiService = aiService,
                            onBack = { currentView = ViewType.FOUNDER_ANALYSIS_LIST }
                        )
                    } ?: run {
                        // Fallback if no analysis selected
                        currentView = ViewType.FOUNDER_ANALYSIS_LIST
                    }
                }
                ViewType.STARTUP_SUBMISSION -> StartupSubmissionView(
                    onBack = { currentView = ViewType.FOUNDER },
                    onSubmit = { data ->
                        submissionData = data
                        currentView = ViewType.ANALYSIS
                    }
                )
                ViewType.ANALYSIS -> {
                    submissionData?.let { data ->
                        AnalysisView(
                            submissionData = data,
                            onBack = { currentView = ViewType.FOUNDER },
                            aiService = aiService,
                            databaseService = databaseService,
                            onSubmitToBMO = { result ->
                                // Handle the result if needed
                                // The database service already handles the submission
                            }
                        )
                    }
                }
            }
        }
    }
}