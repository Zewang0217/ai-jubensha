package org.jubensha.aijubenshabackend.ai.service;


import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

/**
 * 生成剧本的接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-30 20:55
 * @since 2026
 */

public interface ScriptGenerateService {

    /**
     * 生成剧本
     */
    @SystemMessage(fromResource = "prompt/script-generate-system-prompt.txt")
    String generateScript(String userMessage);

    /**
     * 流式生成剧本
     */
    @SystemMessage(fromResource = "prompt/script-generate-system-prompt.txt")
    Flux<String> generateScriptStream(String userMessage);


}
