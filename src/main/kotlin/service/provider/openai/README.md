# OpenAI Service - Quick Start

## Get Your API Key

1. Sign up at https://platform.openai.com/
2. Go to API Keys section
3. Create a new secret key

## How to Use

**Edit `src/main/kotlin/config/AIConfig.kt`:**

```kotlin
object AIConfig {
    val provider: AIProviderType = AIProviderType.OPENAI  // Set to OPENAI
    val openAIApiKey: String = "your-api-key-here"        // Put your API key here
    val openAIModel: String = "gpt-4o-mini"               // Choose your model
}
```

That's it! The service will automatically use your configuration.

### Alternative: Environment Variable

Leave `openAIApiKey` empty and set environment variable instead:

**Windows PowerShell:**
```powershell
$env:OPENAI_API_KEY="your-api-key-here"
```

**Windows CMD:**
```cmd
set OPENAI_API_KEY=your-api-key-here
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY="your-api-key-here"
```

## Available Models

- `gpt-4o-mini` (default) - Fast and cheap
- `gpt-4o` - Better quality
- `gpt-4-turbo` - Latest GPT-4

To use a different model:
```kotlin
AIServiceFactory.create(AIProvider.OPENAI, "your-key", model = "gpt-4o")
```

## Troubleshooting

**"OpenAI API key is required"**  
→ Make sure you set the API key correctly

**401 Unauthorized**  
→ Check if your API key is correct and has credits

**429 Rate Limit**  
→ Wait a moment or upgrade your OpenAI plan

## That's It!

The service will automatically:
- Use your criteria configuration
- Format prompts correctly
- Parse AI responses
- Handle errors gracefully

For more details, see the code in `OpenAIService.kt`.
