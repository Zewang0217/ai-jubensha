package org.jubensha.aijubenshabackend.service.scene;

import org.jubensha.aijubenshabackend.models.dto.SceneDTO;
import org.jubensha.aijubenshabackend.models.entity.Script;

import java.util.List;
import java.util.Optional;

public interface SceneService {
    
    /**
     * 创建新场景
     */
    SceneDTO.SceneResponse createScene(SceneDTO.SceneCreateRequest request);
    
    /**
     * 根据ID获取场景
     */
    Optional<SceneDTO.SceneResponse> getSceneById(Long id);
    
    /**
     * 获取所有场景
     */
    List<SceneDTO.SceneResponse> getAllScenes();
    
    /**
     * 获取剧本的所有场景
     */
    List<SceneDTO.SceneResponse> getScenesByScript(Script script);
    
    /**
     * 根据剧本ID获取场景
     */
    List<SceneDTO.SceneResponse> getScenesByScriptId(Long scriptId);
    
    /**
     * 更新场景
     */
    SceneDTO.SceneResponse updateScene(Long id, SceneDTO.SceneUpdateRequest request);
    
    /**
     * 删除场景
     */
    void deleteScene(Long id);
    
    /**
     * 根据名称搜索场景
     */
    List<SceneDTO.SceneResponse> searchScenesByName(String name);
}