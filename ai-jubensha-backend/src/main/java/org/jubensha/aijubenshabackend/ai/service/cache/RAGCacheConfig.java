package org.jubensha.aijubenshabackend.ai.service.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * RAG缓存配置
 * 为RAG服务提供缓存支持，优化存储和检索性能
 */
@Configuration
public class RAGCacheConfig {

    /**
     * 对话记忆检索缓存
     * 缓存对话记忆的检索结果，减少重复查询
     */
    @Bean
    public Cache<String, List<Map<String, Object>>> conversationMemoryCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .build();
    }

    /**
     * 全局记忆检索缓存
     * 缓存全局记忆的检索结果，减少重复查询
     */
    @Bean
    public Cache<String, List<Map<String, Object>>> globalMemoryCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofHours(1))
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .build();
    }

    /**
     * 父文档缓存
     * 缓存父文档内容，减少重复获取
     */
    @Bean
    public Cache<Long, Map<String, Object>> parentDocumentCache() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(Duration.ofHours(2))
                .expireAfterAccess(Duration.ofHours(1))
                .recordStats()
                .build();
    }

    /**
     * 嵌入向量缓存
     * 缓存文本嵌入向量，减少重复计算
     */
    @Bean
    public Cache<String, List<Float>> embeddingCache() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofHours(24))
                .recordStats()
                .build();
    }

    /**
     * 事实提取缓存
     * 缓存事实提取结果，减少重复LLM调用
     */
    @Bean
    public Cache<String, List<Map<String, Object>>> factExtractionCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofHours(6))
                .recordStats()
                .build();
    }
}
