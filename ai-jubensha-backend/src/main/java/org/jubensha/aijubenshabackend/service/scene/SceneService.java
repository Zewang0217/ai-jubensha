package org.jubensha.aijubenshabackend.service.scene;

import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.entity.Script;

import java.util.List;
import java.util.Optional;

public interface SceneService {
    
    /**
     * 保存场景
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
     * 根据剧本ID和名称查询场景
     */
    List<Scene> getScenesByScriptIdAndName(Long scriptId, String name);

    /**
     * 根据描述关键词搜索场景
     */
    List<Scene> searchScenesByDescription(String keyword);

    /**
     * 根据创建时间范围查询场景
     */
    List<Scene> getScenesByCreateTimeRange(String startTime, String endTime);

    /**
     * 统计剧本中的场景数量
     */
    Long countScenesByScriptId(Long scriptId);

    /**
     * 更新场景图片
     */
    Scene updateSceneImage(Long id, String imageUrl);

    /**
     * 更新场景可用动作
     */
    Scene updateSceneActions(Long id, String availableActions);

    /**
     * 批量删除场景
     */
    void deleteScenesBatch(List<Long> ids);
}