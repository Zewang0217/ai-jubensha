package org.jubensha.aijubenshabackend.ai.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlayerAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerAgent.class);
    
    private final ChatLanguageModel chatModel;
    private final PlayerService playerService;
    private String characterName;
    private String characterBackground;
    private String characterSecret;
    
    @Autowired
    public PlayerAgent(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.playerService = AiServices.create(PlayerService.class, chatModel);
    }
    
    /**
     * 设置角色信息
     */
    public void setCharacterInfo(String name, String background, String secret) {
        this.characterName = name;
        this.characterBackground = background;
        this.characterSecret = secret;
        logger.info("Set character info for PlayerAgent: {}", name);
    }
    
    @Override
    public String process(String input) {
        logger.info("PlayerAgent processing input: {}", input);
        try {
            String response = playerService.playCharacter(characterName, characterBackground, characterSecret, input);
            logger.info("PlayerAgent response: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error processing input in PlayerAgent: {}", e.getMessage());
            return "我遇到了一些问题，请稍后再试。";
        }
    }
    
    @Override
    public String getName() {
        return characterName != null ? characterName : "Player";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.PLAYER;
    }
    
    @Override
    public void reset() {
        logger.info("Resetting PlayerAgent");
        characterName = null;
        characterBackground = null;
        characterSecret = null;
    }
    
    interface PlayerService {
        @UserMessage("你是剧本杀游戏中的角色 {characterName}。\n" +
                "你的背景故事：\n{characterBackground}\n" +
                "你的秘密：\n{characterSecret}\n" +
                "\n" +
                "请你以 {characterName} 的身份参与游戏，表现出角色的性格特点和行为方式。\n" +
                "记住：\n" +
                "1. 你只知道自己的背景故事和秘密，不知道其他角色的信息\n" +
                "2. 你需要保护自己的秘密，不要轻易透露\n" +
                "3. 你可以根据游戏进展调整自己的策略\n" +
                "4. 你的回答应该符合角色的身份和性格\n" +
                "\n" +
                "当前游戏情况：\n" +
                "{input}")
        String playCharacter(String characterName, String characterBackground, String characterSecret, String input);
    }
}
