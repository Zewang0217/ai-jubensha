package org.jubensha.aijubenshabackend.ai.service;

import lombok.Getter;
import org.jubensha.aijubenshabackend.ai.agent.Agent;
import org.jubensha.aijubenshabackend.ai.agent.AgentType;
import org.jubensha.aijubenshabackend.ai.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.ai.agent.SummaryAgent;
import org.jubensha.aijubenshabackend.ai.agent.ScriptGeneratorAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    private final DMAgent dmAgent;
    private final PlayerAgent playerAgent;
    private final JudgeAgent judgeAgent;
    private final SummaryAgent summaryAgent;
    private final ScriptGeneratorAgent scriptGeneratorAgent;
    
    // 存储游戏中的AI玩家实例
    private final Map<Long, Map<String, Agent>> gameAgents = new ConcurrentHashMap<>();
    
    @Autowired
    public AIService(DMAgent dmAgent, PlayerAgent playerAgent, JudgeAgent judgeAgent, SummaryAgent summaryAgent, ScriptGeneratorAgent scriptGeneratorAgent) {
        this.dmAgent = dmAgent;
        this.playerAgent = playerAgent;
        this.judgeAgent = judgeAgent;
        this.summaryAgent = summaryAgent;
        this.scriptGeneratorAgent = scriptGeneratorAgent;
    }
    
    /**
     * 获取DM Agent
     */
    public Agent getDMAgent() {
        return dmAgent;
    }

    /**
     * 获取剧本生成Agent
     */
    public Agent getScriptGeneratorAgent() {
        return scriptGeneratorAgent;
    }

    /**
     * 为游戏创建AI玩家
     */
    public Agent createPlayerAgent(Long gameId, String characterName, String background, String secret) {
        logger.info("Creating PlayerAgent for game {}: {}", gameId, characterName);
        
        try {
            // 通过反射获取chatModel
            java.lang.reflect.Field chatModelField = playerAgent.getClass().getDeclaredField("chatModel");
            chatModelField.setAccessible(true);
            Object chatModel = chatModelField.get(playerAgent);
            
            // 创建新的PlayerAgent实例
            PlayerAgent newPlayerAgent = new PlayerAgent((dev.langchain4j.model.chat.ChatLanguageModel) chatModel);
            newPlayerAgent.setCharacterInfo(characterName, background, secret);
            
            // 存储到游戏Agent映射中
            gameAgents.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>())
                     .put(characterName, newPlayerAgent);
            
            return newPlayerAgent;
        } catch (Exception e) {
            logger.error("Error creating PlayerAgent: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取游戏中的AI玩家
     */
    public Agent getPlayerAgent(Long gameId, String characterName) {
        Map<String, Agent> agents = gameAgents.get(gameId);
        if (agents != null) {
            return agents.get(characterName);
        }
        return null;
    }
    
    /**
     * 处理AI相关的请求
     */
    public String processAIRequest(AgentType agentType, String input) {
        switch (agentType) {
            case DM
                -> dmAgent.process(input);
            case PLAYER ->
                playerAgent.process(input);
            case JUDGE
                -> judgeAgent.process(input);
            case SUMMARY
                -> summaryAgent.process(input);
            case SCRIPT_GENERATOR
                -> scriptGeneratorAgent.process(input);
            default
                -> {
                logger.warn("Unknown agent type: {}", agentType);
                return "未知的Agent类型。";
            }
        }
        return "请求处理完成。";
    }
    
    /**
     * 清理游戏的AI资源
     */
    public void cleanupGameAgents(Long gameId) {
        logger.info("Cleaning up AI agents for game {}", gameId);
        gameAgents.remove(gameId);
    }
}
