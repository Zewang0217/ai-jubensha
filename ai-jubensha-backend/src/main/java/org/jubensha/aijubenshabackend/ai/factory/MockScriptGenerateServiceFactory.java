package org.jubensha.aijubenshabackend.ai.factory;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock 剧本生成服务工厂
 * 在 mock-ai profile 下替代真实的 ScriptGenerateServiceFactory
 * 用于测试 WebSocket 通信的前后端交互，避免大模型 token 消耗
 *
 * @author zewang
 * @date 2026-03-04
 */
@Component
@Slf4j
@Profile("mock-ai")
public class MockScriptGenerateServiceFactory {

    private final ScriptGenerateService mockScriptGenerateService;

    public MockScriptGenerateServiceFactory(ScriptGenerateService mockScriptGenerateService) {
        this.mockScriptGenerateService = mockScriptGenerateService;
        log.info("[Mock AI] MockScriptGenerateServiceFactory 已初始化");
    }

    /**
     * 根据 scriptId 获取服务（返回 Mock 服务）
     */
    public ScriptGenerateService getService(Long scriptId) {
        log.info("[Mock AI] 获取 Mock 剧本生成服务，剧本ID: {}", scriptId);
        return mockScriptGenerateService;
    }

    /**
     * 获取流式服务（返回 Mock 服务）
     */
    public ScriptGenerateService getStreamingService(Long scriptId) {
        log.info("[Mock AI] 获取 Mock 流式剧本生成服务，剧本ID: {}", scriptId);
        return mockScriptGenerateService;
    }

    /**
     * 获取非流式服务（返回 Mock 服务）
     */
    public ScriptGenerateService getNonStreamingService(Long scriptId) {
        log.info("[Mock AI] 获取 Mock 非流式剧本生成服务，剧本ID: {}", scriptId);
        return mockScriptGenerateService;
    }
}
