package org.jubensha.aijubenshabackend.ai.workflow.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.workflow.state.ScriptCreationState;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.dto.ScriptResponseDTO;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.jubensha.aijubenshabackend.service.script.ScriptService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class AssemblyNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCRIPT_CREATION_STATE_KEY = "scriptCreationState";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行节点：组装");
            
            try {
                // 获取现有上下文
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context == null) {
                    throw new IllegalArgumentException("WorkflowContext 为空");
                }
                
                // 获取或创建 ScriptCreationState
                ScriptCreationState creationState = getOrCreateScriptCreationState(context);
                
                // 获取各节点生成的内容
                String outlineJson = creationState.getOutlineJson();
                Map<String, String> characterScripts = creationState.getCharacterScripts();
                String mechanicsJson = creationState.getMechanicsJson();
                
                // 验证所有内容是否存在
                if (outlineJson == null || outlineJson.isEmpty()) {
                    throw new IllegalArgumentException("大纲 JSON 不能为空");
                }
                
                if (characterScripts == null || characterScripts.isEmpty()) {
                    throw new IllegalArgumentException("角色剧本不能为空");
                }
                
                if (mechanicsJson == null || mechanicsJson.isEmpty()) {
                    throw new IllegalArgumentException("场景和线索 JSON 不能为空");
                }
                
                log.info("大纲 JSON 长度: {}", outlineJson.length());
                log.info("角色剧本数量: {}", characterScripts.size());
                log.info("场景和线索 JSON 长度: {}", mechanicsJson.length());
                
                // 解析大纲
                JsonNode outlineNode = objectMapper.readTree(outlineJson);
                String title = outlineNode.path("title").asText("未命名剧本");
                String background = outlineNode.path("background").asText("");
                String truth = outlineNode.path("truth").asText("");
                
                // 解析时间线
                StringBuilder timelineBuilder = new StringBuilder();
                JsonNode timelineNode = outlineNode.path("timeline");
                if (timelineNode.isArray()) {
                    for (JsonNode eventNode : timelineNode) {
                        String time = eventNode.path("time").asText();
                        String event = eventNode.path("event").asText();
                        timelineBuilder.append(time).append("：").append(event).append("\n");
                    }
                }
                
                // 创建剧本实体
                Script script = Script.builder()
                        .name(title)
                        .description(background)
                        .timeline(timelineBuilder.toString())
                        .author("AI Generated")
                        .difficulty(DifficultyLevel.MEDIUM)
                        .duration(120) // 默认2小时
                        .playerCount(characterScripts.size())
                        .coverImageUrl(new URL("https://picsum.photos/200/300").toString())
                        .build();
                
                // 保存剧本到数据库
                ScriptService scriptService = SpringContextUtil.getBean(ScriptService.class);
                ScriptResponseDTO savedScript = ScriptResponseDTO.fromEntity(
                        scriptService.createScript(script)
                );
                
                Long scriptId = savedScript.getId();
                log.info("剧本已保存到数据库，ID: {}", scriptId);
                
                // 保存角色信息
                saveCharacters(characterScripts, scriptId);
                
                // 保存场景和线索信息
                saveScenesAndClues(mechanicsJson, scriptId);
                
                // 保存最终剧本 ID 到状态
                creationState.setFinalScriptId(scriptId);
                context.getMetadata().put(SCRIPT_CREATION_STATE_KEY, creationState);
                context.setScriptId(scriptId);
                context.setScriptName(title);
                context.setSuccess(true);
                log.info("最终剧本 ID 已保存到状态: {}", scriptId);
                
            } catch (Exception e) {
                log.error("组装节点执行失败: {}", e.getMessage(), e);
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context != null) {
                    context.setErrorMessage("组装失败: " + e.getMessage());
                    context.setSuccess(false);
                }
            }
            
            return WorkflowContext.saveContext(WorkflowContext.getContext(state));
        });
    }
    
    /**
     * 获取或创建 ScriptCreationState
     */
    private static ScriptCreationState getOrCreateScriptCreationState(WorkflowContext context) {
        if (context.getMetadata() == null) {
            context.setMetadata(new java.util.HashMap<>());
        }
        
        ScriptCreationState creationState = (ScriptCreationState) context.getMetadata().get(SCRIPT_CREATION_STATE_KEY);
        if (creationState == null) {
            creationState = new ScriptCreationState();
            context.getMetadata().put(SCRIPT_CREATION_STATE_KEY, creationState);
        }
        
        return creationState;
    }
    
    /**
     * 保存角色信息
     */
    private static void saveCharacters(Map<String, String> characterScripts, Long scriptId) {
        try {
            CharacterService characterService = SpringContextUtil.getBean(CharacterService.class);
            List<Character> characters = new ArrayList<>();
            
            for (Map.Entry<String, String> entry : characterScripts.entrySet()) {
                String characterName = entry.getKey();
                String characterJson = entry.getValue();
                
                try {
                    JsonNode characterNode = objectMapper.readTree(characterJson);
                    String bioStory = characterNode.path("bio_story").asText("");
                    String personalTimeline = characterNode.path("personal_timeline").asText("");
                    String secrets = characterNode.path("secrets").asText("");
                    
                    Character character = new Character();
                    character.setScriptId(scriptId);
                    character.setName(characterName);
                    character.setDescription(bioStory.substring(0, Math.min(500, bioStory.length())));
                    character.setBackgroundStory(bioStory);
                    character.setSecret(secrets);
                    character.setTimeline(personalTimeline);
                    character.setCreateTime(LocalDateTime.now());
                    
                    characters.add(character);
                    log.debug("创建角色: {}", characterName);
                    
                } catch (Exception e) {
                    log.error("解析角色 {} 剧本失败: {}", characterName, e.getMessage(), e);
                    // 跳过该角色，继续处理其他角色
                }
            }
            
            // 保存所有角色
            for (Character character : characters) {
                characterService.createCharacter(character);
            }
            
            log.info("已保存 {} 个角色", characters.size());
            
        } catch (Exception e) {
            log.error("保存角色信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存角色信息失败", e);
        }
    }
    
    /**
     * 保存场景和线索信息
     */
    private static void saveScenesAndClues(String mechanicsJson, Long scriptId) {
        try {
            JsonNode rootNode = objectMapper.readTree(mechanicsJson);
            
            // 保存场景
            SceneService sceneService = SpringContextUtil.getBean(SceneService.class);
            List<Scene> scenes = new ArrayList<>();
            
            JsonNode scenesNode = rootNode.path("scenes");
            if (scenesNode.isArray()) {
                for (JsonNode sceneNode : scenesNode) {
                    Scene scene = new Scene();
                    scene.setScriptId(scriptId);
                    scene.setName(sceneNode.path("name").asText("未命名场景"));
                    scene.setDescription(sceneNode.path("desc").asText(""));
                    scene.setCreateTime(LocalDateTime.now());
                    
                    scenes.add(scene);
                    log.debug("创建场景: {}", scene.getName());
                }
            }
            
            for (Scene scene : scenes) {
                sceneService.createScene(scene);
            }
            
            log.info("已保存 {} 个场景", scenes.size());
            
            // 保存线索
            ClueService clueService = SpringContextUtil.getBean(ClueService.class);
            List<Clue> clues = new ArrayList<>();
            
            JsonNode cluesNode = rootNode.path("clues");
            if (cluesNode.isArray()) {
                for (JsonNode clueNode : cluesNode) {
                    Clue clue = new Clue();
                    clue.setScriptId(scriptId);
                    clue.setName(clueNode.path("name").asText("未命名线索"));
                    clue.setDescription(clueNode.path("content").asText(""));
                    
                    try {
                        clue.setType(ClueType.valueOf(clueNode.path("type").asText().toUpperCase()));
                    } catch (Exception e) {
                        clue.setType(ClueType.PHYSICAL);
                    }
                    
                    try {
                        clue.setVisibility(ClueVisibility.valueOf(clueNode.path("visibility").asText().toUpperCase()));
                    } catch (Exception e) {
                        clue.setVisibility(ClueVisibility.PUBLIC);
                    }
                    
                    clue.setScene(clueNode.path("location").asText(""));
                    clue.setImageUrl("");
                    clue.setImportance(1);
                    clue.setCreateTime(LocalDateTime.now());
                    
                    clues.add(clue);
                    log.debug("创建线索: {}", clue.getName());
                }
            }
            
            for (Clue clue : clues) {
                clueService.createClue(clue);
            }
            
            log.info("已保存 {} 个线索", clues.size());
            
        } catch (Exception e) {
            log.error("保存场景和线索信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存场景和线索信息失败", e);
        }
    }
}