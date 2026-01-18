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
import org.example.service.CriteriaLoader
import org.example.service.QuestionGenerator
import org.example.model.StartupSubmissionData
import org.example.model.StartupStage
import org.example.model.IndustryCategories
import org.example.model.FileEntry
import java.io.File

@Composable
fun StartupSubmissionView(
    onBack: () -> Unit, 
    onSubmit: (StartupSubmissionData) -> Unit = {},
    preSelectedIndustry: String? = null,
    businessPlanFile: File? = null,
    extractedInfo: org.example.model.ExtractedStartupInfo? = null
) {
    var startupName by remember { mutableStateOf(extractedInfo?.startupName ?: "") }
    var selectedIndustry by remember { mutableStateOf<String?>(preSelectedIndustry) }
    var industryExpanded by remember { mutableStateOf(false) }
    var problemStatement by remember { mutableStateOf(extractedInfo?.problemStatement ?: "") }
    var proposedSolution by remember { mutableStateOf(extractedInfo?.proposedSolution ?: "") }
    var selectedStage by remember { mutableStateOf<StartupStage?>(null) }
    var stageExpanded by remember { mutableStateOf(false) }
    var fileEntries by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var nextFileId by remember { mutableStateOf(0) }
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Criteria-based questions
    var criteriaQuestions by remember { mutableStateOf<List<org.example.service.Question>>(emptyList()) }
    var criteriaAnswers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    val scrollState = rememberScrollState()
    
    // Add business plan file to file entries if provided
    LaunchedEffect(businessPlanFile) {
        if (businessPlanFile != null && fileEntries.none { it.file == businessPlanFile }) {
            fileEntries = fileEntries + FileEntry(
                id = nextFileId,
                usage = "Business Plan",
                file = businessPlanFile
            )
            nextFileId++
        }
    }
    
    // Load criteria and generate questions when industry is selected
    LaunchedEffect(selectedIndustry) {
        if (selectedIndustry != null && selectedIndustry != "More to come") {
            val criteriaConfig = CriteriaLoader.loadFromResources(selectedIndustry)
            criteriaQuestions = QuestionGenerator.generateQuestionsFromCriteria(criteriaConfig)
            // Initialize empty answers for all questions
            criteriaAnswers = criteriaQuestions.associate { it.id to "" }
        } else {
            criteriaQuestions = emptyList()
            criteriaAnswers = emptyMap()
        }
    }
    
    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()
        
        if (startupName.isBlank()) {
            errors.add("Startup name")
        }
        if (selectedIndustry == null) {
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
        
        // Validate criteria questions (optional but recommended)
        val unansweredRequired = criteriaQuestions.filter { 
            criteriaAnswers[it.id].isNullOrBlank() 
        }
        if (unansweredRequired.isNotEmpty()) {
            // Warn but don't block - questions are recommended but not strictly required
            // You can make them required by uncommenting the error addition below
            // errors.add("Please answer all evaluation questions")
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
                industry = selectedIndustry ?: "",
                problemStatement = problemStatement,
                proposedSolution = proposedSolution,
                stage = selectedStage!!,
                files = fileEntries.filter { it.file != null },
                criteriaAnswers = criteriaAnswers
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
            Column {
                Column {
                    Text(
                        text = "Startup Submission",
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (preSelectedIndustry != null) {
                            "Step 2: Complete evaluation questions"
                        } else {
                            "BMO Evaluation Form"
                        },
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
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
            // Show auto-filled indicator if extracted info was provided
            if (extractedInfo != null && (extractedInfo.startupName != null || extractedInfo.problemStatement != null || extractedInfo.proposedSolution != null)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 1.dp,
                    backgroundColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Some fields have been auto-filled from your business plan",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
            
            // Startup name
            Column(modifier = Modifier.fillMaxWidth()) {
                if (extractedInfo?.startupName != null && startupName.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Startup name",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Auto-filled",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = startupName,
                    onValueChange = { startupName = it },
                    label = { Text("Startup name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // Industry dropdown or display
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Industry",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (preSelectedIndustry != null) {
                        Card(
                            backgroundColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.1f),
                            elevation = 0.dp
                        ) {
                            Text(
                                text = "✓ Auto-detected",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                if (preSelectedIndustry != null) {
                    // Show detected industry as read-only
                    OutlinedTextField(
                        value = selectedIndustry ?: "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colors.onSurface,
                            disabledBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "Industry detected from your business plan",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    // Allow manual selection
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
                            IndustryCategories.forEach { category ->
                                val isSelectable = category != "More to come"
                                DropdownMenuItem(
                                    onClick = {
                                        if (isSelectable) {
                                            selectedIndustry = category
                                            industryExpanded = false
                                        }
                                    },
                                    enabled = isSelectable
                                ) {
                                    Text(
                                        text = category,
                                        style = if (isSelectable) {
                                            MaterialTheme.typography.body1
                                        } else {
                                            MaterialTheme.typography.body2.copy(
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Problem statement
            Column(modifier = Modifier.fillMaxWidth()) {
                if (extractedInfo?.problemStatement != null && problemStatement.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Problem statement",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Auto-filled",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = problemStatement,
                    onValueChange = { problemStatement = it },
                    label = { Text("Problem statement") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
            
            // Proposed solution
            Column(modifier = Modifier.fillMaxWidth()) {
                if (extractedInfo?.proposedSolution != null && proposedSolution.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Proposed solution",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Auto-filled",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = proposedSolution,
                    onValueChange = { proposedSolution = it },
                    label = { Text("Proposed solution") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
            
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
            
            // Criteria-based questions section
            if (criteriaQuestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Evaluation Questions",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${criteriaQuestions.size} questions",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Please answer these questions to help us evaluate your startup. These questions are based on our evaluation criteria and will help provide a more accurate analysis.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Group questions by category
                val questionsByCategory = criteriaQuestions.groupBy { it.category }
                
                questionsByCategory.forEach { (category, questions) ->
                    var isCategoryExpanded by remember { mutableStateOf(true) }
                    
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
                                    text = category,
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { isCategoryExpanded = !isCategoryExpanded }) {
                                    Text(if (isCategoryExpanded) "▼" else "▶")
                                }
                            }
                            
                            if (isCategoryExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                questions.forEach { question ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = question.questionText,
                                            style = MaterialTheme.typography.body1,
                                            fontWeight = FontWeight.Medium
                                        )
                                        
                                        Text(
                                            text = "Criterion: ${question.criterion}",
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                        
                                        OutlinedTextField(
                                            value = criteriaAnswers[question.id] ?: "",
                                            onValueChange = { answer ->
                                                criteriaAnswers = criteriaAnswers + (question.id to answer)
                                            },
                                            label = { Text("Your answer") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 3,
                                            maxLines = 8
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
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
