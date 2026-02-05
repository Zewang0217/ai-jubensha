package org.jubensha.aijubenshabackend.ai.service.agent;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Judge Agent接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 17:40
 * @since 2026
 */
@SystemMessage(fromResource = "prompt/judge-system-prompt.txt")
public interface JudgeAgent {
    @UserMessage("消息内容：{{message}}")
    boolean validateMessage(String message);

    @UserMessage("行为内容：{{action}}\n玩家ID：{{playerId}}")
    boolean validateAction(String action, String playerId);

    @UserMessage("游戏状态：{{gameState}}")
    String generateSummary(String gameState);

    @UserMessage("讨论内容：{{discussionContent}}")
    boolean monitorDiscussion(String discussionContent);

    @UserMessage("讨论内容：{{discussionContent}}")
    String summarizeDiscussion(String discussionContent);
}
