package org.example.service.ai.provider.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.example.model.StartupSubmissionData
import org.example.model.AnalysisResult
import org.example.model.ExtractedStartupInfo
import org.example.service.ai.AIService
import org.example.service.ai.provider.default.DefaultAIService
import org.example.service.CriteriaLoader
import org.example.service.PromptTemplateLoader
import org.example.service.FileReader
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
    
    override suspend fun analyzeSubmission(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile>): AnalysisResult {
        return analyzeSubmissionSync(data, industryFiles)
    }
    
    override fun analyzeSubmissionSync(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile>): AnalysisResult {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("OpenAI API key is required. Please set it in the configuration.")
        }
        
        // Filter industry files to only those matching the submission's industry
        val relevantFiles = industryFiles.filter { 
            it.industry.equals(data.industry, ignoreCase = true) 
        }
        
        try {
            // Load industry-specific criteria
            val criteriaConfig = CriteriaLoader.loadFromResources(data.industry) ?: CriteriaLoader.getDefaultConfig()
            
            // Build the prompt using template and submission data
            val prompt = buildPrompt(data, criteriaConfig, relevantFiles)
            
            // Call OpenAI API
            val response = callOpenAIAPI(prompt)
            
            // Parse JSON response
            return parseAIResponse(response)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Return placeholder result on error
            return createPlaceholderResult(data, relevantFiles)
        }
    }
    
    override fun formatAnalysisResult(result: AnalysisResult): String {
        // Use the same formatting as DefaultAIService
        val defaultService = DefaultAIService()
        return defaultService.formatAnalysisResult(result)
    }
    
    override suspend fun detectIndustry(documentText: String): String? {
        return detectIndustrySync(documentText)
    }
    
    override fun detectIndustrySync(documentText: String): String? {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("OpenAI API key is required. Please set it in the configuration.")
        }
        
        try {
            // Truncate document text if too long (keep first 10000 characters for cost efficiency)
            val truncatedText = documentText.take(10000)
            
            val prompt = """
                You are an AI assistant that analyzes business plans to determine the industry category.
                
                Based on the following business plan document, determine which industry category this startup belongs to.
                
                Available industry categories: Tech, Energy
                
                Analyze the document content and return ONLY the industry name (Tech or Energy) as a JSON object with the format:
                {"industry": "Tech"} or {"industry": "Energy"}
                
                If you cannot determine the industry clearly, return: {"industry": null}
                
                Business Plan Content:
                $truncatedText
            """.trimIndent()
            
            val response = callOpenAIAPI(prompt)
            val jsonResponse = objectMapper.readTree(response)
            val industry = jsonResponse["industry"]?.asText()
            
            // Normalize to match our industry categories
            return when (industry?.lowercase()) {
                "tech", "technology" -> "Tech"
                "energy" -> "Energy"
                else -> null
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Fall back to default implementation
            val defaultService = DefaultAIService()
            return defaultService.detectIndustrySync(documentText)
        }
    }
    
    /**
     * Builds the prompt from template and submission data
     */
    private fun buildPrompt(
        data: StartupSubmissionData, 
        criteriaConfig: org.example.model.CriteriaConfig,
        industryFiles: List<org.example.model.IndustryFile>
    ): String {
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
            ${if (industryFiles.isNotEmpty()) {
                FileReader.formatIndustryFilesForPrompt(industryFiles)
            } else ""}
            === CRITERIA CONFIGURATION ===
            $criteriaYaml
            
            Please analyze this submission according to the criteria and provide your analysis in the specified JSON format.${if (criteriaAnswersSection.isNotEmpty()) " Pay special attention to the founder's responses to the evaluation questions, as they provide detailed information relevant to each criterion." else ""}${if (industryFiles.isNotEmpty()) " IMPORTANT: Use the industry statistics and data files provided above to inform your analysis, especially for market opportunity assessment, competitive analysis, and industry benchmarks. Compare the startup's claims against the industry data provided." else ""}
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
    private fun createPlaceholderResult(data: StartupSubmissionData, industryFiles: List<org.example.model.IndustryFile> = emptyList()): AnalysisResult {
        val defaultService = DefaultAIService()
        // This will create a placeholder result
        // In a real scenario, you might want to handle errors differently
        return defaultService.analyzeSubmissionSync(data, industryFiles)
    }
    
    override suspend fun extractStartupInfo(documentText: String): ExtractedStartupInfo {
        return extractStartupInfoSync(documentText)
    }
    
    override fun extractStartupInfoSync(documentText: String): ExtractedStartupInfo {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("OpenAI API key is required. Please set it in the configuration.")
        }
        
        try {
            // Truncate document text if too long (keep first 15000 characters for cost efficiency)
            val truncatedText = documentText.take(15000)
            
            val prompt = """
                You are an AI assistant that extracts key information from business plan documents.
                
                Based on the following business plan document, extract the following information:
                1. Startup/Company Name
                2. Problem Statement (the problem the startup is trying to solve)
                3. Proposed Solution (the product/service being offered)
                
                Return ONLY a JSON object with the format:
                {
                    "startupName": "Name of the startup or company",
                    "problemStatement": "Description of the problem being addressed (2-3 sentences)",
                    "proposedSolution": "Description of the proposed solution/product/service (2-3 sentences)"
                }
                
                If any information cannot be found, use null for that field.
                
                Business Plan Content:
                $truncatedText
            """.trimIndent()
            
            val response = callOpenAIAPI(prompt)
            val jsonResponse = objectMapper.readTree(response)
            
            val startupName = jsonResponse["startupName"]?.takeIf { !it.isNull }?.asText()
            val problemStatement = jsonResponse["problemStatement"]?.takeIf { !it.isNull }?.asText()
            val proposedSolution = jsonResponse["proposedSolution"]?.takeIf { !it.isNull }?.asText()
            
            return ExtractedStartupInfo(
                startupName = startupName,
                problemStatement = problemStatement,
                proposedSolution = proposedSolution
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Fall back to default implementation
            val defaultService = DefaultAIService()
            return defaultService.extractStartupInfoSync(documentText)
        }
    }
}
