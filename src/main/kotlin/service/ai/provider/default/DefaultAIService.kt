package org.example.service.ai.provider.default

import org.example.service.ai.AIService
import org.example.model.StartupSubmissionData
import org.example.model.AnalysisResult
import org.example.model.ExtractedStartupInfo
import org.example.service.PromptTemplateLoader

/**
 * Default implementation of AIService
 * Uses criteria configuration and prompt template
 * Ready for actual AI integration
 */
class DefaultAIService : AIService {
    private val promptTemplate = PromptTemplateLoader.loadFromResources() ?: PromptTemplateLoader.getDefaultTemplate()
    
    override suspend fun analyzeSubmission(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile>): AnalysisResult {
        // TODO: Implement actual AI analysis with async API calls
        // This should use the prompt template and criteria config
        return analyzeSubmissionSync(data, industryFiles)
    }
    
    override fun analyzeSubmissionSync(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile>): AnalysisResult {
        // TODO: Implement actual AI analysis
        // For now, return a structured placeholder result
        // When implementing, this should:
        // 1. Load industry-specific criteria
        // 2. Build prompt from template with submission data, criteria, and criteria answers
        // 3. Call AI API (OpenAI, Anthropic, etc.)
        // 4. Parse JSON response into AnalysisResult
        // 5. Validate against criteria configuration
        // Note: criteriaAnswers in data.criteriaAnswers should be included in the prompt
        
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
        
        if (result.qualitativeForecast != null) {
            sb.appendLine("\n=== Qualitative Forecast ===")
            sb.appendLine("\nShort-term Outlook (6-12 months):")
            sb.appendLine(result.qualitativeForecast.shortTermOutlook)
            sb.appendLine("\nMedium-term Prospects (1-3 years):")
            sb.appendLine(result.qualitativeForecast.mediumTermProspects)
            sb.appendLine("\nLong-term Potential (3-5+ years):")
            sb.appendLine(result.qualitativeForecast.longTermPotential)
            sb.appendLine("\nKey Success Factors:")
            result.qualitativeForecast.keySuccessFactors.forEach { sb.appendLine("- $it") }
            sb.appendLine("\nPotential Challenges:")
            result.qualitativeForecast.potentialChallenges.forEach { sb.appendLine("- $it") }
            sb.appendLine("\nMarket Trends Impact:")
            sb.appendLine(result.qualitativeForecast.marketTrendsImpact)
        }
        
        return sb.toString()
    }
    
    override suspend fun detectIndustry(documentText: String): String? {
        return detectIndustrySync(documentText)
    }
    
    override fun detectIndustrySync(documentText: String): String? {
        // Default implementation - try to detect from keywords
        // In production, this should use AI
        val textLower = documentText.lowercase()
        return when {
            textLower.contains("energy") || textLower.contains("solar") || 
            textLower.contains("wind") || textLower.contains("renewable") ||
            textLower.contains("power") || textLower.contains("electricity") -> "Energy"
            textLower.contains("software") || textLower.contains("technology") ||
            textLower.contains("tech") || textLower.contains("app") ||
            textLower.contains("platform") || textLower.contains("digital") -> "Tech"
            else -> null
        }
    }
    
    override suspend fun extractStartupInfo(documentText: String): ExtractedStartupInfo {
        return extractStartupInfoSync(documentText)
    }
    
    override fun extractStartupInfoSync(documentText: String): ExtractedStartupInfo {
        // Default implementation - basic extraction using simple heuristics
        // In production, this should use AI
        
        val lines = documentText.lines()
        var startupName: String? = null
        var problemStatement: String? = null
        var proposedSolution: String? = null
        
        // Try to extract startup name from first few lines or title
        for (i in 0 until minOf(20, lines.size)) {
            val line = lines[i].trim()
            if (line.isNotBlank() && line.length > 3 && line.length < 100) {
                // Common patterns for business name
                if (line.matches(Regex(".*[A-Z][a-z]+.*"))) {
                    startupName = line.take(100)
                    break
                }
            }
        }
        
        // Try to find problem statement section
        var foundProblem = false
        val problemKeywords = listOf("problem", "challenge", "issue", "pain point", "gap")
        val problemText = StringBuilder()
        
        for (i in lines.indices) {
            val lineLower = lines[i].lowercase()
            if (problemKeywords.any { lineLower.contains(it) } && !foundProblem) {
                foundProblem = true
                problemText.appendLine(lines[i])
            } else if (foundProblem) {
                if (lineLower.contains("solution") || lineLower.contains("approach") || 
                    lineLower.isBlank() || lineLower.matches(Regex("^\\s*[0-9]+\\.?\\s*.*"))) {
                    if (problemText.isNotBlank()) break
                } else {
                    problemText.appendLine(lines[i])
                }
            }
            if (problemText.length > 500) break
        }
        problemStatement = problemText.toString().trim().take(1000).ifBlank { null }
        
        // Try to find proposed solution section
        var foundSolution = false
        val solutionKeywords = listOf("solution", "approach", "product", "service", "proposal")
        val solutionText = StringBuilder()
        
        for (i in lines.indices) {
            val lineLower = lines[i].lowercase()
            if (solutionKeywords.any { lineLower.contains(it) } && !foundSolution) {
                foundSolution = true
                solutionText.appendLine(lines[i])
            } else if (foundSolution) {
                if (lineLower.contains("market") || lineLower.contains("financial") ||
                    lineLower.isBlank() || lineLower.matches(Regex("^\\s*[0-9]+\\.?\\s*.*"))) {
                    if (solutionText.isNotBlank()) break
                } else {
                    solutionText.appendLine(lines[i])
                }
            }
            if (solutionText.length > 500) break
        }
        proposedSolution = solutionText.toString().trim().take(1000).ifBlank { null }
        
        return ExtractedStartupInfo(
            startupName = startupName,
            problemStatement = problemStatement,
            proposedSolution = proposedSolution
        )
    }
    
    /**
     * Creates a placeholder structured result
     * This will be replaced with actual AI analysis
     */
    private fun createPlaceholderResult(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile> = emptyList()): AnalysisResult {
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
            ),
            qualitativeForecast = org.example.model.QualitativeForecast(
                shortTermOutlook = "In the next 6-12 months, the startup is expected to focus on product development and initial market validation. Key milestones may include completing the MVP, securing early customers, and establishing core operational processes.",
                mediumTermProspects = "Over the next 1-3 years, the startup has potential for growth if it can successfully validate its market fit, secure additional funding, and scale operations. Success will depend on customer acquisition, revenue generation, and competitive positioning.",
                longTermPotential = "In the long term (3-5+ years), the startup could achieve market leadership if it successfully navigates growth challenges, maintains competitive advantages, and adapts to evolving market conditions. Strategic partnerships and continuous innovation will be critical.",
                keySuccessFactors = listOf(
                    "Market validation and customer acquisition",
                    "Sustainable revenue model",
                    "Strong team execution",
                    "Competitive differentiation"
                ),
                potentialChallenges = listOf(
                    "Market competition and saturation",
                    "Funding and cash flow management",
                    "Scaling operations efficiently",
                    "Regulatory and compliance requirements"
                ),
                marketTrendsImpact = "Market trends suggest both opportunities and challenges. Emerging technologies and shifting consumer behaviors may create new market segments, while economic conditions and regulatory changes could impact growth trajectories."
            )
        )
    }
}
