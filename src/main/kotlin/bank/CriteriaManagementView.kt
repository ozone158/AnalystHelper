package org.example.bank

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.model.CriteriaConfig
import org.example.model.Category
import org.example.model.Criterion
import org.example.model.IndustryCategories
import org.example.service.CriteriaLoader
import org.example.service.database.DatabaseService
import org.example.service.database.DatabaseResult

@Composable
fun CriteriaManagementView(
    databaseService: DatabaseService,
    onBack: () -> Unit
) {
    var selectedIndustry by remember { mutableStateOf<String?>(null) }
    var industryExpanded by remember { mutableStateOf(false) }
    var currentConfig by remember { mutableStateOf<CriteriaConfig?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    var showSaveError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Load config when industry is selected
    LaunchedEffect(selectedIndustry) {
        if (selectedIndustry != null) {
            isLoading = true
            // Try to load from database first, then fall back to resources
            currentConfig = databaseService.loadCriteriaConfigSync(selectedIndustry!!)
                ?: CriteriaLoader.loadFromResources(selectedIndustry)
            isLoading = false
        } else {
            currentConfig = null
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
                    text = "Criteria Management",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage evaluation criteria and weights",
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Select Industry",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Medium
            )
            Box {
                OutlinedButton(
                    onClick = { industryExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedIndustry ?: "Select industry to manage criteria"
                    )
                }
                DropdownMenu(
                    expanded = industryExpanded,
                    onDismissRequest = { industryExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IndustryCategories.filter { it != "More to come" }.forEach { category ->
                        DropdownMenuItem(onClick = {
                            selectedIndustry = category
                            industryExpanded = false
                            expandedCategories = emptySet()
                        }) {
                            Text(category)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Criteria configuration editor
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (selectedIndustry == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please select an industry to manage criteria",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        } else if (currentConfig != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Warning about weight validation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Weight Validation",
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "Category weights must sum to 1.0. Criteria weights within each category must also sum to 1.0.",
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Categories editor
                currentConfig!!.categories.forEachIndexed { categoryIndex, category ->
                    // Use key to reset state when config or category index changes
                    val categoryKey = "${selectedIndustry}_${categoryIndex}_${category.name}"
                    var categoryName by remember(categoryKey) { mutableStateOf(category.name) }
                    var categoryWeight by remember(categoryKey) { mutableStateOf(category.weight.toString()) }
                    var criteriaList by remember(categoryKey) { mutableStateOf(category.criteria.toMutableList()) }
                    // Derive isExpanded from expandedCategories to keep them in sync
                    val isExpanded = expandedCategories.contains(category.name)
                    
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
                                OutlinedTextField(
                                    value = categoryName,
                                    onValueChange = { categoryName = it },
                                    label = { Text("Category Name") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                OutlinedTextField(
                                    value = categoryWeight,
                                    onValueChange = { 
                                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                            categoryWeight = it
                                        }
                                    },
                                    label = { Text("Weight") },
                                    modifier = Modifier.width(120.dp)
                                )
                                IconButton(onClick = { 
                                    expandedCategories = if (isExpanded) {
                                        expandedCategories - category.name
                                    } else {
                                        expandedCategories + category.name
                                    }
                                }) {
                                    Text(if (isExpanded) "▼" else "▶")
                                }
                            }
                            
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Criteria list
                                Text(
                                    text = "Criteria",
                                    style = MaterialTheme.typography.subtitle2,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                criteriaList.forEachIndexed { criterionIndex, criterion ->
                                    // Use key to reset state when list changes
                                    val criterionKey = "${categoryKey}_${criterionIndex}_${criterion.name}"
                                    var criterionName by remember(criterionKey) { mutableStateOf(criterion.name) }
                                    var criterionWeight by remember(criterionKey) { mutableStateOf(criterion.weight.toString()) }
                                    var criterionDescription by remember(criterionKey) { mutableStateOf(criterion.description) }
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
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
                                                OutlinedTextField(
                                                    value = criterionName,
                                                    onValueChange = { criterionName = it },
                                                    label = { Text("Criterion Name") },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                OutlinedTextField(
                                                    value = criterionWeight,
                                                    onValueChange = { 
                                                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                                            criterionWeight = it
                                                        }
                                                    },
                                                    label = { Text("Weight") },
                                                    modifier = Modifier.width(120.dp)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        // Create new list without the deleted criterion
                                                        val newCriteriaList = criteriaList.toMutableList()
                                                        newCriteriaList.removeAt(criterionIndex)
                                                        criteriaList = newCriteriaList
                                                    }
                                                ) {
                                                    Text("✕", color = MaterialTheme.colors.error)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = criterionDescription,
                                                onValueChange = { criterionDescription = it },
                                                label = { Text("Description") },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 2,
                                                maxLines = 3
                                            )
                                            
                                            // Update criterion in list
                                            LaunchedEffect(criterionName, criterionWeight, criterionDescription) {
                                                if (criterionIndex < criteriaList.size) {
                                                    criteriaList[criterionIndex] = Criterion(
                                                        name = criterionName,
                                                        weight = criterionWeight.toDoubleOrNull() ?: 0.0,
                                                        description = criterionDescription
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Add new criterion button
                                Button(
                                    onClick = {
                                        val newCriteriaList = criteriaList.toMutableList()
                                        newCriteriaList.add(Criterion(
                                            name = "New Criterion",
                                            weight = 0.0,
                                            description = ""
                                        ))
                                        criteriaList = newCriteriaList
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("+ Add Criterion")
                                }
                                
                                // Show current weight sum
                                val criteriaWeightSum = criteriaList.sumOf { it.weight }
                                Text(
                                    text = "Criteria weights sum: ${String.format("%.2f", criteriaWeightSum)}",
                                    style = MaterialTheme.typography.caption,
                                    color = if (kotlin.math.abs(criteriaWeightSum - 1.0) < 0.01) {
                                        MaterialTheme.colors.primary
                                    } else {
                                        MaterialTheme.colors.error
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            // Update category in config
                            LaunchedEffect(categoryName, categoryWeight, criteriaList) {
                                if (currentConfig != null) {
                                    val updatedCategories = currentConfig!!.categories.toMutableList()
                                    updatedCategories[categoryIndex] = Category(
                                        name = categoryName,
                                        weight = categoryWeight.toDoubleOrNull() ?: 0.0,
                                        criteria = criteriaList
                                    )
                                    currentConfig = currentConfig!!.copy(categories = updatedCategories)
                                }
                            }
                        }
                    }
                }
                
                // Add new category button
                Button(
                    onClick = {
                        if (currentConfig != null) {
                            val newCategory = Category(
                                name = "New Category",
                                weight = 0.0,
                                criteria = emptyList()
                            )
                            currentConfig = currentConfig!!.copy(
                                categories = currentConfig!!.categories + newCategory
                            )
                            expandedCategories = expandedCategories + newCategory.name
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Add Category")
                }
                
                // Show total category weight sum
                val categoryWeightSum = currentConfig?.categories?.sumOf { it.weight } ?: 0.0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (kotlin.math.abs(categoryWeightSum - 1.0) < 0.01) {
                        MaterialTheme.colors.primaryVariant.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colors.error.copy(alpha = 0.1f)
                    },
                    elevation = 0.dp
                ) {
                    Text(
                        text = "Total category weights: ${String.format("%.2f", categoryWeightSum)}",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium,
                        color = if (kotlin.math.abs(categoryWeightSum - 1.0) < 0.01) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.error
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Save button
                Button(
                    onClick = {
                        if (currentConfig != null && selectedIndustry != null) {
                            val result = databaseService.saveCriteriaConfigSync(selectedIndustry!!, currentConfig!!)
                            when (result) {
                                is DatabaseResult.Success -> {
                                    showSaveSuccess = true
                                    showSaveError = false
                                }
                                is DatabaseResult.Error -> {
                                    showSaveError = true
                                    errorMessage = result.message
                                    showSaveSuccess = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = kotlin.math.abs(categoryWeightSum - 1.0) < 0.01
                ) {
                    Text("Save Criteria Configuration")
                }
                
                // Delete category button (if any)
                if (currentConfig != null && currentConfig!!.categories.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            if (currentConfig != null && currentConfig!!.categories.size > 1) {
                                val updatedCategories = currentConfig!!.categories.dropLast(1)
                                currentConfig = currentConfig!!.copy(categories = updatedCategories)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentConfig?.categories?.size ?: 0 > 1
                    ) {
                        Text("Remove Last Category", color = MaterialTheme.colors.error)
                    }
                }
            }
        }
    }
    
    // Save success dialog
    if (showSaveSuccess) {
        AlertDialog(
            onDismissRequest = { showSaveSuccess = false },
            title = { Text("Success") },
            text = { Text("Criteria configuration saved successfully for $selectedIndustry") },
            confirmButton = {
                Button(onClick = { showSaveSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Save error dialog
    if (showSaveError) {
        AlertDialog(
            onDismissRequest = { showSaveError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showSaveError = false }) {
                    Text("OK")
                }
            }
        )
    }
}
