package org.example.service

import org.example.StartupSubmissionData
import org.example.model.SubmissionReview
import org.example.model.ReviewStatus
import org.example.model.ReviewNote
import org.example.model.AnalysisResult
import java.util.UUID

/**
 * Interface for database service
 * Allows for different implementations (e.g., REST API, SQL, NoSQL)
 */
interface DatabaseService {
    /**
     * Submits startup data to the remote database
     * @param data The startup submission data to store
     * @return Result indicating success or failure
     */
    suspend fun submitToDatabase(data: StartupSubmissionData): DatabaseResult
    
    /**
     * Synchronous version for compatibility
     * @param data The startup submission data to store
     * @return Result indicating success or failure
     */
    fun submitToDatabaseSync(data: StartupSubmissionData): DatabaseResult
    
    /**
     * Saves analysis result with submission data to local storage
     * @param data The startup submission data
     * @param analysisResult The analysis result
     * @return Result indicating success or failure
     */
    suspend fun saveAnalysis(data: StartupSubmissionData, analysisResult: AnalysisResult): DatabaseResult
    
    /**
     * Synchronous version for saving analysis
     */
    fun saveAnalysisSync(data: StartupSubmissionData, analysisResult: AnalysisResult): DatabaseResult
    
    /**
     * Fetches all submission reviews from the database
     * @return List of submission reviews
     */
    suspend fun fetchSubmissions(): List<SubmissionReview>
    
    /**
     * Synchronous version for fetching submissions
     */
    fun fetchSubmissionsSync(): List<SubmissionReview>
    
    /**
     * Updates the status of a submission review
     * @param submissionId The ID of the submission
     * @param status The new status
     * @return Result indicating success or failure
     */
    suspend fun updateSubmissionStatus(submissionId: String, status: ReviewStatus): DatabaseResult
    
    /**
     * Synchronous version for updating status
     */
    fun updateSubmissionStatusSync(submissionId: String, status: ReviewStatus): DatabaseResult
    
    /**
     * Adds a note to a submission review
     * @param submissionId The ID of the submission
     * @param note The note to add
     * @return Result indicating success or failure
     */
    suspend fun addNoteToSubmission(submissionId: String, note: ReviewNote): DatabaseResult
    
    /**
     * Synchronous version for adding notes
     */
    fun addNoteToSubmissionSync(submissionId: String, note: ReviewNote): DatabaseResult
}

/**
 * Result of database submission operation
 */
sealed class DatabaseResult {
    data class Success(val message: String = "Submission successful") : DatabaseResult()
    data class Error(val message: String, val exception: Throwable? = null) : DatabaseResult()
}

/**
 * Default implementation of DatabaseService
 * Currently uses placeholder logic with in-memory storage for development
 * Ready for actual database integration
 */
class DefaultDatabaseService : DatabaseService {
    // In-memory storage for development (will be replaced with actual database)
    private val submissions = mutableListOf<org.example.model.SubmissionReview>()
    
    override suspend fun submitToDatabase(data: StartupSubmissionData): DatabaseResult {
        return submitToDatabaseSync(data)
    }
    
    override fun submitToDatabaseSync(data: StartupSubmissionData): DatabaseResult {
        // TODO: Implement actual database submission
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
            val submissionReview = org.example.model.SubmissionReview(
                id = if (existingIndex >= 0) submissions[existingIndex].id else java.util.UUID.randomUUID().toString(),
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
            
            return DatabaseResult.Success("Analysis saved successfully")
        } catch (e: Exception) {
            return DatabaseResult.Error("Failed to save analysis: ${e.message}", e)
        }
    }
    
    override suspend fun fetchSubmissions(): List<SubmissionReview> {
        return fetchSubmissionsSync()
    }
    
    override fun fetchSubmissionsSync(): List<SubmissionReview> {
        // TODO: Implement actual database query
        // Placeholder: return sample data for development
        return submissions.toList()
    }
    
    override suspend fun updateSubmissionStatus(submissionId: String, status: ReviewStatus): DatabaseResult {
        return updateSubmissionStatusSync(submissionId, status)
    }
    
    override fun updateSubmissionStatusSync(submissionId: String, status: ReviewStatus): DatabaseResult {
        // TODO: Implement actual database update
        try {
            val submission = submissions.find { it.id == submissionId }
            if (submission != null) {
                // In real implementation, update in database
                // For now, we'll update the in-memory list
                val index = submissions.indexOf(submission)
                submissions[index] = submission.copy(
                    status = status,
                    updatedAt = java.util.Date()
                )
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
        // TODO: Implement actual database update
        try {
            val submission = submissions.find { it.id == submissionId }
            if (submission != null) {
                val index = submissions.indexOf(submission)
                submissions[index] = submission.copy(
                    notes = submission.notes + note,
                    updatedAt = java.util.Date()
                )
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
    }
}
