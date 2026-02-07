package org.jubensha.aijubenshabackend.core.config.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

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
                .maxTokens(4096)  // 设置最大token数，确保生成完整的剧本JSON
                .build();
    }

    @Bean(name = "streamingChatModel")
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)  // 使用配置的模型名称，而不是硬编码
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(300))  // 设置超时时间为300秒
                .maxTokens(4096)  // 设置最大token数，确保生成完整的剧本JSON
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        // 使用专门为嵌入模型配置的API密钥和baseUrl
        return OpenAiEmbeddingModel.builder()
                .apiKey(embeddingApiKey)
                .baseUrl(embeddingBaseUrl)
                .modelName(embeddingModelName)  // 使用配置的嵌入模型名称
                .build();
    }
}