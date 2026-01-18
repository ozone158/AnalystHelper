package org.example.service.database.filebased

import org.example.service.database.DatabaseService
import org.example.service.database.DatabaseResult
import org.example.model.StartupSubmissionData
import org.example.model.SubmissionReview
import org.example.model.ReviewStatus
import org.example.model.ReviewNote
import org.example.model.AnalysisResult
import org.example.model.CriteriaConfig
import java.util.UUID

/**
 * File-based implementation of DatabaseService
 * Uses local file storage to persist data across application restarts
 * Stores data in JSON files in user's home directory under .bmo-analyst-helper/
 * 
 * This is a "fake" database implementation for development/testing purposes.
 * For production, implement a real database (SQL, NoSQL, REST API) in a separate package:
 * - database/sql/ for SQL database implementation
 * - database/rest/ for REST API implementation
 * - etc.
 */
class FileBasedDatabaseService : DatabaseService {
    // In-memory storage that is synchronized with local files
    private val submissions = mutableListOf<SubmissionReview>()
    private val industryFiles = mutableListOf<org.example.model.IndustryFile>()
    private val criteriaConfigs = mutableMapOf<String, CriteriaConfig>()
    
    init {
        // Load data from local files on initialization
        loadDataFromFiles()
    }
    
    /**
     * Loads all data from local files on startup
     */
    private fun loadDataFromFiles() {
        try {
            // Load submissions
            submissions.clear()
            submissions.addAll(LocalFileStorage.loadSubmissions())
            
            // Load industry files
            industryFiles.clear()
            industryFiles.addAll(LocalFileStorage.loadIndustryFilesMetadata())
            
            // Load criteria configs
            val industriesWithCriteria = LocalFileStorage.getIndustriesWithCustomCriteria()
            criteriaConfigs.clear()
            industriesWithCriteria.forEach { industry ->
                val config = LocalFileStorage.loadCriteriaConfig(industry)
                if (config != null) {
                    criteriaConfigs[industry.lowercase()] = config
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Continue with empty data if loading fails
        }
    }
    
    override suspend fun submitToDatabase(data: StartupSubmissionData): DatabaseResult {
        return submitToDatabaseSync(data)
    }
    
    override fun submitToDatabaseSync(data: StartupSubmissionData): DatabaseResult {
        // TODO: In a real implementation, this would submit to an actual database
        try {
            println("Submitting to BMO database: ${data.startupName}")
            // For now, we'll create a placeholder review
            // In real implementation, this would be created after AI analysis
            return DatabaseResult.Success("Submission sent to BMO successfully")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to submit to database: ${e.message}", e)
        }
    }
    
    override suspend fun saveAnalysis(data: StartupSubmissionData, analysisResult: AnalysisResult): DatabaseResult {
        return saveAnalysisSync(data, analysisResult)
    }
    
    override fun saveAnalysisSync(data: StartupSubmissionData, analysisResult: AnalysisResult): DatabaseResult {
        try {
            // Check if submission already exists (by startup name as identifier)
            val existingIndex = submissions.indexOfFirst { 
                it.submissionData.startupName == data.startupName 
            }
            
            val now = java.util.Date()
            val submissionReview = SubmissionReview(
                id = if (existingIndex >= 0) submissions[existingIndex].id else UUID.randomUUID().toString(),
                submissionData = data,
                analysisResult = analysisResult,
                status = if (existingIndex >= 0) submissions[existingIndex].status else ReviewStatus.PENDING,
                notes = if (existingIndex >= 0) submissions[existingIndex].notes else emptyList(),
                createdAt = if (existingIndex >= 0) submissions[existingIndex].createdAt else now,
                updatedAt = now
            )
            
            if (existingIndex >= 0) {
                // Update existing submission
                submissions[existingIndex] = submissionReview
            } else {
                // Add new submission
                submissions.add(submissionReview)
            }
            
            // Persist to file
            LocalFileStorage.saveSubmissions(submissions)
            
            return DatabaseResult.Success("Analysis saved successfully")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to save analysis: ${e.message}", e)
        }
    }
    
    override suspend fun fetchSubmissions(): List<SubmissionReview> {
        return fetchSubmissionsSync()
    }
    
    override fun fetchSubmissionsSync(): List<SubmissionReview> {
        return submissions.toList()
    }
    
    override suspend fun updateSubmissionStatus(submissionId: String, status: ReviewStatus): DatabaseResult {
        return updateSubmissionStatusSync(submissionId, status)
    }
    
    override fun updateSubmissionStatusSync(submissionId: String, status: ReviewStatus): DatabaseResult {
        try {
            val submission = submissions.find { it.id == submissionId }
            if (submission != null) {
                val index = submissions.indexOf(submission)
                submissions[index] = submission.copy(
                    status = status,
                    updatedAt = java.util.Date()
                )
                // Persist to file
                LocalFileStorage.saveSubmissions(submissions)
                return DatabaseResult.Success("Status updated successfully")
            }
            return DatabaseResult.Error("Submission not found")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to update status: ${e.message}", e)
        }
    }
    
    override suspend fun addNoteToSubmission(submissionId: String, note: ReviewNote): DatabaseResult {
        return addNoteToSubmissionSync(submissionId, note)
    }
    
    override fun addNoteToSubmissionSync(submissionId: String, note: ReviewNote): DatabaseResult {
        try {
            val submission = submissions.find { it.id == submissionId }
            if (submission != null) {
                val index = submissions.indexOf(submission)
                submissions[index] = submission.copy(
                    notes = submission.notes + note,
                    updatedAt = java.util.Date()
                )
                // Persist to file
                LocalFileStorage.saveSubmissions(submissions)
                return DatabaseResult.Success("Note added successfully")
            }
            return DatabaseResult.Error("Submission not found")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to add note: ${e.message}", e)
        }
    }
    
    /**
     * Helper method for testing/development - adds a submission to the list
     * This would not exist in production - submissions come from actual database
     */
    fun addSubmissionForTesting(review: SubmissionReview) {
        submissions.add(review)
        LocalFileStorage.saveSubmissions(submissions)
    }
    
    override suspend fun uploadIndustryFile(industry: String, file: java.io.File, description: String?): DatabaseResult {
        return uploadIndustryFileSync(industry, file, description)
    }
    
    override fun uploadIndustryFileSync(industry: String, file: java.io.File, description: String?): DatabaseResult {
        try {
            if (!file.exists()) {
                return DatabaseResult.Error("File does not exist")
            }
            
            // Validate file type
            val extension = file.extension.lowercase()
            if (extension != "csv" && extension != "txt") {
                return DatabaseResult.Error("Only CSV and TXT files are allowed")
            }
            
            val fileId = UUID.randomUUID().toString()
            
            // Copy file to storage directory
            val storedFile = LocalFileStorage.storeIndustryFile(file, fileId, file.name)
            
            val industryFile = org.example.model.IndustryFile(
                id = fileId,
                industry = industry,
                fileName = file.name,
                file = storedFile,
                uploadedAt = java.util.Date(),
                description = description
            )
            
            industryFiles.add(industryFile)
            
            // Persist metadata to file
            LocalFileStorage.saveIndustryFilesMetadata(industryFiles)
            
            return DatabaseResult.Success("File uploaded successfully for $industry industry")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to upload file: ${e.message}", e)
        }
    }
    
    override suspend fun getIndustryFiles(industry: String): List<org.example.model.IndustryFile> {
        return getIndustryFilesSync(industry)
    }
    
    override fun getIndustryFilesSync(industry: String): List<org.example.model.IndustryFile> {
        return industryFiles.filter { it.industry.equals(industry, ignoreCase = true) }
    }
    
    override suspend fun deleteIndustryFile(fileId: String): DatabaseResult {
        return deleteIndustryFileSync(fileId)
    }
    
    override fun deleteIndustryFileSync(fileId: String): DatabaseResult {
        try {
            val file = industryFiles.find { it.id == fileId }
            if (file != null) {
                // Delete the actual file
                try {
                    file.file.delete()
                } catch (e: Exception) {
                    // Log but continue if file deletion fails
                    e.printStackTrace()
                }
                industryFiles.remove(file)
                
                // Persist metadata to file
                LocalFileStorage.saveIndustryFilesMetadata(industryFiles)
                
                return DatabaseResult.Success("File deleted successfully")
            }
            return DatabaseResult.Error("File not found")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to delete file: ${e.message}", e)
        }
    }
    
    override suspend fun saveCriteriaConfig(industry: String, config: CriteriaConfig): DatabaseResult {
        return saveCriteriaConfigSync(industry, config)
    }
    
    override fun saveCriteriaConfigSync(industry: String, config: CriteriaConfig): DatabaseResult {
        try {
            // Validate weights sum to 1.0 for categories
            val categoryWeightSum = config.categories.sumOf { it.weight }
            if (kotlin.math.abs(categoryWeightSum - 1.0) > 0.01) {
                return DatabaseResult.Error("Category weights must sum to 1.0 (currently: $categoryWeightSum)")
            }
            
            // Validate weights for each category's criteria
            config.categories.forEach { category ->
                val criteriaWeightSum = category.criteria.sumOf { it.weight }
                if (kotlin.math.abs(criteriaWeightSum - 1.0) > 0.01) {
                    return DatabaseResult.Error("Criteria weights in category '${category.name}' must sum to 1.0 (currently: $criteriaWeightSum)")
                }
            }
            
            criteriaConfigs[industry.lowercase()] = config
            
            // Persist to file
            LocalFileStorage.saveCriteriaConfig(industry, config)
            
            return DatabaseResult.Success("Criteria configuration saved successfully for $industry")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to save criteria config: ${e.message}", e)
        }
    }
    
    override suspend fun loadCriteriaConfig(industry: String): CriteriaConfig? {
        return loadCriteriaConfigSync(industry)
    }
    
    override fun loadCriteriaConfigSync(industry: String): CriteriaConfig? {
        // First check in-memory cache
        val cached = criteriaConfigs[industry.lowercase()]
        if (cached != null) {
            return cached
        }
        // If not in cache, try loading from file (might have been modified externally)
        val fileConfig = LocalFileStorage.loadCriteriaConfig(industry)
        if (fileConfig != null) {
            criteriaConfigs[industry.lowercase()] = fileConfig
        }
        return fileConfig
    }
    
    override suspend fun getIndustriesWithCustomCriteria(): List<String> {
        return getIndustriesWithCustomCriteriaSync()
    }
    
    override fun getIndustriesWithCustomCriteriaSync(): List<String> {
        // Merge in-memory and file-based configs
        val fileIndustries = LocalFileStorage.getIndustriesWithCustomCriteria().toSet()
        val memoryIndustries = criteriaConfigs.keys.map { it.replaceFirstChar { char -> char.uppercase() } }.toSet()
        return (fileIndustries + memoryIndustries).sorted()
    }
}
