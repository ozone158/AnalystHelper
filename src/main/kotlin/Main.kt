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
import org.example.service.ai.AIService
import org.example.service.database.DatabaseService
import org.example.service.database.filebased.FileBasedDatabaseService
import org.example.service.ai.AIServiceFactory
import org.example.theme.BMTheme
import org.example.bank.SubmissionReviewListView
import org.example.bank.SubmissionDetailView
import org.example.bank.IndustryFileManagementView
import org.example.bank.CriteriaManagementView
import org.example.founder.FounderView
import org.example.founder.FounderAnalysisListView
import org.example.founder.FounderAnalysisDetailView
import org.example.founder.StartupSubmissionView
import org.example.founder.BusinessPlanUploadView
import org.example.model.StartupSubmissionData
import java.io.File

enum class ViewType {
    MAIN,
    BANK,
    FOUNDER,
    BUSINESS_PLAN_UPLOAD,
    STARTUP_SUBMISSION,
    ANALYSIS,
    SUBMISSION_DETAIL,
    FOUNDER_ANALYSIS_LIST,
    FOUNDER_ANALYSIS_DETAIL,
    INDUSTRY_FILE_MANAGEMENT,
    CRITERIA_MANAGEMENT
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
    var detectedIndustry by remember { mutableStateOf<String?>(null) }
    var businessPlanFile by remember { mutableStateOf<File?>(null) }
    var extractedStartupInfo by remember { mutableStateOf<org.example.model.ExtractedStartupInfo?>(null) }
    
    // Initialize services from configuration
    // Edit AIConfig.kt to configure AI provider and API key
    val aiService: AIService = remember { 
        AIServiceFactory.createFromConfig()
    }
    val databaseService: DatabaseService = remember { FileBasedDatabaseService() }
    
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
                    },
                    onManageIndustryFiles = { currentView = ViewType.INDUSTRY_FILE_MANAGEMENT },
                    onManageCriteria = { currentView = ViewType.CRITERIA_MANAGEMENT }
                )
                ViewType.INDUSTRY_FILE_MANAGEMENT -> IndustryFileManagementView(
                    databaseService = databaseService,
                    onBack = { currentView = ViewType.BANK }
                )
                ViewType.CRITERIA_MANAGEMENT -> CriteriaManagementView(
                    databaseService = databaseService,
                    onBack = { currentView = ViewType.BANK }
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
                    onEnterProcess = { currentView = ViewType.BUSINESS_PLAN_UPLOAD },
                    onViewAnalyses = { currentView = ViewType.FOUNDER_ANALYSIS_LIST }
                )
                ViewType.BUSINESS_PLAN_UPLOAD -> BusinessPlanUploadView(
                    onBack = { currentView = ViewType.FOUNDER },
                    aiService = aiService,
                    onIndustryDetected = { industry, file, startupInfo ->
                        detectedIndustry = industry
                        businessPlanFile = file
                        extractedStartupInfo = startupInfo
                        currentView = ViewType.STARTUP_SUBMISSION
                    }
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
                    onBack = { 
                        detectedIndustry = null
                        businessPlanFile = null
                        extractedStartupInfo = null
                        currentView = ViewType.BUSINESS_PLAN_UPLOAD
                    },
                    preSelectedIndustry = detectedIndustry,
                    businessPlanFile = businessPlanFile,
                    extractedInfo = extractedStartupInfo,
                    onSubmit = { data ->
                        submissionData = data
                        detectedIndustry = null
                        businessPlanFile = null
                        extractedStartupInfo = null
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