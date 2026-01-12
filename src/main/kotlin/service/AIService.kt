package org.example.service

import org.example.StartupSubmissionData
import org.example.model.AnalysisResult

/**
 * Interface for AI analysis service
 * Allows for different implementations (e.g., OpenAI, Anthropic, local model)
 */
interface AIService {
    /**
     * Performs AI analysis on the startup submission data
     * @param data The startup submission data to analyze
     * @return The structured analysis result
     */
    suspend fun analyzeSubmission(data: StartupSubmissionData): AnalysisResult
    
    /**
     * Synchronous version for compatibility
     * @param data The startup submission data to analyze
     * @return The structured analysis result
     */
    fun analyzeSubmissionSync(data: StartupSubmissionData): AnalysisResult
    
    /**
     * Formats the analysis result as a readable string
     * @param result The analysis result to format
     * @return Formatted string representation
     */
    fun formatAnalysisResult(result: AnalysisResult): String
}

/**
 * Default implementation of AIService
 * Uses criteria configuration and prompt template
 * Ready for actual AI integration
 */
class DefaultAIService : AIService {
    private val criteriaConfig = CriteriaLoader.loadFromResources() ?: CriteriaLoader.getDefaultConfig()
    private val promptTemplate = PromptTemplateLoader.loadFromResources() ?: PromptTemplateLoader.getDefaultTemplate()
    
    override suspend fun analyzeSubmission(data: StartupSubmissionData): AnalysisResult {
        // TODO: Implement actual AI analysis with async API calls
        // This should use the prompt template and criteria config
        return analyzeSubmissionSync(data)
    }
    
    override fun analyzeSubmissionSync(data: StartupSubmissionData): AnalysisResult {
        // TODO: Implement actual AI analysis
        // For now, return a structured placeholder result
        // When implementing, this should:
        // 1. Build prompt from template with submission data and criteria
        // 2. Call AI API (OpenAI, Anthropic, etc.)
        // 3. Parse JSON response into AnalysisResult
        // 4. Validate against criteria configuration
        
        return createPlaceholderResult(data)
    }
    
    override fun formatAnalysisResult(result: AnalysisResult): String {
        val sb = StringBuilder()
        sb.appendLine("=== AI Analysis Results ===")
        sb.appendLine()
        sb.appendLine("Overall Score: ${result.overallScore}/5.0")
        sb.appendLine("Recommendation: ${result.recommendation}")
        sb.appendLine()
        sb.appendLine("Recommendation Reasoning:")
        sb.appendLine(result.recommendationReasoning)
        sb.appendLine()
        
        sb.appendLine("=== Category Scores ===")
        result.categoryScores.forEach { category ->
            sb.appendLine("\n${category.categoryName} (Weight: ${category.categoryWeight}, Score: ${category.categoryScore}/5.0)")
            sb.appendLine(category.categoryReasoning)
            category.criteriaScores.forEach { criterion ->
                sb.appendLine("  - ${criterion.criterionName}: ${criterion.score}/5 (${criterion.reasoning})")
            }
        }
        
        sb.appendLine("\n=== Risk Assessment ===")
        sb.appendLine("Privacy/Security: ${result.riskAssessment.privacySecurity.level} - ${result.riskAssessment.privacySecurity.description}")
        sb.appendLine("Compliance: ${result.riskAssessment.compliance.level} - ${result.riskAssessment.compliance.description}")
        sb.appendLine("Market: ${result.riskAssessment.market.level} - ${result.riskAssessment.market.description}")
        sb.appendLine("Technical: ${result.riskAssessment.technical.level} - ${result.riskAssessment.technical.description}")
        
        sb.appendLine("\n=== Key Strengths ===")
        result.keyStrengths.forEach { sb.appendLine("- $it") }
        
        sb.appendLine("\n=== Key Concerns ===")
        result.keyConcerns.forEach { sb.appendLine("- $it") }
        
        return sb.toString()
    }
    
    /**
     * Creates a placeholder structured result
     * This will be replaced with actual AI analysis
     */
    private fun createPlaceholderResult(data: StartupSubmissionData): AnalysisResult {
        // This is a placeholder - actual implementation should parse AI JSON response
        return AnalysisResult(
            overallScore = 3.5,
            categoryScores = emptyList(), // Will be populated from AI response
            riskAssessment = org.example.model.RiskAssessment(
                privacySecurity = org.example.model.RiskDimension(
                    level = org.example.model.RiskLevel.MEDIUM,
                    description = "Standard privacy considerations apply",
                    concerns = emptyList()
                ),
                compliance = org.example.model.RiskDimension(
                    level = org.example.model.RiskLevel.LOW,
                    description = "No major compliance issues identified",
                    concerns = emptyList()
                ),
                market = org.example.model.RiskDimension(
                    level = org.example.model.RiskLevel.MEDIUM,
                    description = "Moderate market risks",
                    concerns = emptyList()
                ),
                technical = org.example.model.RiskDimension(
                    level = org.example.model.RiskLevel.LOW,
                    description = "Technically feasible",
                    concerns = emptyList()
                )
            ),
            recommendation = org.example.model.Recommendation.PARTIAL,
            recommendationReasoning = "This is a placeholder analysis. Actual AI analysis will follow the criteria configuration and prompt template.",
            keyStrengths = listOf("Problem statement is clear", "Solution addresses identified problem"),
            keyConcerns = listOf("Limited market data provided", "Early stage of development"),
            dataQuality = org.example.model.DataQuality(
                completeness = org.example.model.Completeness.PARTIAL,
                gaps = listOf("Financial projections", "Detailed market analysis"),
                impactOnAnalysis = "Analysis is based on available information"
            )
        )
    }
}
