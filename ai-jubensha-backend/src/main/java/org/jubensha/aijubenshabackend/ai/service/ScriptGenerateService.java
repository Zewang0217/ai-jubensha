package org.jubensha.aijubenshabackend.ai.service;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import reactor.core.publisher.Flux;

/**
 * 生成剧本的接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-30 20:55
 * @since 2026
 */

public interface ScriptGenerateService {

    /**
     * 生成剧本
     */
    @SystemMessage(fromResource = "prompt/script-generate-system-prompt.txt")
    String generateScript(String userMessage);

    /**
     * 流式生成剧本
     */
    @SystemMessage(fromResource = "prompt/script-generate-system-prompt.txt")
    Flux<String> generateScriptStream(String userMessage);

//    ========== 新的剧本生成子图 ========

    // 1. 生成上帝视角大纲
    @SystemMessage("""
        你是一个剧本杀总编剧，请设计一个剧本大纲。
                要求：
                1. 设计一个核心诡计（凶手如何作案、如何伪造不在场证明）。
                2. 确定死者、凶手和嫌疑人列表（建议5-6人）。
                3. 梳理案发当天的上帝视角时间线。
               \s
                返回 JSON 格式：
                {
                    "title": "...",
                    "background": "...",
                    "truth": "详细的作案手法和真相",
                    "timeline": [{"time": "...", "event": "..."}],
                    "characters": [{"name": "...", "role": "死者的妻子", "archetype": "嫉妒多疑"}]
                }
        """)
    @UserMessage("主题为:{{topic}}")
    Flux<String> generateWorldOutline(String topic);

    /**
     * 设计搜证线索(基于大纲)
     * 
     * @param outlineJson 剧本大纲 JSON
     * @return 场景和线索的 JSON 流
     */
    @SystemMessage("""
        你是一个剧本杀编剧,熟知各种剧本杀,善于布置和完善剧本杀游戏所需要的场景和线索.
        基于以下大纲，设计游戏所需的场景和线索：
        
        【重要约束 - 必须严格遵守】
        线索的 location 字段必须**精确匹配** scenes 数组中某个场景的 name 值。
        不允许使用场景名称的变体、缩写或组合形式。
        
        【生成步骤】
        第一步：设计所有场景
        - 根据大纲中的角色和背景，设计5-8个场景
        - 场景名称要简洁明确，如"大厅"、"书房"、"李明卧室"
        - 每个场景名称必须唯一
        
        第二步：设计线索
        - 每个场景放置3-5个线索
        - 线索的 location 必须从第一步设计的场景名称中选择
        - 线索要指向凶手，但要有干扰项
        
        【线索要求】
        1. 包含尸体线索、现场线索和每个角色的房间线索
        2. 线索内容要有推理价值
        3. location 字段只能是场景名称，不能是描述性文字
        
        【返回 JSON 格式】
        {
            "scenes": [
                {"name": "场景名称（简洁明确，如：大厅）", "desc": "场景描述"}
            ],
            "clues": [
                {"name": "线索名称", "content": "线索内容", "location": "必须精确匹配 scenes 中某个 name 值"}
            ]
        }
        
        【示例】
        如果 scenes 包含：[{"name": "大厅"}, {"name": "书房"}, {"name": "李明卧室"}]
        那么线索的 location 只能是："大厅"、"书房" 或 "李明卧室"
        不能是："大厅角落"、"李明的卧室"、"书房/卧室" 等变体形式
        """
    )
    @UserMessage("大纲为：{{outlineJson}}")
    Flux<String> generateMechanics(String outlineJson);

    // 3. 生成单个角色的剧本
    @SystemMessage("""
        你现在是一个情感细腻的,善于写剧本的作家.请你带入到剧本的角色中,写下每一个角色的剧本。
               
                请**以第一人称**写下你的回忆录。不要写成说明书，要像小说一样充满情感和细节。主要内容根据剧本大纲来编写,但是注意一第一人称书写,不写不属于该角色能够看到的东西.最好有详细的细节描写和心理描写,叙事要生动.
               
                你需要包含：
                1. **我的前半生**：你的童年、成长经历以及与死者的恩怨纠葛。
                2. **案发当天**：你今天都做了什么？见到了谁？（注意：如果你是凶手，请在回忆录中隐瞒作案细节，或者进行模糊化处理，不要直接承认）。
                3. **对他人的看法**：你眼中的其他角色是怎样的。
                4. **我的秘密**：只有你自己知道的秘密。
               
                返回 JSON：
                {
                    "bio_story": "长文本，第一人称回忆录...",
                    "personal_timeline": "...",
                    "secrets": "..."
                }
    """)
    @UserMessage("{{userMessage}}")
    Flux<String> generateCharacterMemoir(@V("userMessage") String userMessage);
}
