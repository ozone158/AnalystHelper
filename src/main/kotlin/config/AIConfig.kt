package org.example.config

/**
 * AI Service Configuration
 * 
 * Configure your AI provider and API key here.
 * This is the only file you need to edit to switch AI providers or set your API key.
 */
object AIConfig {
    // Choose your AI provider: DEFAULT, OPENAI
    val provider: AIProviderType = AIProviderType.OPENAI
    
    // Your OpenAI API key (leave empty to use OPENAI_API_KEY environment variable)
    val openAIApiKey: String = "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"  // Put your API key here, or leave empty to use env var
    
    // OpenAI model to use (gpt-4o-mini, gpt-4o, gpt-4-turbo, gpt-4, gpt-3.5-turbo)
    val openAIModel: String = "gpt-4o-mini"
}

/**
 * Available AI provider types
 */
enum class AIProviderType {
    DEFAULT,    // Placeholder implementation (no API key needed)
    OPENAI      // OpenAI GPT models (requires API key)
}
