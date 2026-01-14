package org.example.service.provider.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.example.StartupSubmissionData
import org.example.model.AnalysisResult
import org.example.service.AIService
import org.example.service.CriteriaLoader
import org.example.service.PromptTemplateLoader
import java.net.HttpURLConnection
import java.net.URL

/**
 * OpenAI implementation of AIService
 * Requires OpenAI API key to be set
 * 
 * This service uses OpenAI's Chat Completions API to analyze startup submissions
 * according to the criteria configuration and prompt template.
 */
class OpenAIService(
    private val apiKey: String,
    private val model: String = "gpt-4o-mini" // Can be changed to gpt-4, gpt-4-turbo, etc.
) : AIService {
    
    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
        // Enable case-insensitive enum deserialization
        // This handles OpenAI returning "medium" instead of "MEDIUM"
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    }
    
    private val promptTemplate = PromptTemplateLoader.loadFromResources() ?: PromptTemplateLoader.getDefaultTemplate()
    
    override suspend fun analyzeSubmission(data: StartupSubmissionData): AnalysisResult {
        return analyzeSubmissionSync(data)
    }
    
    override fun analyzeSubmissionSync(data: StartupSubmissionData): AnalysisResult {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("OpenAI API key is required. Please set it in the configuration.")
        }
        
        try {
            // Load industry-specific criteria
            val criteriaConfig = CriteriaLoader.loadFromResources(data.industry) ?: CriteriaLoader.getDefaultConfig()
            
            // Build the prompt using template and submission data
            val prompt = buildPrompt(data, criteriaConfig)
            
            // Call OpenAI API
            val response = callOpenAIAPI(prompt)
            
            // Parse JSON response
            return parseAIResponse(response)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Return placeholder result on error
            return createPlaceholderResult(data)
        }
    }
    
    override fun formatAnalysisResult(result: AnalysisResult): String {
        // Use the same formatting as DefaultAIService
        val defaultService = org.example.service.DefaultAIService()
        return defaultService.formatAnalysisResult(result)
    }
    
    /**
     * Builds the prompt from template and submission data
     */
    private fun buildPrompt(data: StartupSubmissionData, criteriaConfig: org.example.model.CriteriaConfig): String {
        val criteriaYaml = objectMapper.writeValueAsString(criteriaConfig)
        
        // Format criteria answers if available
        val criteriaAnswersSection = if (data.criteriaAnswers.isNotEmpty()) {
            formatCriteriaAnswers(data.criteriaAnswers, criteriaConfig)
        } else {
            ""
        }
        
        return """
            $promptTemplate
            
            === SUBMISSION DATA ===
            Startup Name: ${data.startupName}
            Industry: ${data.industry}
            Stage: ${data.stage.name.lowercase().replace("_", " ")}
            
            Problem Statement:
            ${data.problemStatement}
            
            Proposed Solution:
            ${data.proposedSolution}
            
            ${if (criteriaAnswersSection.isNotEmpty()) """
            === FOUNDER RESPONSES TO EVALUATION QUESTIONS ===
            The founder has provided the following responses to evaluation questions based on the criteria. Use these responses to inform your analysis:
            
            $criteriaAnswersSection
            
            """ else ""}
            === CRITERIA CONFIGURATION ===
            $criteriaYaml
            
            Please analyze this submission according to the criteria and provide your analysis in the specified JSON format.${if (criteriaAnswersSection.isNotEmpty()) " Pay special attention to the founder's responses to the evaluation questions, as they provide detailed information relevant to each criterion." else ""}
        """.trimIndent()
    }
    
    /**
     * Formats criteria answers in a readable format for the AI prompt
     */
    private fun formatCriteriaAnswers(
        answers: Map<String, String>,
        criteriaConfig: org.example.model.CriteriaConfig
    ): String {
        if (answers.isEmpty()) return ""
        
        val sb = StringBuilder()
        
        // Group answers by category
        val answersByCategory = mutableMapOf<String, MutableList<Pair<String, String>>>()
        
        criteriaConfig.categories.forEach { category ->
            category.criteria.forEach { criterion ->
                val questionId = "${category.name}_${criterion.name}"
                val answer = answers[questionId]
                if (!answer.isNullOrBlank()) {
                    answersByCategory.getOrPut(category.name) { mutableListOf() }
                        .add(criterion.name to answer)
                }
            }
        }
        
        // Format by category
        answersByCategory.forEach { (categoryName, criterionAnswers) ->
            sb.appendLine("\n--- $categoryName ---")
            criterionAnswers.forEach { (criterionName, answer) ->
                sb.appendLine("\nCriterion: $criterionName")
                sb.appendLine("Founder's Response:")
                sb.appendLine(answer)
                sb.appendLine()
            }
        }
        
        return sb.toString().trim()
    }
    
    /**
     * Calls OpenAI API with the prompt
     */
    private fun callOpenAIAPI(prompt: String): String {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.doOutput = true
        
        // Build request body using ObjectMapper for proper JSON serialization
        val requestBody = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf(
                    "role" to "system",
                    "content" to "You are an AI assistant that analyzes startup submissions. Always respond with valid JSON only, no additional text."
                ),
                mapOf(
                    "role" to "user",
                    "content" to prompt
                )
            ),
            "temperature" to 0.3,
            "response_format" to mapOf("type" to "json_object")
        )
        
        val jsonRequestBody = objectMapper.writeValueAsString(requestBody)
        
        connection.outputStream.use { 
            it.write(jsonRequestBody.toByteArray(Charsets.UTF_8))
        }
        
        val responseCode = connection.responseCode
        if (responseCode != 200) {
            val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
            throw RuntimeException("OpenAI API error: $responseCode - $errorBody")
        }
        
        val response = connection.inputStream.bufferedReader().readText()
        
        // Parse the response to extract the content
        val jsonResponse = objectMapper.readTree(response)
        val content = jsonResponse["choices"][0]["message"]["content"].asText()
        
        return content
    }
    
    /**
     * Parses the AI JSON response into AnalysisResult
     */
    private fun parseAIResponse(jsonResponse: String): AnalysisResult {
        try {
            // The response should be a JSON object matching AnalysisResult structure
            return objectMapper.readValue<AnalysisResult>(jsonResponse)
        } catch (e: Exception) {
            // If parsing fails, try to extract JSON from markdown code blocks
            val jsonMatch = Regex("```(?:json)?\\s*([\\s\\S]*?)```").find(jsonResponse)
            val jsonContent = jsonMatch?.groupValues?.get(1) ?: jsonResponse
            
            return objectMapper.readValue<AnalysisResult>(jsonContent.trim())
        }
    }
    
    /**
     * Creates a placeholder result (fallback)
     */
    private fun createPlaceholderResult(data: StartupSubmissionData): AnalysisResult {
        val defaultService = org.example.service.DefaultAIService()
        // This will create a placeholder result
        // In a real scenario, you might want to handle errors differently
        return defaultService.analyzeSubmissionSync(data)
    }
}
