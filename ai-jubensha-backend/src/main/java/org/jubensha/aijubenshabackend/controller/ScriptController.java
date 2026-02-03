package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import org.jubensha.aijubenshabackend.models.dto.ScriptDTO;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.jubensha.aijubenshabackend.service.task.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * @param request 剧本创建请求DTO
     * @return 创建的剧本响应DTO
     */
    @PostMapping
    public ResponseEntity<ScriptDTO.ScriptResponse> createScript(
            @Valid @RequestBody ScriptDTO.ScriptCreateRequest request) {
        ScriptDTO.ScriptResponse response = scriptService.createScript(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /**
     * 更新剧本
     * @param id 剧本ID
     * @param request 剧本更新请求DTO
     * @return 更新后的剧本响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScriptDTO.ScriptResponse> updateScript(@PathVariable Long id,
                                                                 @Valid @RequestBody ScriptDTO.ScriptUpdateRequest request) {
        ScriptDTO.ScriptResponse response = scriptService.updateScript(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 删除剧本
     * @param id 剧本ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable Long id) {
        scriptService.deleteScript(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据ID查询剧本
     * @param id 剧本ID
     * @return 剧本响应DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScriptDTO.ScriptResponse> getScriptById(@PathVariable Long id) {
        Optional<ScriptDTO.ScriptResponse> script = scriptService.getScriptById(id);
        return script.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询所有剧本
     * @return 剧本响应DTO列表
     */
    @GetMapping
    public ResponseEntity<List<ScriptDTO.ScriptResponse>> getAllScripts() {
        List<ScriptDTO.ScriptResponse> scripts = scriptService.getAllScripts();
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }

    /**
     * 根据难度查询剧本
     * @param difficulty 难度
     * @return 剧本响应DTO列表
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<ScriptDTO.ScriptResponse>> getScriptsByDifficulty(@PathVariable String difficulty) {
        try {
            DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());
            List<ScriptDTO.ScriptResponse> scripts = scriptService.getScriptsByDifficulty(difficultyLevel);
            return new ResponseEntity<>(scripts, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据玩家人数查询剧本
     * @param playerCount 玩家人数
     * @return 剧本响应DTO列表
     */
    @GetMapping("/player-count/{playerCount}")
    public ResponseEntity<List<ScriptDTO.ScriptResponse>> getScriptsByPlayerCount(@PathVariable Integer playerCount) {
        List<ScriptDTO.ScriptResponse> scripts = scriptService.getScriptsByPlayerCount(playerCount);
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }

    /**
     * 根据名称搜索剧本
     * @param name 名称
     * @return 剧本响应DTO列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<ScriptDTO.ScriptResponse>> searchScriptsByName(@RequestParam String name) {
        List<ScriptDTO.ScriptResponse> scripts = scriptService.searchScriptsByName(name);
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }

    /**
     * 根据时长查询剧本
     * @param maxDuration 最大时长
     * @return 剧本响应DTO列表
     */
    @GetMapping("/duration/{maxDuration}")
    public ResponseEntity<List<ScriptDTO.ScriptResponse>> getScriptsByDuration(@PathVariable Integer maxDuration) {
        List<ScriptDTO.ScriptResponse> scripts = scriptService.getScriptsByDuration(maxDuration);
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }
    
//    /**
//     * 生成剧本（异步）
//     * @param request 剧本生成请求
//     * @return 任务ID
//     */
//    @PostMapping("/generate")
//    public ResponseEntity<Map<String, String>> generateScript(@RequestBody GenerateScriptRequest request) {
//        try {
//            String taskId = taskService.submitScriptGenerationTask(
//                request.getScriptName(),
//                request.getDescription(),
//                request.getPlayerCount(),
//                request.getDifficulty().name(),
//                request.getExtraRequirements()
//            );
//            return ResponseEntity.ok(Map.of("taskId", taskId));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to submit script generation task"));
//        }
//    }

    /**
     * 查询任务状态
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
