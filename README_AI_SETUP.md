# AI Service Setup Guide

This application supports multiple AI providers through a modular architecture. You can easily switch between different AI services by simply changing the provider configuration.

## Quick Start

### Using OpenAI (Recommended)

1. **Get your OpenAI API Key**
   - Sign up at https://platform.openai.com/
   - Navigate to API Keys section
   - Create a new secret key

2. **Configure the API Key**

   **Option 1: Environment Variable (Recommended)**
   ```bash
   # Windows (PowerShell)
   $env:OPENAI_API_KEY="your-api-key-here"
   
   # Windows (CMD)
   set OPENAI_API_KEY=your-api-key-here
   
   # Linux/Mac
   export OPENAI_API_KEY="your-api-key-here"
   ```

   Then in `Main.kt`, use:
   ```kotlin
   val aiService: AIService = remember { 
       AIServiceFactory.createFromEnvironment(AIProvider.OPENAI)
   }
   ```

   **Option 2: Direct Configuration**
   In `Main.kt`, change:
   ```kotlin
   val aiService: AIService = remember { 
       AIServiceFactory.create(AIProvider.OPENAI, "your-api-key-here")
   }
   ```

3. **Choose a Model** (optional)
   ```kotlin
   val aiService: AIService = remember { 
       AIServiceFactory.create(AIProvider.OPENAI, "your-api-key-here", model = "gpt-4")
   }
   ```
   
   Available models:
   - `gpt-4o-mini` (default, cost-effective)
   - `gpt-4o`
   - `gpt-4-turbo`
   - `gpt-4`
   - `gpt-3.5-turbo`

### Using Default Placeholder (No API Key Required)

For testing or development without an API key:
```kotlin
val aiService: AIService = remember { 
    AIServiceFactory.create(AIProvider.DEFAULT)
}
```

## Switching AI Providers

The architecture is designed for easy switching. To add a new provider:

1. Create a new class implementing `AIService`
2. Add the provider to `AIProvider` enum in `AIServiceFactory.kt`
3. Update the factory's `create()` method

Example for adding Anthropic Claude:
```kotlin
// In AIServiceFactory.kt
enum class AIProvider {
    DEFAULT,
    OPENAI,
    ANTHROPIC  // Add new provider
}

// In create() method
AIProvider.ANTHROPIC -> AnthropicService(apiKey)
```

## Architecture

- **AIService Interface**: Defines the contract for all AI implementations
- **OpenAIService**: OpenAI GPT models implementation
- **DefaultAIService**: Placeholder implementation for testing
- **AIServiceFactory**: Factory pattern for easy provider switching

All implementations use:
- Criteria configuration from `criteria.yaml`
- Prompt template from `prompt_template.md`
- Structured JSON output format
- Same interface, so switching is seamless

## Notes

- The OpenAI implementation uses the Chat Completions API
- Responses are parsed as JSON matching the `AnalysisResult` structure
- Error handling falls back to placeholder results if API calls fail
- API key should never be committed to version control
