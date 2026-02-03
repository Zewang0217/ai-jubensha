package org.jubensha.aijubenshabackend.ai.workflow.node;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.factory.ScriptGenerateServiceFactory;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.dto.ScriptDTO;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 剧本生成节点
 *
 * @author zewang
 * @version 1.0
 * @date 2026-01-30 20:29
 * @since 2026
 */

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ScriptGeneratorNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("ScriptGeneratorNode: {}", context);
            log.info("执行节点：剧本生成");
            // 构建用户消息（包含原始提示词合可能的错误修复信息）
            String userMessage = buildUserMessage(context);

            // 获取 AI 服务实例
            ScriptGenerateServiceFactory scriptGenerateServiceFactory = SpringContextUtil.getBean(
                ScriptGenerateServiceFactory.class);
            log.info("获取 AI 服务实例");
            log.info("开始生成剧本");
            // 临时scriptId，用于服务实例创建
            Long tempScriptId = System.currentTimeMillis();
            ScriptGenerateService generateServiceFactoryService = scriptGenerateServiceFactory.getService(tempScriptId);
            String scriptJson = generateServiceFactoryService.generateScript(userMessage);

            log.info("剧本生成完成");
            
            try {
                // 预处理JSON，移除代码块标记
                String cleanedJson = preprocessJson(scriptJson);
                log.debug("清理后的JSON长度: {}", cleanedJson.length());
                
                // 解析JSON剧本
                JsonNode rootNode = objectMapper.readTree(cleanedJson);
                
                // 提取剧本基本信息
                String scriptName = rootNode.path("scriptName").asText(context.getOriginalPrompt());
                String scriptIntro = rootNode.path("scriptIntro").asText("");
                String scriptTimeline = rootNode.path("scriptTimeline").asText("");
                
                // 创建剧本实体（不设置ID，由数据库自增）
                Script newScript = Script.builder()
                    .name(scriptName)
                    .description(scriptIntro)
                    .timeline(scriptTimeline)
                    .build();

                // 保存剧本到数据库，获取自增ID
                ScriptService scriptService = SpringContextUtil.getBean(ScriptService.class);

              // TODO：修改逻辑，使用更新后的代码逻辑
                // 将Script实体转换为ScriptCreateRequest DTO
                ScriptDTO.ScriptCreateRequest createRequest = new ScriptDTO.ScriptCreateRequest();
                createRequest.setName(newScript.getName());
                createRequest.setDescription(newScript.getDescription());
                createRequest.setAuthor("AI Generated");
                createRequest.setDifficulty(org.jubensha.aijubenshabackend.models.enums.DifficultyLevel.MEDIUM);
                createRequest.setDuration(120); // 默认2小时
                createRequest.setPlayerCount(6); // 默认6人
                
                ScriptDTO.ScriptResponse savedScript = scriptService.createScript(createRequest);
                log.info("剧本已保存到数据库，ID: {}", savedScript.getId());
                context.setScriptId(savedScript.getId());
                Script savedScript = scriptService.createScript(newScript);
                Long scriptId = savedScript.getId();
                log.info("剧本已保存到数据库，ID: {}", scriptId);
                context.setScriptId(scriptId);
                
                // 提取并保存角色信息
                List<Character> characters = parseCharacters(rootNode, scriptId);
                CharacterService characterService = SpringContextUtil.getBean(CharacterService.class);
                for (Character character : characters) {
                    characterService.createCharacter(character);
                }
                log.info("已保存 {} 个角色", characters.size());
                
                // 提取并保存线索信息
                List<Clue> clues = parseClues(rootNode, scriptId);
                ClueService clueService = SpringContextUtil.getBean(ClueService.class);
                for (Clue clue : clues) {
                    clueService.createClue(clue);
                }
                log.info("已保存 {} 个线索", clues.size());
                
                // 提取并保存场景信息
                List<Scene> scenes = parseScenes(rootNode, scriptId);
                SceneService sceneService = SpringContextUtil.getBean(SceneService.class);
                for (Scene scene : scenes) {
                    sceneService.createScene(scene);
                }
                log.info("已保存 {} 个场景", scenes.size());
                
                // 更新WorkflowContext
                context.setCurrentStep("剧本生成");
                context.setScriptName(scriptName);
                context.setScriptType("推理本"); // 默认类型，后续可从用户输入中解析
                context.setScriptDifficulty("中等"); // 默认难度，后续可从用户输入中解析
                context.setModelOutput(scriptJson);
                context.setSuccess(true);
                context.setStartTime(LocalDateTime.now());
                
            } catch (Exception e) {
                log.error("解析和保存剧本失败: {}", e.getMessage(), e);
                context.setErrorMessage("解析剧本失败: " + e.getMessage());
                context.setSuccess(false);
            }
            
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 解析角色信息
     */
    private static List<Character> parseCharacters(JsonNode rootNode, Long scriptId) {
        List<Character> characters = new ArrayList<>();
        JsonNode charactersNode = rootNode.path("characters");
        if (charactersNode.isArray()) {
            for (JsonNode characterNode : charactersNode) {
                Character character = new Character();
                character.setScriptId(scriptId);
                character.setName(characterNode.path("name").asText());
                character.setDescription("年龄: " + characterNode.path("age").asText() + "\n" +
                    "身份: " + characterNode.path("identity").asText() + "\n" +
                    "性格特点: " + characterNode.path("personality").asText());
                character.setBackgroundStory(characterNode.path("background").asText());
                character.setSecret(characterNode.path("secrets").asText());
                character.setTimeline(characterNode.path("timeline").asText());
                character.setCreateTime(LocalDateTime.now());
                characters.add(character);
            }
        }
        return characters;
    }

    /**
     * 解析线索信息
     */
    private static List<Clue> parseClues(JsonNode rootNode, Long scriptId) {
        List<Clue> clues = new ArrayList<>();
        JsonNode cluesNode = rootNode.path("clues");
        if (cluesNode.isArray()) {
            for (JsonNode clueNode : cluesNode) {
                Clue clue = new Clue();
                clue.setScriptId(scriptId);
                clue.setName(clueNode.path("name").asText());
                clue.setDescription(clueNode.path("content").asText());
                try {
                    clue.setType(ClueType.valueOf(clueNode.path("type").asText().toUpperCase()));
                } catch (Exception e) {
                    clue.setType(ClueType.PHYSICAL); // 默认类型
                }
                try {
                    clue.setVisibility(ClueVisibility.valueOf(clueNode.path("visibility").asText().toUpperCase()));
                } catch (Exception e) {
                    clue.setVisibility(ClueVisibility.PUBLIC); // 默认可见性
                }
                clue.setScene(clueNode.path("scene").asText());
                clue.setImportance(clueNode.path("importance").asInt(1));
                clue.setCreateTime(LocalDateTime.now());
                clues.add(clue);
            }
        }
        return clues;
    }

    /**
     * 解析场景信息
     */
    private static List<Scene> parseScenes(JsonNode rootNode, Long scriptId) {
        List<Scene> scenes = new ArrayList<>();
        JsonNode scenesNode = rootNode.path("scenes");
        if (scenesNode.isArray()) {
            for (JsonNode sceneNode : scenesNode) {
                Scene scene = new Scene();
                Script script = new Script();
                script.setId(scriptId);
                scene.setScript(script);
                scene.setName(sceneNode.path("name").asText());
                scene.setDescription("时间: " + sceneNode.path("time").asText() + "\n" +
                    "地点: " + sceneNode.path("location").asText() + "\n" +
                    "氛围: " + sceneNode.path("atmosphere").asText() + "\n" +
                    "描述: " + sceneNode.path("description").asText());
                scene.setCreateTime(LocalDateTime.now());
                scenes.add(scene);
            }
        }
        return scenes;
    }

    /**
     * 构造用户信息
     */
    private static String buildUserMessage(WorkflowContext context) {
        StringBuilder message = new StringBuilder();
        message.append("用户需求：").append(context.getOriginalPrompt()).append("\n");

        if (context.getErrorMessage() != null) {
            message.append("\n之前的错误：").append(context.getErrorMessage()).append("\n");
            message.append("请避免类似错误，重新生成剧本。");
        }

        return message.toString();
    }

    /**
     * 预处理JSON，移除代码块标记
     */
    private static String preprocessJson(String json) {
        // 移除开头的代码块标记
        if (json.startsWith("```json")) {
            json = json.substring(7);
        } else if (json.startsWith("```")) {
            json = json.substring(3);
        }
        
        // 移除结尾的代码块标记
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        
        // 去除首尾空白
        json = json.trim();
        
        return json;
    }
}
