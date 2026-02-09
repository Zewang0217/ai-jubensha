package org.jubensha.aijubenshabackend.ai.service.agent;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Player Agent接口
 * 采用工具驱动的推理方案，AI通过调用工具获取所需信息
 *
 * @author Zewang
 * @version 2.0
 * @date 2026-02-09
 * @since 2026
 */
@SystemMessage("你是一个剧本杀游戏中的AI玩家。请遵循以下原则：\n\n1. 工具使用优先：在进行推理和决策前，务必通过调用工具获取相关信息\n2. 信息获取顺序：\n   - 获取讨论历史了解当前情况\n   - 获取角色线索和时间线掌握背景\n   - 获取其他玩家状态了解动态\n3. 推理基于事实：所有推理必须基于通过工具获取的信息\n4. 角色一致性：保持与角色设定一致的言行\n5. 工具调用格式：使用JSON格式调用工具，包含必要参数\n\n可用工具：\n- getDiscussionHistory：获取讨论历史\n- getClue：获取角色线索\n- getTimeline：获取角色时间线\n- getSecret：获取角色秘密\n- getPlayerStatus：获取玩家状态\n- sendDiscussionMessage：发送讨论消息\n- sendPrivateChatRequest：发送单聊请求\n\n示例工具调用：\n{\n  \"toolcall\": {\n    \"thought\": \"需要了解最近的讨论情况\",\n    \"name\": \"getDiscussionHistory\",\n    \"params\": {\n      \"gameId\": \"1\",\n      \"limit\": 20\n    }\n  }\n}")
public interface PlayerAgent {
    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n请分析当前游戏状态，通过调用工具获取必要信息，然后生成下一步的发言内容。")
    String speak(String gameId, String playerId);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n请分析当前讨论情况，通过调用工具获取相关线索，然后对该线索做出合理回应。")
    String respondToClue(String gameId, String playerId);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n讨论话题：{{topic}}\n请通过调用工具获取相关信息，然后针对该话题生成详细的讨论内容。")
    String discuss(String gameId, String playerId, String topic);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n请通过调用工具分析所有玩家的表现和线索，然后做出投票决定。")
    String vote(String gameId, String playerId);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n目标玩家ID：{{targetPlayerId}}\n请通过调用工具了解目标玩家的情况，然后生成合适的单聊消息。")
    String privateChat(String gameId, String playerId, String targetPlayerId);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n问题：{{question}}\n请通过调用工具获取相关信息，然后回答这个问题。")
    String answerQuestion(String gameId, String playerId, String question);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n当前讨论阶段：{{phase}}\n请通过调用工具收集讨论历史、线索和其他相关信息，然后生成下一步的讨论内容。")
    String reasonAndDiscuss(String gameId, String playerId, String phase);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n讨论话题：{{topic}}\n请通过调用工具获取相关信息，然后针对该话题生成详细的讨论内容。")
    String analyzeTopic(String gameId, String playerId, String topic);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n请通过调用工具分析当前讨论情况和其他玩家状态，决定是否需要发起单聊，并选择合适的目标玩家。")
    String decidePrivateChat(String gameId, String playerId);
}
