package org.jubensha.aijubenshabackend.ai.service.agent;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Player Agent接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 17:35
 * @since 2026
 */
@SystemMessage(fromResource = "prompt/player-system-prompt.txt")
public interface PlayerAgent {
    @UserMessage("发言内容：{{message}}")
    String speak(String message);

    @UserMessage("线索信息：{{clueInfo}}")
    String respondToClue(String clueInfo);

    @UserMessage("讨论话题：{{topic}}")
    String discuss(String topic);

    @UserMessage("投票对象：{{suspect}}")
    String vote(String suspect);

    @UserMessage("目标玩家ID：{{targetPlayerId}}\n消息内容：{{message}}")
    String privateChat(String targetPlayerId, String message);

    @UserMessage("问题：{{question}}")
    String answerQuestion(String question);
}
