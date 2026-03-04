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

    @UserMessage("""
玩家答案：{{answers}}

请基于玩家的答题内容和游戏表现，为每个玩家生成评分和评论。

评分标准：
- 凶手身份判断准确性：0-40分
- 作案动机分析：0-20分
- 作案手法分析：0-20分
- 关键线索分析：0-20分

请严格返回以下JSON格式的评分结果：
{
  "scores": [
    {
      "playerId": "玩家ID",
      "score": 总分,
      "breakdown": {
        "motive": 动机分,
        "method": 手法分,
        "clues": 线索分,
        "accuracy": 准确性分
      },
      "comment": "对该玩家的评论，包括答题质量和游戏表现"
    },
    ...
  ],
  "summary": "整体游戏表现总结",
  "ending": "百字左右的小说风格结局叙述，详细描述案发当天的真实情况"
}

请确保JSON格式正确，评分合理，评论内容符合玩家表现，结局叙述采用小说叙事风格，控制在百字左右。
""")
    String scoreAnswers(List<Map<String, Object>> answers);
} 
