package org.jubensha.aijubenshabackend.ai.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 生成剧本的agent
 *
 * @author zewan
 * @version 1.0
 * @date 2026-01-29 19:48
 * @since 2026
 */

@Component
@Slf4j
public class ScriptGeneratorAgent implements Agent{

    private final ChatLanguageModel chatLanguageModel;
    private final ScriptGeneratorService scriptGeneratorService;
    private String scriptName;
    private String description;
    private Integer playerCount;
    private DifficultyLevel difficulty;


    /**
     * 设置剧本信息
     */
    public void setScriptInfo(String scriptName,
        Integer playerCount, DifficultyLevel difficulty) {
        this.scriptName = scriptName;
//        this.description = description;
        this.playerCount = playerCount;
        this.difficulty = difficulty;
    }

    @Autowired
    public ScriptGeneratorAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
        this.scriptGeneratorService = AiServices.create(ScriptGeneratorService.class, chatLanguageModel);
    }

    /**
     * 处理输入消息并生成响应
     */
    @Override
    public String process(String input) {
        log.info("ScriptGeneratorAgent processing input: {}", input);
        try {
            String response = scriptGeneratorService.generateScript(
                scriptName, playerCount, difficulty.toString(), input);
            log.info("ScriptGeneratorAgent response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error processing input in ScriptGeneratorAgent: {}", e.getMessage());
            return "剧本生成失败，请稍后再试。";
        }
    }

    /**
     * 获取Agent的名称
     */
    @Override
    public String getName() {
        return "ScriptGenerator";
    }

    /**
     * 获取Agent的类型
     */
    @Override
    public AgentType getType() {
        return AgentType.SCRIPT_GENERATOR;
    }

    /**
     * 重置Agent状态
     */
    @Override
    public void reset() {
        log.info("Resetting ScriptGeneratorAgent");
        scriptName = null;
        description = null;
        playerCount = null;
        difficulty = null;
    }

    interface ScriptGeneratorService {
        @UserMessage("你是一个专业的剧本杀剧本作家，擅长创作各种类型的剧本杀剧本。\n" +
                "请根据以下要求生成一个完整的剧本杀剧本：\n" +
                "剧本名称：{scriptName}\n" +
                "剧本描述：{description}\n" +
                "玩家人数：{playerCount}\n" +
                "难度级别：{difficulty}\n" +
                "额外要求：{input}\n" +
                "\n" +
                "剧本应包含以下内容：\n" +
                "1. 剧本背景故事\n" +
                "2. 每个角色的详细背景、性格特点、秘密\n" +
                "3. 游戏流程（开场、搜证、讨论、投票、结局）\n" +
                "4. 线索系统（包括公共线索和个人线索）\n" +
                "5. 关键时间线\n" +
                "6. 结局设定（包括不同的可能结局）\n" +
                "\n" +
                "请确保剧本逻辑严谨，角色关系复杂，线索设计合理，具有足够的可玩性和推理空间。")
        String generateScript(@V("scriptName") String scriptName, @V("playerCount") Integer playerCount, @V("difficulty") String difficulty, @V("input") String input);
    }

}
