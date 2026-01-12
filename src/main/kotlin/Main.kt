package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.service.AIService
import org.example.service.DatabaseService
import org.example.service.DefaultAIService
import org.example.service.DefaultDatabaseService

enum class ViewType {
    MAIN,
    BANK,
    FOUNDER,
    STARTUP_SUBMISSION,
    ANALYSIS,
    SUBMISSION_DETAIL
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
        Text(
            text = "Analyst Helper",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateToBank,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
        ) {
            Text("Bank View", style = MaterialTheme.typography.body1)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNavigateToFounder,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
        ) {
            Text("Founder View", style = MaterialTheme.typography.body1)
        }
    }
}

fun main() = application {
    var currentView by remember { mutableStateOf(ViewType.MAIN) }
    var submissionData by remember { mutableStateOf<StartupSubmissionData?>(null) }
    var selectedSubmissionReview by remember { mutableStateOf<org.example.model.SubmissionReview?>(null) }
    
    // Initialize services (can be swapped with different implementations)
    val aiService: AIService = remember { DefaultAIService() }
    val databaseService: DatabaseService = remember { DefaultDatabaseService() }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Analyst Helper",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        MaterialTheme {
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
                    onEnterProcess = { currentView = ViewType.STARTUP_SUBMISSION }
                )
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