package org.jubensha.aijubenshabackend.core.config.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
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
        log.info("创建 OpenAiChatModel 实例...");
        log.info("模型名称: {}", modelName);
        log.info("基础URL: {}", baseUrl);
        log.info("API密钥配置: {}", apiKey != null && !apiKey.isEmpty() ? "已配置" : "未配置");
        
        try {
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)  // 使用配置的模型名称，而不是硬编码
                    .temperature(0.7)
                    .timeout(java.time.Duration.ofSeconds(300))  // 设置超时时间为300秒
                    .maxTokens(4096)  // 设置最大token数，确保生成完整的剧本JSON
                    .build();
            
            log.info("OpenAiChatModel 创建成功");
            return chatModel;
        } catch (Exception e) {
            log.error("创建 OpenAiChatModel 失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean(name = "streamingChatModel")
    public OpenAiStreamingChatModel streamingChatModel() {
        log.info("创建 OpenAiStreamingChatModel 实例...");
        log.info("模型名称: {}", modelName);
        log.info("基础URL: {}", baseUrl);
        log.info("API密钥配置: {}", apiKey != null && !apiKey.isEmpty() ? "已配置" : "未配置");
        
        try {
            OpenAiStreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)  // 使用配置的模型名称，而不是硬编码
                    .temperature(0.7)
                    .timeout(java.time.Duration.ofSeconds(300))  // 设置超时时间为300秒
                    .maxTokens(4096)  // 设置最大token数，确保生成完整的剧本JSON
                    .build();
            
            log.info("OpenAiStreamingChatModel 创建成功");
            return streamingChatModel;
        } catch (Exception e) {
            log.error("创建 OpenAiStreamingChatModel 失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("创建 EmbeddingModel 实例...");
        log.info("嵌入模型名称: {}", embeddingModelName);
        log.info("嵌入基础URL: {}", embeddingBaseUrl);
        log.info("嵌入API密钥配置: {}", embeddingApiKey != null && !embeddingApiKey.isEmpty() ? "已配置" : "未配置");
        
        try {
            // 使用专门为嵌入模型配置的API密钥和baseUrl
            EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                    .apiKey(embeddingApiKey)
                    .baseUrl(embeddingBaseUrl)
                    .modelName(embeddingModelName)  // 使用配置的嵌入模型名称
                    .build();
            
            log.info("EmbeddingModel 创建成功");
            return embeddingModel;
        } catch (Exception e) {
            log.error("创建 EmbeddingModel 失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}