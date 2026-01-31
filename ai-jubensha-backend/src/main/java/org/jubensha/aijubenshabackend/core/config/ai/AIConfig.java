package org.jubensha.aijubenshabackend.core.config.ai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    
    @Value("${ai.api-key}")
    private String apiKey;
    
    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.embedding-base-url}")
    private String embeddingBaseUrl;
    
    @Value("${ai.model:deepseek-chat}")
    private String modelName;
    
    @Value("${ai.embedding-model:text-embedding-ada-002}")
    private String embeddingModelName;

    @Value("${ai.embedding-api-key}")
    private String embeddingApiKey;
    
    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)  // 使用配置的模型名称，而不是硬编码
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(300))  // 设置超时时间为300秒
                .build();
    }
    
    @Bean
    public EmbeddingModel embeddingModel() {
        // 由于DeepSeek API可能不支持专门的嵌入模型，我们使用OpenAI兼容的嵌入模型
        // 可以使用text-embedding-ada-002或其他兼容的嵌入模型
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(embeddingModelName)  // 使用配置的嵌入模型名称
                .build();
    }
}