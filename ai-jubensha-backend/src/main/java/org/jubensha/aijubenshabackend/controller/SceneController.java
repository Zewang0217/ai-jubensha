package org.jubensha.aijubenshabackend.controller;

import org.jubensha.aijubenshabackend.models.dto.SceneDTO;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
     * @param request 场景创建请求DTO
     * @return 创建的场景响应DTO
     */
    @PostMapping
    public ResponseEntity<SceneDTO.SceneResponse> createScene(
            @Valid @RequestBody SceneDTO.SceneCreateRequest request) {
        SceneDTO.SceneResponse response = sceneService.createScene(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * 更新场景
     * @param id 场景ID
     * @param request 场景更新请求DTO
     * @return 更新后的场景响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<SceneDTO.SceneResponse> updateScene(@PathVariable Long id,
                                               @Valid @RequestBody SceneDTO.SceneUpdateRequest request) {
        SceneDTO.SceneResponse response = sceneService.updateScene(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
     * @return 场景详细信息DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<SceneDTO.SceneResponse> getSceneById(@PathVariable Long id) {
        Optional<SceneDTO.SceneResponse> scene = sceneService.getSceneById(id);
        return scene.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 查询所有场景
     * @return 场景详细列表
     */
    @GetMapping
    public ResponseEntity<List<SceneDTO.SceneResponse>> getAllScenes() {
        List<SceneDTO.SceneResponse> scenes = sceneService.getAllScenes();
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 根据剧本ID查询场景
     * @param scriptId 剧本ID
     * @return 场景列表响应DTO
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<SceneDTO.SceneResponse>> getScenesByScriptId(@PathVariable Long scriptId) {
        List<SceneDTO.SceneResponse> scenes = sceneService.getScenesByScriptId(scriptId);
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 根据名称搜索场景
     * @param name 场景名称
     * @return 场景列表响应DTO
     */
    @GetMapping("/search")
    public ResponseEntity<List<SceneDTO.SceneResponse>> searchScenesByName(@RequestParam String name) {
        List<SceneDTO.SceneResponse> scenes = sceneService.searchScenesByName(name);
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }

}