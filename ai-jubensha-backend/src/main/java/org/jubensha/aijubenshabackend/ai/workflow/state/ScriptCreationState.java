package org.jubensha.aijubenshabackend.ai.workflow.state;


import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * 剧本创建子图的流转状态对象
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-11 14:40
 * @since 2026
 */

@Data
public class ScriptCreationState implements Serializable {
    private static final long serialVersionUID = 1L;
    // 1. 用户输入
    private String userPrompt;

    // 2. 第一阶段：上帝视角大纲 (JSON 字符串)
    // 包含：Title, Background, Truth(真相), CoreTrick(诡计), FullTimeline(总时间线), CharacterList(角色名+定位)
    private String outlineJson;

    // 3. 第二阶段：线索与场景 (JSON 字符串)
    // 包含：SceneList(场景), ClueList(线索 - 必须基于 Truth 生成)
    private String mechanicsJson;

    // 4. 第三阶段：角色剧本 (Map<角色名, 剧本内容>)
    // 这里并行生成，每个角色的内容包含：Bio(生平), Relationship, PersonalTimeline, Secret
    private Map<String, String> characterScripts;

    // 5. 最终产物
    private Long finalScriptId;
}
