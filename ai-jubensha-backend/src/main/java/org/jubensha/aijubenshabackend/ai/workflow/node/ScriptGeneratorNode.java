package org.jubensha.aijubenshabackend.ai.workflow.node;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.factory.ScriptGenerateServiceFactory;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
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
import org.jubensha.aijubenshabackend.service.script.ScriptTempStorageService;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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
            // 生成临时scriptId，使用UUID的哈希值确保唯一性
            Long tempScriptId = Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits());
            ScriptGenerateService generateServiceFactoryService = scriptGenerateServiceFactory.getService(tempScriptId);
            String scriptJson = generateServiceFactoryService.generateScript(userMessage);

            log.info("剧本生成完成");

            try {
                // 预处理JSON，移除代码块标记并修复不完整的JSON
                String cleanedJson = preprocessJson(scriptJson);
                log.debug("清理后的JSON长度: {}", cleanedJson.length());

                // 解析JSON剧本
                JsonNode rootNode = objectMapper.readTree(cleanedJson);

                // 提取剧本基本信息
                String scriptName = rootNode.path("scriptName").asText();
                if (scriptName == null || scriptName.isEmpty()) {
                    scriptName = context.getOriginalPrompt();
                    if (scriptName == null || scriptName.isEmpty()) {
                        scriptName = "默认剧本_" + System.currentTimeMillis();
                    }
                }
                String scriptIntro = rootNode.path("scriptIntro").asText("");
                String scriptTimeline = rootNode.path("scriptTimeline").asText("");

                // 创建封面图片URL
                URL coverImageUrl = new URL("https://picsum.photos/200/300");

                // 创建剧本实体（不设置ID，由数据库自增）
                Script newScript = Script.builder()
                        .name(scriptName)
                        .description(scriptIntro)
                        .timeline(scriptTimeline)
                        .author("AI Generated")
                        .difficulty(DifficultyLevel.MEDIUM)
                        // 默认2小时
                        .duration(120)
                        // 默认6人
                        .playerCount(6)
                        .coverImageUrl(coverImageUrl.toString())
                        .build();

                // 保存剧本到数据库，获取自增ID
                ScriptService scriptService = SpringContextUtil.getBean(ScriptService.class);

                /*
                // 将Script实体转换为ScriptCreateRequest DTO
                ScriptDTO.ScriptCreateRequest createRequest = new ScriptDTO.ScriptCreateRequest();
                createRequest.setName(newScript.getName());
                createRequest.setDescription(newScript.getDescription());
                createRequest.setAuthor("AI Generated");
                createRequest.setDifficulty(org.jubensha.aijubenshabackend.models.enums.DifficultyLevel.MEDIUM);
                createRequest.setDuration(120); // 默认2小时
                createRequest.setPlayerCount(6); // 默认6人
                */

                ScriptResponseDTO savedScript = ScriptResponseDTO.fromEntity(
                        scriptService.createScript(newScript)
                );

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
     * 创建流式生成节点
     */
    public static AsyncNodeAction<MessagesState<String>> createStream() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("ScriptGeneratorNode (Stream): {}", context);
            log.info("执行节点：流式剧本生成");
            // 构建用户消息（包含原始提示词合可能的错误修复信息）
            String userMessage = buildUserMessage(context);

            // 获取 AI 服务实例
            ScriptGenerateServiceFactory scriptGenerateServiceFactory = SpringContextUtil.getBean(
                    ScriptGenerateServiceFactory.class);
            log.info("获取 AI 服务实例");
            log.info("开始流式生成剧本");
            // 生成临时scriptId，使用UUID的哈希值确保唯一性
            Long tempScriptId = Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits());
            ScriptGenerateService generateServiceFactoryService = scriptGenerateServiceFactory.getService(tempScriptId);

            // 获取临时存储服务
            ScriptTempStorageService tempStorageService = SpringContextUtil.getBean(ScriptTempStorageService.class);

            // 使用流式生成
            AtomicReference<String> scriptJsonBuilder = new AtomicReference<>("");
            CompletableFuture<Void> streamFuture = new CompletableFuture<>();

            // 调整批量大小，减少临时存储操作频率
            final int BATCH_SIZE = 2000; // 每2000个字符保存一次
            
            generateServiceFactoryService.generateScriptStream(userMessage)
                    .doOnNext(chunk -> {
                        // 处理每个生成的chunk
                        scriptJsonBuilder.updateAndGet(current -> current + chunk);
                        log.debug("收到生成chunk，长度: {}", chunk.length());
                        // 每生成一定量的内容，进行一次临时保存
                        if (scriptJsonBuilder.get().length() % BATCH_SIZE == 0) {
                            log.debug("临时保存生成内容，当前长度: {}", scriptJsonBuilder.get().length());
                            // 保存到临时存储
                            tempStorageService.storeTempScript(tempScriptId, scriptJsonBuilder.get());
                        }
                    })
                    .doOnComplete(() -> {
                        log.info("流式生成剧本完成");
                        // 生成完成后，删除临时存储
                        tempStorageService.deleteTempScript(tempScriptId);
                        streamFuture.complete(null);
                    })
                    .doOnError(error -> {
                        log.error("流式生成剧本失败: {}", error.getMessage(), error);
                        // 生成失败后，保留临时存储，以便后续恢复
                        streamFuture.completeExceptionally(error);
                    })
                    .subscribe();

            // 等待流式生成完成
            try {
                streamFuture.join();
                String scriptJson = scriptJsonBuilder.get();

                // 预处理JSON，移除代码块标记并修复不完整的JSON
                String cleanedJson = preprocessJson(scriptJson);
                log.debug("清理后的JSON长度: {}", cleanedJson.length());

                // 解析JSON剧本
                JsonNode rootNode = objectMapper.readTree(cleanedJson);

                // 提取剧本基本信息
                String scriptName = rootNode.path("scriptName").asText();
                if (scriptName == null || scriptName.isEmpty()) {
                    scriptName = context.getOriginalPrompt();
                    if (scriptName == null || scriptName.isEmpty()) {
                        scriptName = "默认剧本_" + System.currentTimeMillis();
                    }
                }
                String scriptIntro = rootNode.path("scriptIntro").asText("");
                String scriptTimeline = rootNode.path("scriptTimeline").asText("");

                // 创建封面图片URL
                URL coverImageUrl = new URL("https://picsum.photos/200/300");

                // 创建剧本实体（不设置ID，由数据库自增）
                Script newScript = Script.builder()
                        .name(scriptName)
                        .description(scriptIntro)
                        .timeline(scriptTimeline)
                        .author("AI Generated")
                        .difficulty(DifficultyLevel.MEDIUM)
                        // 默认2小时
                        .duration(120)
                        // 默认6人
                        .playerCount(6)
                        .coverImageUrl(coverImageUrl.toString())
                        .build();

                // 保存剧本到数据库，获取自增ID
                ScriptService scriptService = SpringContextUtil.getBean(ScriptService.class);
                ScriptResponseDTO savedScript = ScriptResponseDTO.fromEntity(
                        scriptService.createScript(newScript)
                );

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
                context.setCurrentStep("流式剧本生成");
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
                scene.setScriptId(scriptId);
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
     * 预处理JSON，移除代码块标记并修复不完整的JSON
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

        // 尝试修复不完整的JSON
        json = fixIncompleteJson(json);

        return json;
    }

    /**
     * 修复不完整的JSON字符串
     */
    private static String fixIncompleteJson(String json) {
        try {
            // 尝试解析JSON，如果成功则返回原JSON
            objectMapper.readTree(json);
            return json;
        } catch (Exception e) {
            log.debug("JSON解析失败，尝试修复: {}", e.getMessage());
            
            // 1. 处理字符串中间被截断的情况
            char[] chars = json.toCharArray();
            boolean inString = false;
            int lastValidQuoteIndex = -1;
            boolean escaped = false;
            
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = !inString;
                    lastValidQuoteIndex = i;
                }
            }
            
            // 如果JSON在字符串中间被截断，截断到最后一个有效引号
            if (inString && lastValidQuoteIndex != -1) {
                json = json.substring(0, lastValidQuoteIndex + 1);
                log.debug("JSON在字符串中间被截断，已截断到最后一个有效引号");
            }
            
            // 2. 清理无效的尾部字符
            // 移除尾部可能的不完整单词或字符
            json = json.replaceAll("[,\s}]*$", "");
            
            // 3. 补充缺失的闭合括号
            int openBraces = 0;
            int closeBraces = 0;
            int openBrackets = 0;
            int closeBrackets = 0;
            
            // 重新计算括号数量，确保只在非字符串内部计数
            inString = false;
            escaped = false;
            for (char c : json.toCharArray()) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') openBraces++;
                    if (c == '}') closeBraces++;
                    if (c == '[') openBrackets++;
                    if (c == ']') closeBrackets++;
                }
            }
            
            // 补充缺失的闭合括号
            StringBuilder sb = new StringBuilder(json);
            
            // 先补充缺失的方括号
            while (openBrackets > closeBrackets) {
                sb.append(']');
                closeBrackets++;
            }
            
            // 再补充缺失的花括号
            while (openBraces > closeBraces) {
                sb.append('}');
                closeBraces++;
            }
            
            json = sb.toString();
            log.debug("修复后的JSON长度: {}", json.length());
            
            // 4. 再次尝试解析，如果仍然失败，尝试更激进的修复
            try {
                objectMapper.readTree(json);
                return json;
            } catch (Exception e2) {
                log.warn("修复后的JSON仍然无法解析，尝试更激进的修复");
                
                // 尝试找到最后一个有效的JSON结构
                int lastValidJsonEnd = json.lastIndexOf('}');
                if (lastValidJsonEnd != -1) {
                    String partialJson = json.substring(0, lastValidJsonEnd + 1);
                    try {
                        objectMapper.readTree(partialJson);
                        log.info("使用部分有效的JSON结构");
                        return partialJson;
                    } catch (Exception e3) {
                        // 继续尝试
                    }
                }
                
                // 如果仍然失败，返回一个基本的JSON结构
                log.warn("无法修复JSON，返回基本结构");
                return "{\"scriptName\": \"未完成的剧本\", \"scriptIntro\": \"剧本生成过程中出现错误\", \"characters\": [], \"scenes\": [], \"clues\": []}";
            }
        }
    }
}
