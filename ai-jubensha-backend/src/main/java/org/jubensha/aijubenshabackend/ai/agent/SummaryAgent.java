package org.jubensha.aijubenshabackend.ai.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SummaryAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(SummaryAgent.class);
    
    private final ChatLanguageModel chatModel;
    private final SummaryService summaryService;
    
    @Autowired
    public SummaryAgent(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.summaryService = AiServices.create(SummaryService.class, chatModel);
    }
    
    @Override
    public String process(String input) {
        logger.info("SummaryAgent processing input: {}", input);
        try {
            String response = summaryService.summarizeConversation(input);
            logger.info("SummaryAgent response: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error processing input in SummaryAgent: {}", e.getMessage());
            return "我遇到了一些问题，请稍后再试。";
        }
    }
    
    /**
     * 生成当前战况摘要
     */
    public String generateBattleSummary(String conversationHistory) {
        logger.info("Generating battle summary");
        try {
            return summaryService.generateBattleSummary(conversationHistory);
        } catch (Exception e) {
            logger.error("Error generating battle summary: {}", e.getMessage());
            return "生成摘要时遇到问题。";
        }
    }
    
    @Override
    public String getName() {
        return "Summary";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.SUMMARY;
    }
    
    @Override
    public void reset() {
        logger.info("Resetting SummaryAgent");
        // 重置逻辑
    }
    
    interface SummaryService {
        @UserMessage("你是一个专业的对话摘要专家，负责总结剧本杀游戏中的对话内容。\n" +
                "请你对以下对话进行简要总结，突出关键信息和重要线索：\n" +
                "\n" +
                "{input}")
        String summarizeConversation(String input);
        
        @UserMessage("你是一个专业的剧本杀战况分析师，负责生成当前游戏的战况摘要。\n" +
                "请你基于以下对话历史，生成一份简洁的当前战况摘要，包括：\n" +
                "1. 已发现的关键线索\n" +
                "2. 玩家的主要怀疑对象\n" +
                "3. 当前的讨论焦点\n" +
                "4. 游戏的进展情况\n" +
                "\n" +
                "对话历史：\n" +
                "{conversationHistory}")
        String generateBattleSummary(String conversationHistory);
    }
}
