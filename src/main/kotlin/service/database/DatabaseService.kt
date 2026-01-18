package org.example.service.database

import org.example.model.StartupSubmissionData
import org.example.model.SubmissionReview
import org.example.model.ReviewStatus
import org.example.model.ReviewNote
import org.example.model.AnalysisResult
import org.example.model.CriteriaConfig

/**
 * Interface for database service
 * Allows for different implementations (e.g., File-based, REST API, SQL, NoSQL)
 * 
 * Implementations should be placed in subfolders:
 * - database/filebased/ - File-based JSON storage
 * - database/sql/ - SQL database implementation (future)
 * - database/rest/ - REST API implementation (future)
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
    
    /**
     * Uploads an industry-specific file for AI analysis
     * @param industry The industry name (e.g., "Tech", "Energy")
     * @param file The file to upload
     * @param description Optional description of the file
     * @return Result indicating success or failure
     */
    suspend fun uploadIndustryFile(industry: String, file: java.io.File, description: String? = null): DatabaseResult
    
    /**
     * Synchronous version for uploading industry files
     */
    fun uploadIndustryFileSync(industry: String, file: java.io.File, description: String? = null): DatabaseResult
    
    /**
     * Fetches all files for a specific industry
     * @param industry The industry name
     * @return List of industry files
     */
    suspend fun getIndustryFiles(industry: String): List<org.example.model.IndustryFile>
    
    /**
     * Synchronous version for fetching industry files
     */
    fun getIndustryFilesSync(industry: String): List<org.example.model.IndustryFile>
    
    /**
     * Deletes an industry file
     * @param fileId The ID of the file to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteIndustryFile(fileId: String): DatabaseResult
    
    /**
     * Synchronous version for deleting industry files
     */
    fun deleteIndustryFileSync(fileId: String): DatabaseResult
    
    /**
     * Saves criteria configuration for a specific industry
     * @param industry The industry name (e.g., "Tech", "Energy")
     * @param config The criteria configuration to save
     * @return Result indicating success or failure
     */
    suspend fun saveCriteriaConfig(industry: String, config: CriteriaConfig): DatabaseResult
    
    /**
     * Synchronous version for saving criteria config
     */
    fun saveCriteriaConfigSync(industry: String, config: CriteriaConfig): DatabaseResult
    
    /**
     * Loads criteria configuration for a specific industry
     * @param industry The industry name
     * @return CriteriaConfig or null if not found
     */
    suspend fun loadCriteriaConfig(industry: String): CriteriaConfig?
    
    /**
     * Synchronous version for loading criteria config
     */
    fun loadCriteriaConfigSync(industry: String): CriteriaConfig?
    
    /**
     * Gets list of all industries that have custom criteria configurations
     * @return List of industry names
     */
    suspend fun getIndustriesWithCustomCriteria(): List<String>
    
    /**
     * Synchronous version
     */
    fun getIndustriesWithCustomCriteriaSync(): List<String>
}

/**
 * Result of database operation
 */
sealed class DatabaseResult {
    data class Success(val message: String = "Operation successful") : DatabaseResult()
    data class Error(val message: String, val exception: Throwable? = null) : DatabaseResult()
}
