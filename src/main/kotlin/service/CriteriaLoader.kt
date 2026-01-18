package org.example.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.example.model.CriteriaConfig
import org.example.model.CommonConfig
import org.example.model.IndustryConfig
import java.io.InputStream

/**
 * Loads criteria configuration from YAML files
 * Supports both legacy single-file format and new modular format
 */
object CriteriaLoader {
    private val yamlMapper = ObjectMapper(YAMLFactory()).apply {
        registerKotlinModule()
    }
    
    /**
     * Loads criteria configuration from resources using modular structure
     * Loads common.yaml and industry-specific file, then merges them
     * @param industry Optional industry name to load industry-specific criteria
     * @param databaseService Optional database service to check for custom criteria first
     * @return CriteriaConfig or null if files not found
     */
    fun loadFromResources(industry: String? = null, databaseService: org.example.service.database.DatabaseService? = null): CriteriaConfig? {
        return try {
            // First check if there's a custom config in database
            if (industry != null && databaseService != null) {
                val customConfig = databaseService.loadCriteriaConfigSync(industry)
                if (customConfig != null) {
                    return customConfig
                }
            }
            // Try to load from new modular structure first
            loadModularConfig(industry) ?: loadLegacyConfig(industry)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Loads configuration from new modular structure
     * Combines common.yaml with industry-specific categories
     */
    private fun loadModularConfig(industry: String?): CriteriaConfig? {
        // Load common configuration (rubrics and decision_mapping)
        val commonConfig = loadCommonConfig() ?: return null
        
        // Load industry-specific categories
        val industryConfig = loadIndustryConfig(industry) ?: return null
        
        // Merge into complete CriteriaConfig
        return CriteriaConfig(
            categories = industryConfig.categories,
            rubrics = commonConfig.rubrics,
            decisionMapping = commonConfig.decisionMapping
        )
    }
    
    /**
     * Loads common configuration from criteria/common.yaml
     */
    private fun loadCommonConfig(): CommonConfig? {
        return try {
            val inputStream: InputStream? = CriteriaLoader::class.java
                .classLoader
                .getResourceAsStream("criteria/common.yaml")
            
            inputStream?.use {
                yamlMapper.readValue(it, CommonConfig::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Loads industry-specific categories from criteria/industries/{industry}.yaml
     */
    private fun loadIndustryConfig(industry: String?): IndustryConfig? {
        // Determine which industry file to load
        val fileName = when (industry?.lowercase()) {
            "tech" -> "criteria/industries/tech.yaml"
            "energy" -> "criteria/industries/energy.yaml"
            else -> "criteria/industries/default.yaml"
        }
        
        return try {
            val inputStream: InputStream? = CriteriaLoader::class.java
                .classLoader
                .getResourceAsStream(fileName)
            
            inputStream?.use {
                yamlMapper.readValue(it, IndustryConfig::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Loads configuration from legacy single-file format (backward compatibility)
     */
    private fun loadLegacyConfig(industry: String?): CriteriaConfig? {
        // Determine which criteria file to load based on industry
        val fileName = when (industry?.lowercase()) {
            "tech" -> "criteria_tech.yaml"
            "energy" -> "criteria_energy.yaml"
            else -> "criteria.yaml" // Default criteria
        }
        
        return try {
            val inputStream: InputStream? = CriteriaLoader::class.java
                .classLoader
                .getResourceAsStream(fileName)
            
            inputStream?.use {
                yamlMapper.readValue(it, CriteriaConfig::class.java)
            }
        } catch (e: Exception) {
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
