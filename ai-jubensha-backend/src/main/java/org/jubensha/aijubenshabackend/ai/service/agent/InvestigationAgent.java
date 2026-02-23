package org.jubensha.aijubenshabackend.ai.service.agent;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 搜证 Agent 接口
 * 用于处理 AI 玩家的搜证逻辑
 *
 * @author zewang
 * @date 2026-02-22
 */
@SystemMessage("你是一个剧本杀游戏中的AI玩家，现在处于搜证阶段。请遵循以下原则：\n\n1. 工具使用优先：在进行推理和决策前，务必通过调用工具获取相关信息\n2. 信息获取顺序：\n   - 获取场景列表和可搜证的线索\n   - 获取角色背景信息和秘密\n   - 获取角色时间线\n   - 获取现有线索和对话历史\n3. 推理基于事实：所有推理必须基于通过工具获取的信息\n4. 角色一致性：保持与角色设定一致的言行\n5. 搜证策略：根据角色背景和秘密，选择最有可能获得对自己有利信息的场景\n6. 公开策略：根据线索的重要性和对自己的影响，决定是否公开线索\n\n可用工具：\n- getScenes：获取可搜证的场景列表\n- getCluesByScene：获取场景中的线索\n- getSecret：获取角色秘密\n- getTimeline：获取角色时间线\n- getDiscussionHistory：获取讨论历史\n- investigate：执行搜证操作\n- setClueVisibility：设置线索可见性\n\n示例工具调用：\n{\n  \"toolcall\": {\n    \"thought\": \"需要了解可搜证的场景\",\n    \"name\": \"getScenes\",\n    \"params\": {\n      \"gameId\": \"1\"\n    }\n  }\n}")
public interface InvestigationAgent {

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n角色名称：{{characterName}}\n可搜证场景ID列表：{{sceneIds}}\n最大搜证次数：{{maxChances}}\n\n请分析当前游戏状态，通过调用工具获取必要信息，然后决定要对哪些场景进行搜证。\n\n决策过程：\n1. 首先获取可搜证的场景列表和每个场景的线索\n2. 获取角色的背景信息、秘密和时间线\n3. 分析哪些场景最有可能包含对自己有利的信息\n4. 制定搜证策略，选择要搜证的场景和线索\n5. 执行搜证操作，获得线索信息\n6. 决定是否公开获得的线索\n7. 更新线索的可见性\n\n请生成详细的搜证计划和执行结果。")
    String investigate(String gameId, String playerId, String characterName, String sceneIds, String maxChances);

    @UserMessage("游戏ID：{{gameId}}\n玩家ID：{{playerId}}\n线索ID：{{clueId}}\n线索内容：{{clueContent}}\n\n请分析这个线索的重要性和对自己的影响，然后决定是否公开这个线索。\n\n考虑因素：\n1. 线索是否与角色的秘密相关\n2. 线索是否会暴露角色的罪行\n3. 线索是否对推理案情有帮助\n4. 公开线索是否会对角色有利\n\n请生成决策结果和理由。")
    String decideToReveal(String gameId, String playerId, String clueId, String clueContent);
}
