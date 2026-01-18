package org.example.service.ai

import org.example.model.StartupSubmissionData
import org.example.model.AnalysisResult
import org.example.model.ExtractedStartupInfo

/**
 * Interface for AI analysis service
 * Allows for different implementations (e.g., OpenAI, Anthropic, local model)
 * 
 * Implementations should be placed in subfolders:
 * - ai/provider/openai/ - OpenAI GPT models
 * - ai/provider/anthropic/ - Anthropic Claude (future)
 * - ai/provider/default/ - Default placeholder implementation
 */
interface AIService {
    /**
     * Performs AI analysis on the startup submission data
     * @param data The startup submission data to analyze
     * @param industryFiles Optional list of industry-specific files to consider
     * @return The structured analysis result
     */
    suspend fun analyzeSubmission(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile> = emptyList()): AnalysisResult
    
    /**
     * Synchronous version for compatibility
     * @param data The startup submission data to analyze
     * @param industryFiles Optional list of industry-specific files to consider
     * @return The structured analysis result
     */
    fun analyzeSubmissionSync(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile> = emptyList()): AnalysisResult
    
    /**
     * Formats the analysis result as a readable string
     * @param result The analysis result to format
     * @return Formatted string representation
     */
    fun formatAnalysisResult(result: AnalysisResult): String
    
    /**
     * Detects the industry from a business plan document
     * @param documentText The extracted text from the business plan document
     * @return The detected industry name (e.g., "Tech", "Energy"), or null if unable to detect
     */
    suspend fun detectIndustry(documentText: String): String?
    
    /**
     * Synchronous version for compatibility
     * @param documentText The extracted text from the business plan document
     * @return The detected industry name (e.g., "Tech", "Energy"), or null if unable to detect
     */
    fun detectIndustrySync(documentText: String): String?
    
    /**
     * Extracts startup information (name, problem statement, proposed solution) from a business plan document
     * @param documentText The extracted text from the business plan document
     * @return ExtractedStartupInfo with extracted information, or null fields if unable to extract
     */
    suspend fun extractStartupInfo(documentText: String): ExtractedStartupInfo
    
    /**
     * Synchronous version for compatibility
     * @param documentText The extracted text from the business plan document
     * @return ExtractedStartupInfo with extracted information, or null fields if unable to extract
     */
    fun extractStartupInfoSync(documentText: String): ExtractedStartupInfo
}
