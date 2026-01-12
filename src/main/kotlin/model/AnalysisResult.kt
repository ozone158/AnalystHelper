package org.example.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Structured analysis result matching the JSON output format
 * from the AI prompt template
 */
data class AnalysisResult(
    @JsonProperty("overall_score")
    val overallScore: Double,
    @JsonProperty("category_scores")
    val categoryScores: List<CategoryScore>,
    @JsonProperty("risk_assessment")
    val riskAssessment: RiskAssessment,
    val recommendation: Recommendation,
    @JsonProperty("recommendation_reasoning")
    val recommendationReasoning: String,
    @JsonProperty("key_strengths")
    val keyStrengths: List<String>,
    @JsonProperty("key_concerns")
    val keyConcerns: List<String>,
    @JsonProperty("data_quality")
    val dataQuality: DataQuality
)

data class CategoryScore(
    @JsonProperty("category_name")
    val categoryName: String,
    @JsonProperty("category_weight")
    val categoryWeight: Double,
    @JsonProperty("category_score")
    val categoryScore: Double,
    @JsonProperty("criteria_scores")
    val criteriaScores: List<CriterionScore>,
    @JsonProperty("category_reasoning")
    val categoryReasoning: String
)

data class CriterionScore(
    @JsonProperty("criterion_name")
    val criterionName: String,
    @JsonProperty("criterion_weight")
    val criterionWeight: Double,
    val score: Int,
    val reasoning: String,
    @JsonProperty("supporting_evidence")
    val supportingEvidence: List<String>
)

data class RiskAssessment(
    @JsonProperty("privacy_security")
    val privacySecurity: RiskDimension,
    val compliance: RiskDimension,
    val market: RiskDimension,
    val technical: RiskDimension
)

data class RiskDimension(
    val level: RiskLevel,
    val description: String,
    val concerns: List<String>
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

enum class Recommendation {
    FUND, PARTIAL, DECLINE
}

data class DataQuality(
    val completeness: Completeness,
    val gaps: List<String>,
    @JsonProperty("impact_on_analysis")
    val impactOnAnalysis: String
)

enum class Completeness {
    COMPLETE, PARTIAL, INCOMPLETE
}
