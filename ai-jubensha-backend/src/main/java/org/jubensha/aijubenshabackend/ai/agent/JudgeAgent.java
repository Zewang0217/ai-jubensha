package org.jubensha.aijubenshabackend.ai.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JudgeAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(JudgeAgent.class);
    
    private final ChatLanguageModel chatModel;
    private final JudgeService judgeService;
    
    @Autowired
    public JudgeAgent(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.judgeService = AiServices.create(JudgeService.class, chatModel);
    }
    
    @Override
    public String process(String input) {
        logger.info("JudgeAgent processing input: {}", input);
        try {
            String response = judgeService.judgeAction(input);
            logger.info("JudgeAgent response: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error processing input in JudgeAgent: {}", e.getMessage());
            return "我遇到了一些问题，请稍后再试。";
        }
    }
    
    /**
     * 验证AI玩家的推理是否基于已发现的线索
     */
    public boolean validateReasoning(String playerInput, String discoveredClues) {
        logger.info("Validating player reasoning: {}", playerInput);
        try {
            String response = judgeService.validateReasoning(playerInput, discoveredClues);
            return response.toLowerCase().contains("valid");
        } catch (Exception e) {
            logger.error("Error validating reasoning: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查玩家行为是否符合角色设定
     */
    public boolean validateBehavior(String playerAction, String characterBackground) {
        logger.info("Validating player behavior: {}", playerAction);
        try {
            String response = judgeService.validateBehavior(playerAction, characterBackground);
            return response.toLowerCase().contains("valid");
        } catch (Exception e) {
            logger.error("Error validating behavior: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getName() {
        return "Judge";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.JUDGE;
    }
    
    @Override
    public void reset() {
        logger.info("Resetting JudgeAgent");
        // 重置逻辑
    }
    
    interface JudgeService {
        @UserMessage("你是剧本杀游戏的法官，负责判断玩家行为的合理性和推理的正确性。\n" +
                "你的职责包括：\n" +
                "1. 验证玩家的推理是否基于已发现的线索\n" +
                "2. 检查玩家的行为是否符合角色设定\n" +
                "3. 确保游戏规则的执行\n" +
                "4. 处理玩家的违规行为\n" +
                "\n" +
                "当前情况：\n" +
                "{input}")
        String judgeAction(String input);
        
        @UserMessage("请验证以下玩家推理是否基于已发现的线索：\n" +
                "玩家推理：{playerInput}\n" +
                "已发现线索：{discoveredClues}\n" +
                "\n" +
                "如果推理基于已发现的线索，请返回'VALID'，否则返回'INVALID'。")
        String validateReasoning(String playerInput, String discoveredClues);
        
        @UserMessage("请检查以下玩家行为是否符合角色设定：\n" +
                "玩家行为：{playerAction}\n" +
                "角色背景：{characterBackground}\n" +
                "\n" +
                "如果行为符合角色设定，请返回'VALID'，否则返回'INVALID'。")
        String validateBehavior(String playerAction, String characterBackground);
    }
}
