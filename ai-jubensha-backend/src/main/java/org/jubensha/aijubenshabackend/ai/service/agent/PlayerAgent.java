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

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n角色ID：{{characterId}}\n角色名称：{{characterName}}\n剧本ID：{{scriptId}}\n\n【角色信息】\n背景故事：{{backgroundStory}}\n角色秘密：{{secret}}\n角色时间线：{{timeline}}\n\n请作为{{characterName}}角色，基于以上角色信息，通过调用工具获取以下信息：\n1. 讨论历史，了解当前游戏进展\n2. 你的角色线索，掌握关键信息（使用剧本ID：{{scriptId}}）\n3. 其他玩家状态，掌握全局情况\n\n基于获取的信息和角色设定，生成一个结构化的陈述，包括：\n- 自我介绍和角色背景\n- 案发时间前后的行动轨迹\n- 你发现的关键线索\n- 你对案件的初步分析\n- 你对其他玩家的观察\n\n陈述要符合{{characterName}}的性格特点，逻辑清晰，信息准确。请确保你的陈述基于角色设定和通过工具获取的真实信息，而不是虚构内容。")
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
               \s
                【你的内心深层思考】
                {{reasoningResult}}
               \s
                [你的秘密]
                {{secret}}
               \s
                [你的背景信息]
                {{backgroundStory}}
               \s
                [你的时间线]
                {{timeline}}
                请基于以上内容，结合当前局势,通过调用工具来获取最新的线索和时间线以及对话内容，生成对外的发言或行动。
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

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n讨论话题：{{topic}}\n\n【角色信息】\n角色名称：{{characterName}}\n背景故事：{{backgroundStory}}\n角色秘密：{{secret}}\n角色时间线：{{timeline}}\n\n请通过调用工具获取相关信息，然后针对该话题生成详细的讨论内容。\n讨论内容要符合你作为{{characterName}}的角色设定，基于你的背景故事、秘密和时间线。")
    String discussWithCharacterInfo(
        @V("gameId") String gameId,
        @V("playerId") String playerId,
        @V("topic") String topic,
        @V("characterName") String characterName,
        @V("backgroundStory") String backgroundStory,
        @V("secret") String secret,
        @V("timeline") String timeline
    );

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n角色名称：{{characterName}}\n场景ID列表：{{sceneIds}}\n最大搜证次数：{{maxChances}}\n\n请作为{{characterName}}角色，分析当前游戏状态，决定在哪些场景进行搜证，并生成搜证计划。\n\n搜证计划应包括：\n1. 你选择的场景\n2. 每个场景的搜索重点\n3. 搜索顺序\n4. 预期可能发现的线索\n\n请基于你的角色背景、秘密和时间线，以及当前游戏进展，做出合理的搜证决策。")
    String investigate(@V("gameId") String gameId, @V("playerId") String playerId, @V("characterName") String characterName, @V("sceneIds") List<String> sceneIds, @V("maxChances") int maxChances);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n线索ID：{{clueId}}\n线索内容：{{clueContent}}\n\n请分析该线索的重要性和敏感性，决定是否将其公开。\n\n决策应考虑以下因素：\n1. 线索对案件的重要性：线索是否直接指向凶手，或对解决案件有关键作用\n2. 线索是否涉及你的秘密：线索是否会暴露你的罪行或隐藏的身份\n3. 线索对你的影响：公开后是否会对你造成不利影响，如被怀疑为凶手\n4. 线索的可替代性：是否有其他线索可以替代该线索的作用\n5. 当前游戏阶段：游戏处于哪个阶段，是否需要公开线索推进游戏\n\n决策逻辑：\n- 如果线索对案件非常重要，且不涉及你的秘密，应选择公开\n- 如果线索涉及你的秘密，但对案件至关重要，可以考虑有策略地公开，同时准备好解释\n- 如果线索会严重暴露你的秘密，且对案件不是特别关键，应选择不公开\n- 如果线索对案件不重要，无论是否涉及你的秘密，都可以选择不公开\n\n请以'公开'或'不公开'作为最终决策结果，并简要说明理由。")
    String decideToReveal(@V("gameId") String gameId, @V("playerId") String playerId, @V("clueId") String clueId, @V("clueContent") String clueContent);
}
