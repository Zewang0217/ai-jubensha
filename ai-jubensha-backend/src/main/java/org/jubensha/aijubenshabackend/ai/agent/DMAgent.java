package org.jubensha.aijubenshabackend.ai.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DMAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(DMAgent.class);
    
    private final ChatLanguageModel chatModel;
    private final DMService dmService;
    
    @Autowired
    public DMAgent(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.dmService = AiServices.create(DMService.class, chatModel);
    }
    
    @Override
    public String process(String input) {
        logger.info("DMAgent processing input: {}", input);
        try {
            String response = dmService.handleGameEvent(input);
            logger.info("DMAgent response: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error processing input in DMAgent: {}", e.getMessage());
            return "我遇到了一些问题，请稍后再试。";
        }
    }
    
    @Override
    public String getName() {
        return "DM";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.DM;
    }
    
    @Override
    public void reset() {
        logger.info("Resetting DMAgent");
        // 重置逻辑
    }
    
    interface DMService {
        @UserMessage("你是一个专业的剧本杀主持人（DM），负责引导游戏流程，控制游戏节奏，发放线索，维护游戏氛围。\n" +
                "你的职责包括：\n" +
                "1. 引导玩家完成游戏的各个阶段：开场介绍、搜证、讨论、投票、结局\n" +
                "2. 根据玩家的行动和提问，发放相应的线索\n" +
                "3. 维持游戏的紧张感和趣味性\n" +
                "4. 确保游戏按照预定的流程进行\n" +
                "5. 在适当的时候推动剧情发展\n" +
                "\n" +
                "当前游戏情况：\n" +
                "{input}")
        String handleGameEvent(String input);
    }
}
