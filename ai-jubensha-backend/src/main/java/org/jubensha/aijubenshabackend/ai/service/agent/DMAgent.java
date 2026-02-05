package org.jubensha.aijubenshabackend.ai.service.agent;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import java.util.List;
import java.util.Map;

/**
 * DM Agent接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 17:30
 * @since 2026
 */
@SystemMessage(fromResource = "prompt/dm-system-prompt.txt")
public interface DMAgent {
    @UserMessage("游戏信息：{{gameInfo}}")
    String introduceGame(String gameInfo);

    @UserMessage("线索信息：{{clueInfo}}")
    String presentClue(String clueInfo);

    @UserMessage("阶段信息：{{phaseInfo}}")
    String advancePhase(String phaseInfo);

    @UserMessage("玩家消息：{{playerMessage}}\n玩家ID：{{playerId}}")
    String respondToPlayer(String playerMessage, String playerId);

    @UserMessage("讨论信息：{{discussionInfo}}")
    String startDiscussion(String discussionInfo);

    @UserMessage("讨论状态：{{discussionState}}")
    String moderateDiscussion(String discussionState);

    @UserMessage("玩家答案：{{answers}}")
    String scoreAnswers(List<Map<String, Object>> answers);
}
