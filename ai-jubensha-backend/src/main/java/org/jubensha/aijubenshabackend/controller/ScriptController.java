package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.URL;
import org.jubensha.aijubenshabackend.models.dto.ScriptCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.ScriptResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.ScriptUpdateDTO;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.jubensha.aijubenshabackend.service.task.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     * @param id         剧本ID
     * @param coverImageUrl 封面图片URL
     * @return 更新后的剧本响应DTO
     */
    @PutMapping("/{id}/cover-image")
    public ResponseEntity<ScriptResponseDTO> updateScriptCoverImage(@PathVariable Long id, @URL @RequestParam String coverImageUrl) {
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
}
