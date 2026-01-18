package org.example.service.ai

import org.example.service.ai.provider.openai.OpenAIService
import org.example.service.ai.provider.default.DefaultAIService

/**
 * Factory for creating AI service instances
 * Makes it easy to switch between different AI providers
 */
object AIServiceFactory {
    
    /**
     * Creates an AI service from AIConfig
     * This is the recommended way - edit AIConfig.kt to configure your AI provider
     */
    fun createFromConfig(): AIService {
        return when (AIConfig.provider) {
            AIProviderType.OPENAI -> {
                val key = AIConfig.openAIApiKey.ifBlank { 
                    System.getenv("OPENAI_API_KEY") ?: ""
                }
                if (key.isBlank()) {
                    throw IllegalArgumentException(
                        "OpenAI API key is required. " +
                        "Set it in AIConfig.kt (openAIApiKey) or set OPENAI_API_KEY environment variable"
                    )
                }
                OpenAIService(
                    apiKey = key,
                    model = AIConfig.openAIModel
                )
            }
            AIProviderType.DEFAULT -> DefaultAIService()
        }
    }
    
    /**
     * Creates an AI service based on configuration
     * @param provider The AI provider to use (OPENAI, DEFAULT, etc.)
     * @param apiKey The API key for the provider (if required)
     * @param model The model name (optional, provider-specific)
     * @return An instance of AIService
     */
    fun create(
        provider: AIProvider = AIProvider.DEFAULT,
        apiKey: String = "",
        model: String? = null
    ): AIService {
        return when (provider) {
            AIProvider.OPENAI -> {
                val key = apiKey.ifBlank { 
                    System.getenv("OPENAI_API_KEY") ?: ""
                }
                if (key.isBlank()) {
                    throw IllegalArgumentException(
                        "OpenAI API key is required. " +
                        "Set it as: AIServiceFactory.create(AIProvider.OPENAI, \"your-api-key\") " +
                        "or set OPENAI_API_KEY environment variable"
                    )
                }
                OpenAIService(
                    apiKey = key,
                    model = model ?: "gpt-4o-mini"
                )
            }
            AIProvider.DEFAULT -> DefaultAIService()
            // Add more providers here in the future
            // AIProvider.ANTHROPIC -> AnthropicService(apiKey)
            // AIProvider.GEMINI -> GeminiService(apiKey)
        }
    }
    
    /**
     * Creates AI service from environment variables
     * Reads OPENAI_API_KEY from environment if using OpenAI
     */
    fun createFromEnvironment(provider: AIProvider = AIProvider.DEFAULT): AIService {
        val apiKey = when (provider) {
            AIProvider.OPENAI -> System.getenv("OPENAI_API_KEY") ?: ""
            else -> ""
        }
        return create(provider, apiKey)
    }
}

/**
 * Enum for AI providers
 */
enum class AIProvider {
    DEFAULT,    // Placeholder implementation
    OPENAI,     // OpenAI GPT models
    // Add more providers as needed:
    // ANTHROPIC,  // Anthropic Claude
    // GEMINI,     // Google Gemini
    // LOCAL       // Local model
}
