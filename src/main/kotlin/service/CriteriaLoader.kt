package org.example.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.example.model.CriteriaConfig
import java.io.InputStream

/**
 * Loads criteria configuration from YAML file
 */
object CriteriaLoader {
    private val yamlMapper = ObjectMapper(YAMLFactory()).apply {
        registerKotlinModule()
    }
    
    /**
     * Loads criteria configuration from resources
     * @param industry Optional industry name to load industry-specific criteria
     * @return CriteriaConfig or null if file not found
     */
    fun loadFromResources(industry: String? = null): CriteriaConfig? {
        return try {
            // Determine which criteria file to load based on industry
            val fileName = when (industry?.lowercase()) {
                "tech" -> "criteria_tech.yaml"
                "energy" -> "criteria_energy.yaml"
                else -> "criteria.yaml" // Default criteria
            }
            
            val inputStream: InputStream? = CriteriaLoader::class.java
                .classLoader
                .getResourceAsStream(fileName)
            
            inputStream?.use {
                yamlMapper.readValue(it, CriteriaConfig::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Creates a default criteria configuration if file loading fails
     */
    fun getDefaultConfig(): CriteriaConfig {
        // Return a default configuration structure
        // This matches the structure in criteria.yaml
        return CriteriaConfig(
            categories = emptyList(), // Will be populated from YAML
            rubrics = org.example.model.Rubrics(
                scoreLevels = listOf(
                    org.example.model.ScoreLevel(5, "Excellent", "Exceeds expectations"),
                    org.example.model.ScoreLevel(4, "Good", "Meets expectations"),
                    org.example.model.ScoreLevel(3, "Average", "Meets basic requirements"),
                    org.example.model.ScoreLevel(2, "Below Average", "Does not fully meet requirements"),
                    org.example.model.ScoreLevel(1, "Poor", "Fails to meet requirements")
                )
            ),
            decisionMapping = org.example.model.DecisionMapping(
                fund = org.example.model.DecisionThreshold(minScore = 4.0, description = "Recommend funding"),
                partial = org.example.model.DecisionThreshold(minScore = 3.0, maxScore = 3.9, description = "Recommend partial funding"),
                decline = org.example.model.DecisionThreshold(maxScore = 2.9, description = "Recommend decline")
            )
        )
    }
}

/**
 * Loads prompt template from markdown file
 */
object PromptTemplateLoader {
    /**
     * Loads prompt template from resources
     */
    fun loadFromResources(): String? {
        return try {
            val inputStream: InputStream? = PromptTemplateLoader::class.java
                .classLoader
                .getResourceAsStream("prompt_template.md")
            
            inputStream?.use {
                it.bufferedReader().readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Creates a default prompt template if file loading fails
     */
    fun getDefaultTemplate(): String {
        return """
            You are a decision-support assistant for BMO's startup evaluation process.
            Analyze the startup submission and provide structured scoring and recommendations.
            Output must be in JSON format as specified in the template.
        """.trimIndent()
    }
}
