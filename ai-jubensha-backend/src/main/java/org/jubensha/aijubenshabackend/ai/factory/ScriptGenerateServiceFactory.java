package org.jubensha.aijubenshabackend.ai.factory;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.guardrail.PromptSafetyInputGuardrail;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.ai.tools.ToolManager;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.springframework.context.annotation.Configuration;

/**
 * 剧本生成服务类的工厂
 *
 * @author Zewang
 * @date 2026-01-30 21:22
 * @version 1.0
 * @since 2026
 */

@Configuration
@Slf4j
public class ScriptGenerateServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

//    @Resource
//    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ToolManager toolManager;



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

    /**
     * 根据 scriptId 获取服务
     */
    public ScriptGenerateService getService(Long scriptId) {
        String cacheKey = "scriptId:" + scriptId;
        return serviceCache.get(cacheKey, key -> createScriptGenerateService(scriptId));
    }

    /**
     * 创建新的剧本生成服务实例
     */
    private ScriptGenerateService createScriptGenerateService(Long scriptId) {
        log.info("创建新的剧本生成服务实例, 剧本ID：{}", scriptId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .id(scriptId)
//            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(10)
            .build();

        return AiServices.builder(ScriptGenerateService.class)
            .chatModel(chatModel)
            // TODO: 流式响应
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
