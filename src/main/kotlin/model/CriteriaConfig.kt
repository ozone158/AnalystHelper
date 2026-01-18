package org.example.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Configuration model for evaluation criteria
 * Maps to criteria.yaml structure
 */
data class CriteriaConfig(
    val categories: List<Category>,
    val rubrics: Rubrics,
    @JsonProperty("decision_mapping")
    val decisionMapping: DecisionMapping
)

/**
 * Common configuration containing shared rubrics and decision mapping
 * Used in modular criteria structure
 */
data class CommonConfig(
    val rubrics: Rubrics,
    @JsonProperty("decision_mapping")
    val decisionMapping: DecisionMapping
)

/**
 * Industry-specific configuration containing only categories
 * Used in modular criteria structure
 */
data class IndustryConfig(
    val categories: List<Category>
)

data class Category(
    val name: String,
    val weight: Double,
    val criteria: List<Criterion>
)

data class Criterion(
    val name: String,
    val weight: Double,
    val description: String
)

data class Rubrics(
    @JsonProperty("score_levels")
    val scoreLevels: List<ScoreLevel>
)

data class ScoreLevel(
    val level: Int,
    val label: String,
    val description: String
)

data class DecisionMapping(
    val fund: DecisionThreshold,
    val partial: DecisionThreshold? = null,
    val decline: DecisionThreshold
)

data class DecisionThreshold(
    @JsonProperty("min_score")
    val minScore: Double? = null,
    @JsonProperty("max_score")
    val maxScore: Double? = null,
    val description: String
)
