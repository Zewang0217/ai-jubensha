package org.jubensha.aijubenshabackend.api.controller;

import org.jubensha.aijubenshabackend.domain.model.Scene;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 场景控制器
 */
@RestController
@RequestMapping("/api/scenes")
public class SceneController {
    
    private final SceneService sceneService;
    
    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }
    
    /**
     * 创建场景
     * @param scene 场景实体
     * @return 创建的场景
     */
    @PostMapping
    public ResponseEntity<Scene> createScene(@RequestBody Scene scene) {
        Scene createdScene = sceneService.createScene(scene);
        return new ResponseEntity<>(createdScene, HttpStatus.CREATED);
    }
    
    /**
     * 更新场景
     * @param id 场景ID
     * @param scene 场景实体
     * @return 更新后的场景
     */
    @PutMapping("/{id}")
    public ResponseEntity<Scene> updateScene(@PathVariable Long id, @RequestBody Scene scene) {
        Scene updatedScene = sceneService.updateScene(id, scene);
        return new ResponseEntity<>(updatedScene, HttpStatus.OK);
    }
    
    /**
     * 删除场景
     * @param id 场景ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 根据ID查询场景
     * @param id 场景ID
     * @return 场景实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Scene> getSceneById(@PathVariable Long id) {
        Optional<Scene> scene = sceneService.getSceneById(id);
        return scene.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 查询所有场景
     * @return 场景列表
     */
    @GetMapping
    public ResponseEntity<List<Scene>> getAllScenes() {
        List<Scene> scenes = sceneService.getAllScenes();
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 根据剧本ID查询场景
     * @param scriptId 剧本ID
     * @return 场景列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<Scene>> getScenesByScriptId(@PathVariable Long scriptId) {
        List<Scene> scenes = sceneService.getScenesByScriptId(scriptId);
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 根据状态查询场景
     * @param status 状态
     * @return 场景列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Scene>> getScenesByStatus(@PathVariable String status) {
        List<Scene> scenes = sceneService.getScenesByStatus(status);
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 根据剧本ID和状态查询场景
     * @param scriptId 剧本ID
     * @param status 状态
     * @return 场景列表
     */
    @GetMapping("/script/{scriptId}/status/{status}")
    public ResponseEntity<List<Scene>> getScenesByScriptIdAndStatus(@PathVariable Long scriptId, @PathVariable String status) {
        List<Scene> scenes = sceneService.getScenesByScriptIdAndStatus(scriptId, status);
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 根据剧本ID和顺序查询场景
     * @param scriptId 剧本ID
     * @return 场景列表（按顺序排序）
     */
    @GetMapping("/script/{scriptId}/order")
    public ResponseEntity<List<Scene>> getScenesByScriptIdOrderByOrderIndexAsc(@PathVariable Long scriptId) {
        List<Scene> scenes = sceneService.getScenesByScriptIdOrderByOrderIndexAsc(scriptId);
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
}
