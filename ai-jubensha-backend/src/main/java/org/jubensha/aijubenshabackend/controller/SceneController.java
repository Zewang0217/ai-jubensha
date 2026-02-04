package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import org.jubensha.aijubenshabackend.models.dto.SceneCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.SceneResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.SceneUpdateDTO;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     *
     * @param sceneCreateDTO 场景创建DTO
     * @return 创建的场景响应DTO
     */
    @PostMapping
    public ResponseEntity<SceneResponseDTO> createScene(@Valid @RequestBody SceneCreateDTO sceneCreateDTO) {
        Scene scene = new Scene();

        // 设置关联的剧本
        scene.setScriptId(sceneCreateDTO.getScriptId());
        scene.setName(sceneCreateDTO.getName());
        scene.setDescription(sceneCreateDTO.getDescription());
        scene.setImageUrl(sceneCreateDTO.getImageUrl());
        scene.setAvailableActions(sceneCreateDTO.getAvailableActions());

        Scene createdScene = sceneService.createScene(scene);
        SceneResponseDTO responseDTO = SceneResponseDTO.fromEntity(createdScene);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * 更新场景
     *
     * @param id             场景ID
     * @param sceneUpdateDTO 场景更新DTO
     * @return 更新后的场景响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<SceneResponseDTO> updateScene(@PathVariable Long id, @Valid @RequestBody SceneUpdateDTO sceneUpdateDTO) {
        Scene scene = new Scene();

        // 设置关联的剧本
        scene.setScriptId(sceneUpdateDTO.getScriptId());
        scene.setName(sceneUpdateDTO.getName());
        scene.setDescription(sceneUpdateDTO.getDescription());
        scene.setImageUrl(sceneUpdateDTO.getImageUrl());
        scene.setAvailableActions(sceneUpdateDTO.getAvailableActions());

        try {
            Scene updatedScene = sceneService.updateScene(id, scene);
            SceneResponseDTO responseDTO = SceneResponseDTO.fromEntity(updatedScene);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除场景
     *
     * @param id 场景ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id) {
        try {
            sceneService.deleteScene(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 根据ID查询场景
     *
     * @param id 场景ID
     * @return 场景响应DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<SceneResponseDTO> getSceneById(@PathVariable Long id) {
        Optional<Scene> scene = sceneService.getSceneById(id);
        return scene.map(value -> {
            SceneResponseDTO responseDTO = SceneResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 查询所有场景
     *
     * @return 场景响应DTO列表
     */
    @GetMapping
    public ResponseEntity<List<SceneResponseDTO>> getAllScenes() {
        List<Scene> scenes = sceneService.getAllScenes();
        List<SceneResponseDTO> responseDTOs = scenes.stream()
                .map(SceneResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据剧本ID查询场景
     *
     * @param scriptId 剧本ID
     * @return 场景响应DTO列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<SceneResponseDTO>> getScenesByScriptId(@PathVariable Long scriptId) {
        List<Scene> scenes = sceneService.getScenesByScriptId(scriptId);
        List<SceneResponseDTO> responseDTOs = scenes.stream()
                .map(SceneResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据剧本ID和名称查询场景
     *
     * @param scriptId 剧本ID
     * @param name     场景名称
     * @return 场景响应DTO列表
     */
    @GetMapping("/script/{scriptId}/name/{name}")
    public ResponseEntity<List<SceneResponseDTO>> getScenesByScriptIdAndName(@PathVariable Long scriptId, @PathVariable String name) {
        List<Scene> scenes = sceneService.getScenesByScriptIdAndName(scriptId, name);
        List<SceneResponseDTO> responseDTOs = scenes.stream()
                .map(SceneResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据名称搜索场景
     *
     * @param name 场景名称
     * @return 场景响应DTO列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<SceneResponseDTO>> searchScenesByName(@RequestParam String name) {
        List<Scene> scenes = sceneService.searchScenesByName(name);
        List<SceneResponseDTO> responseDTOs = scenes.stream()
                .map(SceneResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据描述关键词搜索场景
     *
     * @param keyword 关键词
     * @return 场景响应DTO列表
     */
    @GetMapping("/search/description")
    public ResponseEntity<List<SceneResponseDTO>> searchScenesByDescription(@RequestParam String keyword) {
        List<Scene> scenes = sceneService.searchScenesByDescription(keyword);
        List<SceneResponseDTO> responseDTOs = scenes.stream()
                .map(SceneResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据创建时间范围查询场景
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 场景响应DTO列表
     */
    @GetMapping("/created")
    public ResponseEntity<List<SceneResponseDTO>> getScenesByCreateTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<Scene> scenes = sceneService.getScenesByCreateTimeRange(startTime, endTime);
        List<SceneResponseDTO> responseDTOs = scenes.stream()
                .map(SceneResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 统计剧本中的场景数量
     *
     * @param scriptId 剧本ID
     * @return 场景数量
     */
    @GetMapping("/script/{scriptId}/count")
    public ResponseEntity<Long> countScenesByScriptId(@PathVariable Long scriptId) {
        Long count = sceneService.countScenesByScriptId(scriptId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    /**
     * 更新场景图片
     *
     * @param id       场景ID
     * @param imageUrl 图片URL
     * @return 更新后的场景响应DTO
     */
    @PutMapping("/{id}/image")
    public ResponseEntity<SceneResponseDTO> updateSceneImage(@PathVariable Long id, @RequestParam String imageUrl) {
        try {
            Scene scene = sceneService.updateSceneImage(id, imageUrl);
            SceneResponseDTO responseDTO = SceneResponseDTO.fromEntity(scene);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 更新场景可用动作
     *
     * @param id               场景ID
     * @param availableActions 可用动作
     * @return 更新后的场景响应DTO
     */
    @PutMapping("/{id}/actions")
    public ResponseEntity<SceneResponseDTO> updateSceneActions(@PathVariable Long id, @RequestParam String availableActions) {
        try {
            Scene scene = sceneService.updateSceneActions(id, availableActions);
            SceneResponseDTO responseDTO = SceneResponseDTO.fromEntity(scene);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 批量删除场景
     *
     * @param ids 场景ID列表
     * @return 响应
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteScenesBatch(@RequestParam List<Long> ids) {
        try {
            sceneService.deleteScenesBatch(ids);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}