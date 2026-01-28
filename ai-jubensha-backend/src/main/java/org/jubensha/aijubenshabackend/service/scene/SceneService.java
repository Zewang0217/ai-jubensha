package org.jubensha.aijubenshabackend.service.scene;

import org.jubensha.aijubenshabackend.domain.model.Scene;
import org.jubensha.aijubenshabackend.domain.model.Script;

import java.util.List;
import java.util.Optional;

public interface SceneService {
    
    /**
     * 创建新场景
     */
    Scene createScene(Scene scene);
    
    /**
     * 根据ID获取场景
     */
    Optional<Scene> getSceneById(Long id);
    
    /**
     * 获取所有场景
     */
    List<Scene> getAllScenes();
    
    /**
     * 获取剧本的所有场景
     */
    List<Scene> getScenesByScript(Script script);
    
    /**
     * 根据剧本ID获取场景
     */
    List<Scene> getScenesByScriptId(Long scriptId);
    
    /**
     * 更新场景
     */
    Scene updateScene(Long id, Scene scene);
    
    /**
     * 删除场景
     */
    void deleteScene(Long id);
    
    /**
     * 根据名称搜索场景
     */
    List<Scene> searchScenesByName(String name);
    
    /**
     * 根据状态获取场景
     */
    List<Scene> getScenesByStatus(String status);
    
    /**
     * 根据剧本ID和状态获取场景
     */
    List<Scene> getScenesByScriptIdAndStatus(Long scriptId, String status);
    
    /**
     * 根据剧本ID按顺序索引升序获取场景
     */
    List<Scene> getScenesByScriptIdOrderByOrderIndexAsc(Long scriptId);
}