package org.jubensha.aijubenshabackend.ai.factory;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.guardrail.PromptSafetyInputGuardrail;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.ai.tools.ToolManager;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * 剧本生成服务类的工厂
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-30 21:22
 * @since 2026
 */

@Component
@Slf4j
public class ScriptGenerateServiceFactory {

    /**
     * 剧本生成 服务实例缓存
     * 缓存策略：
     * - 最大缓存 100 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, ScriptGenerateService> serviceCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            }) // 监听缓存项被移除的回调
            .build();

    //    @Resource
//    private RedisChatMemoryStore redisChatMemoryStore;
    @Resource(name = "openAiChatModel")
    private OpenAiChatModel chatModel;
    @Resource(name = "streamingChatModel")
    private StreamingChatModel streamingChatModel;
    @Resource
    private ToolManager toolManager;

    /**
     * 根据 scriptId 获取服务（默认使用流式服务）
     */
    public ScriptGenerateService getService(Long scriptId) {
        return getStreamingService(scriptId);
    }

    /**
     * 获取流式服务
     */
    public ScriptGenerateService getStreamingService(Long scriptId) {
        String cacheKey = "streaming:scriptId:" + scriptId;
        return serviceCache.get(cacheKey, key -> createStreamingScriptGenerateService(scriptId));
    }

    /**
     * 获取非流式服务
     */
    public ScriptGenerateService getNonStreamingService(Long scriptId) {
        String cacheKey = "non-streaming:scriptId:" + scriptId;
        return serviceCache.get(cacheKey, key -> createNonStreamingScriptGenerateService(scriptId));
    }

    /**
     * 创建新的流式剧本生成服务实例
     */
    private ScriptGenerateService createStreamingScriptGenerateService(Long scriptId) {
        log.info("创建新的流式剧本生成服务实例, 剧本ID：{}", scriptId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(scriptId)
//            .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(10)
                .build();

        // 检查 streamingChatModel 是否为 null
        if (streamingChatModel == null) {
            log.warn("streamingChatModel 为 null，尝试使用 chatModel 作为替代");
            // 检查 chatModel 是否为 null
            if (chatModel == null) {
                log.error("chatModel 也为 null，无法创建剧本生成服务");
                log.error("请检查 AI 配置：");
                log.error("1. 确保 application.yml 中的 ai.api-key 和 ai.base-url 配置正确");
                log.error("2. 确保 AIConfig 类中的 openAiChatModel 和 streamingChatModel bean 能够正常创建");
                log.error("3. 检查依赖是否正确加载");
                throw new IllegalArgumentException("Both streamingChatModel and chatModel are null. Please check AI configuration in application.yml");
            }
            // 使用非流式方法
            log.info("使用 chatModel 创建非流式服务作为替代");
            return createNonStreamingScriptGenerateService(scriptId);
        }

        log.info("使用 streamingChatModel 创建流式服务");

        return AiServices.builder(ScriptGenerateService.class)
                .chatModel(chatModel)
//            .chatMemoryProvider(memoryId -> chatMemory)
            .streamingChatModel(streamingChatModel)
                .tools(toolManager.getAllTools())
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no toll called" + toolExecutionRequest.name()))
                .maxSequentialToolsInvocations(20)
                .inputGuardrails(new PromptSafetyInputGuardrail())
                .build();
    }

    /**
     * 创建新的非流式剧本生成服务实例
     */
    private ScriptGenerateService createNonStreamingScriptGenerateService(Long scriptId) {
        log.info("创建新的非流式剧本生成服务实例, 剧本ID：{}", scriptId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(scriptId)
//            .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(10)
                .build();

        // 检查 chatModel 是否为 null
        if (chatModel == null) {
            log.error("chatModel 为 null，无法创建非流式剧本生成服务");
            log.error("请检查 AI 配置：");
            log.error("1. 确保 application.yml 中的 ai.api-key 和 ai.base-url 配置正确");
            log.error("2. 确保 AIConfig 类中的 openAiChatModel bean 能够正常创建");
            log.error("3. 检查依赖是否正确加载");
            throw new IllegalArgumentException("chatModel cannot be null. Please check AI configuration in application.yml");
        }

        log.info("使用 chatModel 创建非流式服务");

        return AiServices.builder(ScriptGenerateService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
//            .chatMemoryProvider(memoryId -> chatMemory)
                .tools(toolManager.getAllTools())
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no toll called" + toolExecutionRequest.name()))
                .maxSequentialToolsInvocations(20)
                .inputGuardrails(new PromptSafetyInputGuardrail())
                .build();
    }
}
