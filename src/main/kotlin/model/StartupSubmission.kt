package org.example.model

import java.io.File

enum class StartupStage {
    IDEA,
    MVP,
    EARLY_REVENUE
}

// Available industry categories
val IndustryCategories = listOf("Tech", "Energy", "More to come")

data class FileEntry(
    val id: Int,
    var usage: String = "",
    var file: File? = null
)

data class StartupSubmissionData(
    val startupName: String,
    val industry: String,
    val problemStatement: String,
    val proposedSolution: String,
    val stage: StartupStage,
    val files: List<FileEntry>,
    val criteriaAnswers: Map<String, String> = emptyMap() // Map of question ID to answer
)
