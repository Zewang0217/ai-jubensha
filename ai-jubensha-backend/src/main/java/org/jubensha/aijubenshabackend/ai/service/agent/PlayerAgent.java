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

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n当前讨论阶段：{{phase}}\n讨论历史：{{discussionHistory}}\n请基于上述信息，通过调用相关工具收集更多信息，然后生成下一步的讨论内容。")
    String reasonAndDiscuss(String gameId, String playerId, String phase, String discussionHistory);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n讨论话题：{{topic}}\n请通过调用工具获取相关信息，然后针对该话题生成详细的讨论内容。")
    String analyzeTopic(String gameId, String playerId, String topic);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n请分析当前讨论情况，决定是否需要发起单聊，并选择合适的目标玩家。")
    String decidePrivateChat(String gameId, String playerId);
}
