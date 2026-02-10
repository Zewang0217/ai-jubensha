package org.jubensha.aijubenshabackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.jubensha.aijubenshabackend.ai.factory.ScriptGenerateServiceFactory;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.dto.ScriptCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.ScriptResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.ScriptUpdateDTO;
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
import org.jubensha.aijubenshabackend.service.task.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 剧本控制器
 */
@RestController
@RequestMapping("/api/scripts")
public class ScriptController {

    private final ScriptService scriptService;
    private final TaskService taskService;

    public ScriptController(ScriptService scriptService, TaskService taskService) {
        this.scriptService = scriptService;
        this.taskService = taskService;
    }

    /**
     * 创建剧本
     *
     * @param scriptCreateDTO 剧本创建DTO
     * @return 创建的剧本响应DTO
     */
    @PostMapping
    public ResponseEntity<ScriptResponseDTO> createScript(@Valid @RequestBody ScriptCreateDTO scriptCreateDTO) {
        Script script = new Script();
        script.setName(scriptCreateDTO.getName());
        script.setDescription(scriptCreateDTO.getDescription());
        script.setAuthor(scriptCreateDTO.getAuthor());
        script.setDifficulty(scriptCreateDTO.getDifficulty());
        script.setDuration(scriptCreateDTO.getDuration());
        script.setPlayerCount(scriptCreateDTO.getPlayerCount());
        script.setCoverImageUrl(scriptCreateDTO.getCoverImageUrl());

        Script createdScript = scriptService.createScript(script);
        ScriptResponseDTO responseDTO = ScriptResponseDTO.fromEntity(createdScript);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * 更新剧本
     *
     * @param id              剧本ID
     * @param scriptUpdateDTO 剧本更新DTO
     * @return 更新后的剧本响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScriptResponseDTO> updateScript(@PathVariable Long id, @Valid @RequestBody ScriptUpdateDTO scriptUpdateDTO) {
        Script script = new Script();
        script.setName(scriptUpdateDTO.getName());
        script.setDescription(scriptUpdateDTO.getDescription());
        script.setAuthor(scriptUpdateDTO.getAuthor());
        script.setDifficulty(scriptUpdateDTO.getDifficulty());
        script.setDuration(scriptUpdateDTO.getDuration());
        script.setPlayerCount(scriptUpdateDTO.getPlayerCount());
        script.setCoverImageUrl(scriptUpdateDTO.getCoverImageUrl());

        try {
            Script updatedScript = scriptService.updateScript(id, script);
            ScriptResponseDTO responseDTO = ScriptResponseDTO.fromEntity(updatedScript);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除剧本
     *
     * @param id 剧本ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable Long id) {
        try {
            scriptService.deleteScript(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 根据ID查询剧本
     *
     * @param id 剧本ID
     * @return 剧本响应DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScriptResponseDTO> getScriptById(@PathVariable Long id) {
        Optional<Script> script = scriptService.getScriptById(id);
        return script.map(value -> {
            ScriptResponseDTO responseDTO = ScriptResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 查询所有剧本
     *
     * @return 剧本响应DTO列表
     */
    @GetMapping
    public ResponseEntity<List<ScriptResponseDTO>> getAllScripts() {
        List<Script> scripts = scriptService.getAllScripts();
        List<ScriptResponseDTO> responseDTOs = scripts.stream()
                .map(ScriptResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据作者查询剧本
     *
     * @param author 作者
     * @return 剧本响应DTO列表
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<List<ScriptResponseDTO>> getScriptsByAuthor(@PathVariable String author) {
        List<Script> scripts = scriptService.getScriptsByAuthor(author);
        List<ScriptResponseDTO> responseDTOs = scripts.stream()
                .map(ScriptResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据难度查询剧本
     *
     * @param difficulty 难度
     * @return 剧本响应DTO列表
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<ScriptResponseDTO>> getScriptsByDifficulty(@PathVariable String difficulty) {
        try {
            DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());
            List<Script> scripts = scriptService.getScriptsByDifficulty(difficultyLevel);
            List<ScriptResponseDTO> responseDTOs = scripts.stream()
                    .map(ScriptResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据玩家人数查询剧本
     *
     * @param playerCount 玩家人数
     * @return 剧本响应DTO列表
     */
    @GetMapping("/player-count/{playerCount}")
    public ResponseEntity<List<ScriptResponseDTO>> getScriptsByPlayerCount(@PathVariable Integer playerCount) {
        List<Script> scripts = scriptService.getScriptsByPlayerCount(playerCount);
        List<ScriptResponseDTO> responseDTOs = scripts.stream()
                .map(ScriptResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据时长查询剧本
     *
     * @param duration 游戏时长
     * @return 剧本响应DTO列表
     */
    @GetMapping("/duration/{duration}")
    public ResponseEntity<List<ScriptResponseDTO>> getScriptsByDuration(@PathVariable Integer duration) {
        List<Script> scripts = scriptService.getScriptsByDuration(duration);
        List<ScriptResponseDTO> responseDTOs = scripts.stream()
                .map(ScriptResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据名称搜索剧本
     *
     * @param name 名称
     * @return 剧本响应DTO列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<ScriptResponseDTO>> searchScriptsByName(@RequestParam String name) {
        List<Script> scripts = scriptService.searchScriptsByName(name);
        List<ScriptResponseDTO> responseDTOs = scripts.stream()
                .map(ScriptResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据描述关键词搜索剧本
     *
     * @param keyword 关键词
     * @return 剧本响应DTO列表
     */
    @GetMapping("/search/description")
    public ResponseEntity<List<ScriptResponseDTO>> searchScriptsByDescription(@RequestParam String keyword) {
        List<Script> scripts = scriptService.searchScriptsByDescription(keyword);
        List<ScriptResponseDTO> responseDTOs = scripts.stream()
                .map(ScriptResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据难度和玩家人数查询剧本
     *
     * @param difficulty  难度
     * @param playerCount 玩家人数
     * @return 剧本响应DTO列表
     */
    @GetMapping("/difficulty/{difficulty}/player-count/{playerCount}")
    public ResponseEntity<List<ScriptResponseDTO>> getScriptsByDifficultyAndPlayerCount(
            @PathVariable String difficulty, @PathVariable Integer playerCount) {
        try {
            DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());
            List<Script> scripts = scriptService.getScriptsByDifficultyAndPlayerCount(difficultyLevel, playerCount);
            List<ScriptResponseDTO> responseDTOs = scripts.stream()
                    .map(ScriptResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据创建时间范围查询剧本
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 剧本响应DTO列表
     */
    @GetMapping("/created")
    public ResponseEntity<List<ScriptResponseDTO>> getScriptsByCreateTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<Script> scripts = scriptService.getScriptsByCreateTimeRange(startTime, endTime);
        List<ScriptResponseDTO> responseDTOs = scripts.stream()
                .map(ScriptResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 更新剧本封面图片
     *
     * @param id            剧本ID
     * @param coverImageUrl 封面图片URL
     * @return 更新后的剧本响应DTO
     */
    @PutMapping("/{id}/cover-image")
    public ResponseEntity<ScriptResponseDTO> updateScriptCoverImage(@PathVariable Long id, @RequestParam String coverImageUrl) {
        try {
            Script script = scriptService.updateScriptCoverImage(id, coverImageUrl);
            ScriptResponseDTO responseDTO = ScriptResponseDTO.fromEntity(script);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 更新剧本时间线
     *
     * @param id       剧本ID
     * @param timeline 时间线内容
     * @return 更新后的剧本响应DTO
     */
    @PutMapping("/{id}/timeline")
    public ResponseEntity<ScriptResponseDTO> updateScriptTimeline(@PathVariable Long id, @RequestParam String timeline) {
        try {
            Script script = scriptService.updateScriptTimeline(id, timeline);
            ScriptResponseDTO responseDTO = ScriptResponseDTO.fromEntity(script);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 批量删除剧本
     *
     * @param ids 剧本ID列表
     * @return 响应
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteScriptsBatch(@RequestParam List<Long> ids) {
        try {
            scriptService.deleteScriptsBatch(ids);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        try {
            return taskService.getTaskStatus(taskId)
                    .map(taskInfo -> {
                        Map<String, Object> response = Map.of(
                                "taskId", taskId,
                                "status", taskInfo.getStatus(),
                                "result", taskInfo.getResult(),
                                "errorMessage", taskInfo.getErrorMessage()
                        );
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Task not found")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get task status"));
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 流式生成剧本
     *
     * @param prompt 用户需求
     * @return 流式响应
     */
    @GetMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateScriptStream(@RequestParam String prompt) {
        try {
            // 生成临时scriptId，使用UUID的哈希值确保唯一性
            Long tempScriptId = Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits());
            
            // 获取AI服务实例
            ScriptGenerateServiceFactory scriptGenerateServiceFactory = SpringContextUtil.getBean(ScriptGenerateServiceFactory.class);
            ScriptGenerateService generateService = scriptGenerateServiceFactory.getService(tempScriptId);
            
            // 获取临时存储服务
            ScriptTempStorageService tempStorageService = SpringContextUtil.getBean(ScriptTempStorageService.class);
            
            // 调用流式生成方法
            Flux<String> scriptStream = generateService.generateScriptStream(prompt);
            
            // 用于收集完整剧本内容
            AtomicReference<String> fullScript = new AtomicReference<>("");
            // 调整批量大小，增加存储频率，确保即使在流式输出被截断的情况下，也能保存尽可能多的内容
            final int BATCH_SIZE = 500; // 每500个字符保存一次
            final int MAX_BATCH_SIZE = 15000; // 最大批量大小
            
            // 转换为ServerSentEvent
            return scriptStream.map(content -> {
                // 收集完整剧本内容
                fullScript.updateAndGet(current -> current + content);
                // 每生成一定量的内容，进行一次临时保存
                int currentLength = fullScript.get().length();
                if (currentLength % BATCH_SIZE == 0 || currentLength >= MAX_BATCH_SIZE) {
                    tempStorageService.storeTempScript(tempScriptId, fullScript.get());
                    System.out.println("临时保存剧本内容，长度: " + currentLength);
                }
                // 返回ServerSentEvent
                return ServerSentEvent.<String>builder()
                        .data(content)
                        .build();
            })
            .doOnComplete(() -> {
                // 流式输出完成后，再次保存完整内容到临时存储
                tempStorageService.storeTempScript(tempScriptId, fullScript.get());
                System.out.println("流式输出完成，保存完整内容到临时存储，长度: " + fullScript.get().length());
                // 将剧本保存到数据库
                saveScriptToDatabase(fullScript.get(), tempScriptId, tempStorageService);
            })
            .onErrorResume(error -> {
                // 流式输出出错时，记录错误信息，并保存当前已生成的内容到临时存储
                System.err.println("流式生成剧本失败: " + error.getMessage());
                if (!fullScript.get().isEmpty()) {
                    tempStorageService.storeTempScript(tempScriptId, fullScript.get());
                    System.out.println("流式输出出错，保存已生成的内容到临时存储，长度: " + fullScript.get().length());
                }
                return Flux.just(ServerSentEvent.<String>builder()
                        .data("Error: " + error.getMessage())
                        .build());
            });
        } catch (Exception e) {
            System.err.println("初始化流式生成失败: " + e.getMessage());
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 将生成的剧本保存到数据库
     */
    private void saveScriptToDatabase(String scriptJson, Long tempScriptId, ScriptTempStorageService tempStorageService) {
        try {
            System.out.println("开始将剧本保存到数据库...");
            
            // 尝试从临时存储中获取完整的剧本内容
            String tempScriptContent = tempStorageService.getTempScript(tempScriptId);
            if (tempScriptContent != null && !tempScriptContent.isEmpty() && tempScriptContent.length() > scriptJson.length()) {
                System.out.println("从临时存储中获取到更长的剧本内容，使用临时存储中的内容进行解析");
                scriptJson = tempScriptContent;
            }
            
            // 预处理JSON，移除代码块标记并修复不完整的JSON
            String cleanedJson = preprocessJson(scriptJson);
            System.out.println("清理后的JSON长度: " + cleanedJson.length());

            // 解析JSON剧本
            JsonNode rootNode = objectMapper.readTree(cleanedJson);

            // 提取剧本基本信息
            String scriptName = rootNode.path("scriptName").asText();
            if (scriptName == null || scriptName.isEmpty()) {
                scriptName = "自动生成剧本_" + System.currentTimeMillis();
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
            System.out.println("剧本已保存到数据库，ID: " + scriptId);

            // 提取并保存角色信息
            List<Character> characters = parseCharacters(rootNode, scriptId);
            CharacterService characterService = SpringContextUtil.getBean(CharacterService.class);
            for (Character character : characters) {
                characterService.createCharacter(character);
            }
            System.out.println("已保存 " + characters.size() + " 个角色");

            // 提取并保存线索信息
            List<Clue> clues = parseClues(rootNode, scriptId);
            ClueService clueService = SpringContextUtil.getBean(ClueService.class);
            for (Clue clue : clues) {
                clueService.createClue(clue);
            }
            System.out.println("已保存 " + clues.size() + " 个线索");

            // 提取并保存场景信息
            List<Scene> scenes = parseScenes(rootNode, scriptId);
            SceneService sceneService = SpringContextUtil.getBean(SceneService.class);
            for (Scene scene : scenes) {
                sceneService.createScene(scene);
            }
            System.out.println("已保存 " + scenes.size() + " 个场景");

            // 生成完成后，删除临时存储
            tempStorageService.deleteTempScript(tempScriptId);
            System.out.println("剧本生成和存储完成，临时存储已清理");

        } catch (Exception e) {
            System.err.println("解析和保存剧本失败: " + e.getMessage());
            // 生成失败后，保留临时存储，以便后续恢复
            System.out.println("保留临时存储，以便后续恢复: " + tempScriptId);
        }
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
     * 预处理JSON，移除代码块标记和非JSON文本并修复不完整的JSON
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

        // 识别并移除JSON前面的非JSON文本
        int firstBraceIndex = json.indexOf('{');
        if (firstBraceIndex != -1) {
            // 只保留从第一个左花括号开始的内容
            json = json.substring(firstBraceIndex);
            System.out.println("移除了JSON前面的非JSON文本");
        }

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
            System.out.println("JSON解析失败，尝试修复: " + e.getMessage());
            
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
                System.out.println("JSON在字符串中间被截断，已截断到最后一个有效引号");
            }
            
            // 2. 处理字段名中间被截断的情况
            // 查找最后一个有效的字段名结束位置
            int lastValidFieldEnd = json.lastIndexOf(':');
            if (lastValidFieldEnd != -1) {
                // 从最后一个冒号开始，查找下一个有效的值结束位置
                int valueEnd = lastValidFieldEnd + 1;
                boolean inValue = false;
                boolean valueEscaped = false;
                
                while (valueEnd < json.length()) {
                    char c = json.charAt(valueEnd);
                    if (valueEscaped) {
                        valueEscaped = false;
                    } else if (c == '\\') {
                        valueEscaped = true;
                    } else if (c == '"') {
                        inValue = !inValue;
                    } else if (!inValue && (c == ',' || c == '}' || c == ']')) {
                        break;
                    }
                    valueEnd++;
                }
                
                // 如果找到了有效的值结束位置，截断到该位置
                if (valueEnd < json.length()) {
                    json = json.substring(0, valueEnd);
                    System.out.println("JSON字段名中间被截断，已修复到最后一个有效字段");
                }
            }
            
            // 3. 清理无效的尾部字符
            // 移除尾部可能的不完整单词或字符
            json = json.replaceAll("[,\s}]*$", "");
            
            // 4. 补充缺失的闭合括号
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
            System.out.println("修复后的JSON长度: " + json.length());
            
            // 5. 再次尝试解析，如果仍然失败，尝试更激进的修复
            try {
                objectMapper.readTree(json);
                return json;
            } catch (Exception e2) {
                System.out.println("修复后的JSON仍然无法解析，尝试更激进的修复: " + e2.getMessage());
                
                // 尝试找到最后一个有效的JSON结构
                int lastValidJsonEnd = json.lastIndexOf('}');
                if (lastValidJsonEnd != -1) {
                    String partialJson = json.substring(0, lastValidJsonEnd + 1);
                    try {
                        objectMapper.readTree(partialJson);
                        System.out.println("使用部分有效的JSON结构");
                        return partialJson;
                    } catch (Exception e3) {
                        // 继续尝试
                        System.out.println("部分JSON结构仍然无法解析: " + e3.getMessage());
                    }
                }
                
                // 尝试找到最后一个有效的数组结构
                int lastValidArrayEnd = json.lastIndexOf(']');
                if (lastValidArrayEnd != -1) {
                    String partialJson = json.substring(0, lastValidArrayEnd + 1);
                    try {
                        objectMapper.readTree(partialJson);
                        System.out.println("使用部分有效的数组结构");
                        return partialJson;
                    } catch (Exception e3) {
                        // 继续尝试
                        System.out.println("部分数组结构仍然无法解析: " + e3.getMessage());
                    }
                }
                
                // 如果仍然失败，返回一个基本的JSON结构
                System.out.println("无法修复JSON，返回基本结构");
                return "{\"scriptName\": \"未完成的剧本\", \"scriptIntro\": \"剧本生成过程中出现错误\", \"characters\": [], \"scenes\": [], \"clues\": []}";
            }
        }
    }
}
