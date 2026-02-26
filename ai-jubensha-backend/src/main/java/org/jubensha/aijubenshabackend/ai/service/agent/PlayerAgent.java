package org.jubensha.aijubenshabackend.ai.service.agent;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

/**
 * Player Agent接口
 * 采用工具驱动的推理方案，AI通过调用工具获取所需信息
 *
 * @author Zewang
 * @version 2.0
 * @date 2026-02-09
 * @since 2026
 */
@SystemMessage("""
你是一个剧本杀游戏中的AI玩家。请遵循以下原则：

1. 工具使用优先：在进行推理和决策前，务必通过调用工具获取相关信息
2. 信息获取顺序：
   - 获取讨论历史了解当前情况
   - 获取角色线索和时间线掌握背景
   - 获取其他玩家状态了解动态
3. 推理基于事实：所有推理必须基于通过工具获取的信息
4. 角色一致性：保持与角色设定一致的言行
5. 工具调用格式：使用JSON格式调用工具，包含必要参数

可用工具：
- getDiscussionHistory：获取讨论历史
- getClue：获取角色线索
- getTimeline：获取角色时间线
- getSecret：获取角色秘密
- getPlayerStatus：获取玩家状态
- sendDiscussionMessage：发送讨论消息
- sendPrivateChatRequest：发送单聊请求

示例工具调用：
{
  "toolcall": {
    "thought": "需要了解最近的讨论情况",
    "name": "getDiscussionHistory",
    "params": {
      "gameId": "1",
      "limit": 20
    }
  }
}
""")
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
    String analyzeTopic(@V("gameId") String gameId, @V("playerId") String playerId, @V("topic") String topic);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n请通过调用工具分析当前讨论情况和其他玩家状态，决定是否需要发起单聊，并选择合适的目标玩家。")
    String decidePrivateChat(String gameId, String playerId);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n角色ID：{{characterId}}\n角色名称：{{characterName}}\n剧本ID：{{scriptId}}\n\n【角色信息】\n背景故事：{{backgroundStory}}\n角色秘密：{{secret}}\n角色时间线：{{timeline}}\n\n请作为{{characterName}}角色，基于以上角色信息，通过调用工具获取以下信息：\n1. 讨论历史，了解当前游戏进展\n2. 你的角色线索，掌握关键信息（使用剧本ID：{{scriptId}}）\n3. 其他玩家状态，掌握全局情况\n\n请以自然、流畅的语言风格，像真人一样进行陈述，内容包括：\n- 你的自我介绍和背景\n- 案发时间前后你的行动\n- 你发现的重要线索\n- 你对案件的分析\n- 你对其他玩家的观察\n\n请保持{{characterName}}的性格特点，语言自然真实，逻辑清晰，信息准确。确保你的陈述基于角色设定和通过工具获取的真实信息，而不是虚构内容。请直接开始你的陈述，不需要任何开场白或引言。每一次都请返回完整的,没有任何开场白的陈述")
    String generateStatement(@V("gameId") String gameId,
                           @V("playerId") String playerId,
                           @V("characterId") String characterId,
                           @V("characterName") String characterName,
                           @V("scriptId") String scriptId,
                           @V("backgroundStory") String backgroundStory,
                           @V("secret") String secret,
                           @V("timeline") String timeline);

    @UserMessage("""
        游戏ID：{{gameId}}
        玩家ID：{{playerId}}
        
        你的内心思考：{{reasoningResult}}
        你的秘密：{{secret}}
        你的背景：{{backgroundStory}}
        你的时间线：{{timeline}}
        
        请作为{{characterName}}角色，基于以上信息，通过调用工具获取最新的游戏动态，然后生成自然、真实的发言。请直接开始发言，不需要任何开场白，保持语言流畅自然，符合角色性格特点。
        备注:如果在发言历史中看见同样角色的发言,请记住,那就是你的发言.
        """)
    String speakWithReasoning(
        @V("gameId") String gameId,
        @V("playerId") String playerId,
        @V("reasoningResult") String reasoningResult,
        @V("characterName") String characterName,
        @V("secret") String secret,
        @V("timeline") String timeline,
        @V("backgroundStory") String backgroundStory
    );

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n角色ID：{{characterId}}\n角色名称：{{characterName}}\n\n【剧本内容】\n{{scriptContent}}\n\n请作为{{characterName}}角色，仔细阅读上述剧本内容，包括你的背景故事、秘密和时间线。\n这些信息将作为你在游戏中的基础设定，请牢记并在后续的讨论中保持角色一致性。\n\n阅读完成后，请确认你已了解所有信息，并准备开始游戏。")
    String readScript(
        @V("gameId") String gameId,
        @V("playerId") String playerId,
        @V("characterId") String characterId,
        @V("characterName") String characterName,
        @V("scriptContent") String scriptContent
    );

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n讨论话题：{{topic}}\n\n【角色信息】\n角色名称：{{characterName}}\n背景故事：{{backgroundStory}}\n角色秘密：{{secret}}\n角色时间线：{{timeline}}\n\n请通过调用工具获取相关信息，然后针对该话题生成详细的讨论内容。\n讨论内容要符合你作为{{characterName}}的角色设定，基于你的背景故事、秘密和时间线。备注:如果在发言历史中看见同样角色的发言,请记住,那就是你的发言.")
    String discussWithCharacterInfo(
        @V("gameId") String gameId,
        @V("playerId") String playerId,
        @V("topic") String topic,
        @V("characterName") String characterName,
        @V("backgroundStory") String backgroundStory,
        @V("secret") String secret,
        @V("timeline") String timeline
    );

    @UserMessage("""
游戏ID：{{gameId}}
玩家ID：{{playerId}}
角色名称：{{characterName}}
线索ID列表: {{sceneIds}}
最大搜证次数：{{maxChances}}

请作为{{characterName}}角色，分析当前游戏状态，决定在哪些场景进行搜证，并生成搜证计划。

请严格返回以下JSON格式的搜证请求：
{
  "investigationRequests": [
    {
      "clueId": "线索ID"
    },
    ...
  ]
}

其中：
- investigationRequests：搜证请求列表，长度不超过maxChances
- clueId：线索ID，必须是提供的线索选项列表中对应线索的ID部分

请基于你的角色背景、秘密和时间线，以及当前游戏进展，做出合理的搜证决策，选择最可能包含关键线索的选项。
""")
    String investigate(@V("gameId") String gameId, @V("playerId") String playerId, @V("characterName") String characterName, @V("sceneIds") List<String> sceneIds, @V("maxChances") int maxChances);

    @UserMessage("""
游戏ID：{{gameId}}
玩家ID：{{playerId}}
角色名称：{{characterName}}
线索ID：{{clueId}}
线索内容：{{clueContent}}

请作为角色{{characterName}}，详细分析该线索的内容，然后决定是否将其公开。

分析线索时请考虑以下方面：
1. 线索内容分析：线索具体描述了什么，包含哪些关键信息
2. 线索与案件的关联性：线索是否直接指向凶手，或对解决案件有关键作用
3. 线索与你的关系：线索是否涉及你的秘密、罪行或隐藏的身份
4. 线索与其他玩家的关系：线索是否指向其他玩家，或可能影响其他玩家的嫌疑
5. 公开线索的利弊：公开后对你、对案件进展、对其他玩家的影响
6. 当前游戏情境：通过工具获取当前讨论情况和游戏阶段，评估是否需要公开线索

决策逻辑：
- 如果线索对案件非常重要，且不涉及你的秘密，应选择公开
- 如果线索涉及你的秘密，但对案件至关重要，可以考虑有策略地公开，同时准备好合理解释
- 如果线索会严重暴露你的秘密，且对案件不是特别关键，应选择不公开
- 如果线索对案件不重要，无论是否涉及你的秘密，都可以选择不公开
- 考虑当前游戏阶段和讨论情况，确保决策符合游戏进展需要
- 为了更好的完成推理与合作,如果该线索不影响玩家,建议进行公开

请先通过调用工具获取必要的信息（如讨论历史、游戏阶段等），然后基于完整信息做出决策。

最终决策结果必须严格以JSON格式返回，包含以下字段：
{
  "decision": "公开"或"不公开",
  "reason": "简要说明决策理由",
  "analysis": "对线索内容的详细分析"
}

请确保返回的JSON格式正确，decision字段只能是"公开"或"不公开"。
""")
    String decideToReveal(@V("gameId") String gameId, @V("playerId") String playerId, @V("characterName") String characterName, @V("clueId") String clueId, @V("clueContent") String clueContent);

    @UserMessage("""
游戏ID：{{gameId}}
玩家ID：{{playerId}}
角色名称：{{characterName}}

请作为{{characterName}}角色，通过调用工具获取以下信息：
1. 讨论历史，了解当前游戏进展和其他玩家的观点
2. 你的角色线索，掌握关键信息
3. 其他玩家状态，了解全局情况

基于以上信息，分析整个案件，包括：
- 凶手身份
- 作案动机
- 作案手法
- 关键线索分析
- 对其他玩家的怀疑理由

请生成一个全面、详细的案件答案，确保答案基于通过工具获取的真实信息，而不是虚构内容。

请直接开始你的答案，不需要任何开场白或引言。
""")
    String answer(@V("gameId") String gameId, @V("playerId") String playerId, @V("characterName") String characterName);
}