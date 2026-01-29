package org.jubensha.aijubenshabackend.controller;

import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 剧本控制器
 */
@RestController
@RequestMapping("/api/scripts")
public class ScriptController {
    
    private final ScriptService scriptService;
    
    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }
    
    /**
     * 创建剧本
     * @param script 剧本实体
     * @return 创建的剧本
     */
    @PostMapping
    public ResponseEntity<Script> createScript(@RequestBody Script script) {
        Script createdScript = scriptService.createScript(script);
        return new ResponseEntity<>(createdScript, HttpStatus.CREATED);
    }
    
    /**
     * 更新剧本
     * @param id 剧本ID
     * @param script 剧本实体
     * @return 更新后的剧本
     */
    @PutMapping("/{id}")
    public ResponseEntity<Script> updateScript(@PathVariable Long id, @RequestBody Script script) {
        Script updatedScript = scriptService.updateScript(id, script);
        return new ResponseEntity<>(updatedScript, HttpStatus.OK);
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
     * @return 剧本实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptById(@PathVariable Long id) {
        Optional<Script> script = scriptService.getScriptById(id);
        return script.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 查询所有剧本
     * @return 剧本列表
     */
    @GetMapping
    public ResponseEntity<List<Script>> getAllScripts() {
        List<Script> scripts = scriptService.getAllScripts();
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }
    
    /**
     * 根据难度查询剧本
     * @param difficulty 难度
     * @return 剧本列表
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<Script>> getScriptsByDifficulty(@PathVariable String difficulty) {
        try {
            DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());
            List<Script> scripts = scriptService.getScriptsByDifficulty(difficultyLevel);
            return new ResponseEntity<>(scripts, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 根据玩家人数查询剧本
     * @param playerCount 玩家人数
     * @return 剧本列表
     */
    @GetMapping("/player-count/{playerCount}")
    public ResponseEntity<List<Script>> getScriptsByPlayerCount(@PathVariable Integer playerCount) {
        List<Script> scripts = scriptService.getScriptsByPlayerCount(playerCount);
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }
    
    /**
     * 根据名称搜索剧本
     * @param name 名称
     * @return 剧本列表
     */
    @GetMapping("/search/{name}")
    public ResponseEntity<List<Script>> searchScriptsByName(@PathVariable String name) {
        List<Script> scripts = scriptService.searchScriptsByName(name);
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }
    
    /**
     * 根据时长查询剧本
     * @param maxDuration 最大时长
     * @return 剧本列表
     */
    @GetMapping("/duration/{maxDuration}")
    public ResponseEntity<List<Script>> getScriptsByDuration(@PathVariable Integer maxDuration) {
        List<Script> scripts = scriptService.getScriptsByDuration(maxDuration);
        return new ResponseEntity<>(scripts, HttpStatus.OK);
    }
}
