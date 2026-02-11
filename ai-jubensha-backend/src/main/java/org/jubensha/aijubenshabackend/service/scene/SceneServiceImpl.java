package org.jubensha.aijubenshabackend.service.scene;

import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.repository.scene.SceneRepository;
import org.jubensha.aijubenshabackend.repository.script.ScriptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SceneServiceImpl implements SceneService {

    private static final Logger logger = LoggerFactory.getLogger(SceneServiceImpl.class);

    private final SceneRepository sceneRepository;
    private final ScriptRepository scriptRepository;

    @Autowired
    public SceneServiceImpl(SceneRepository sceneRepository, ScriptRepository scriptRepository) {
        this.sceneRepository = sceneRepository;
        this.scriptRepository = scriptRepository;
    }

    @Override
    public Scene createScene(Scene scene) {
        logger.info("Saving scene: {}", scene.getName());

        // 验证剧本是否存在
        if (scene.getScript() != null && scene.getScript().getId() != null) {
            Script script = scriptRepository.findById(scene.getScript().getId())
                    .orElseThrow(() -> new IllegalArgumentException("剧本不存在: " + scene.getScript().getId()));
            scene.setScript(script);
        }

        return sceneRepository.save(scene);
    }

    @Override
    public Optional<Scene> getSceneById(Long id) {
        logger.info("Getting scene by id: {}", id);
        return sceneRepository.findById(id);
    }

    @Override
    public List<Scene> getAllScenes() {
        logger.info("Getting all scenes");
        return sceneRepository.findAll();
    }

    @Override
    public List<Scene> getScenesByScript(Script script) {
        logger.info("Getting scenes by script: {}", script.getName());
        return sceneRepository.findByScript(script);
    }

    @Override
    public List<Scene> getScenesByScriptId(Long scriptId) {
        logger.info("Getting scenes by script id: {}", scriptId);
        return sceneRepository.findByScriptId(scriptId);
    }

    @Override
    public Scene updateScene(Long id, Scene scene) {
        logger.info("Updating scene: {}", id);
        Scene existingScene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("场景不存在: " + id));

        // 验证剧本是否存在
        if (scene.getScript() != null && scene.getScript().getId() != null) {
            Script script = scriptRepository.findById(scene.getScript().getId())
                    .orElseThrow(() -> new IllegalArgumentException("剧本不存在: " + scene.getScript().getId()));
            existingScene.setScript(script);
        }

        // 更新允许的字段
        if (scene.getName() != null && !scene.getName().trim().isEmpty()) {
            existingScene.setName(scene.getName());
        }

        if (scene.getDescription() != null && !scene.getDescription().trim().isEmpty()) {
            existingScene.setDescription(scene.getDescription());
        }

        if (scene.getImageUrl() != null) {
            existingScene.setImageUrl(scene.getImageUrl());
        }

        if (scene.getAvailableActions() != null) {
            existingScene.setAvailableActions(scene.getAvailableActions());
        }

        return sceneRepository.save(existingScene);
    }

    @Override
    public void deleteScene(Long id) {
        logger.info("Deleting scene: {}", id);
        sceneRepository.deleteById(id);
    }

    @Override
    public List<Scene> searchScenesByName(String name) {
        logger.info("Searching scenes by name: {}", name);
        return sceneRepository.findByNameContaining(name);
    }

    @Override
    public List<Scene> getScenesByScriptIdAndName(Long scriptId, String name) {
        logger.info("Getting scenes by script id: {} and name: {}", scriptId, name);
        // 这里可以根据需要添加更复杂的查询逻辑
        List<Scene> scenes = sceneRepository.findByScriptId(scriptId);
        if (name != null && !name.trim().isEmpty()) {
            return scenes.stream()
                    .filter(scene -> scene.getName().contains(name.trim()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return scenes;
    }

    @Override
    public List<Scene> searchScenesByDescription(String keyword) {
        logger.info("Searching scenes by description keyword: {}", keyword);
        // 这里可以添加具体的实现逻辑，目前返回所有场景
        return getAllScenes();
    }

    @Override
    public List<Scene> getScenesByCreateTimeRange(String startTime, String endTime) {
        logger.info("Getting scenes by create time range: {} to {}", startTime, endTime);
        // 这里可以添加具体的实现逻辑，目前返回所有场景
        return getAllScenes();
    }

    @Override
    public Long countScenesByScriptId(Long scriptId) {
        logger.info("Counting scenes by script id: {}", scriptId);
        return (long) sceneRepository.findByScriptId(scriptId).size();
    }

    @Override
    public Scene updateSceneImage(Long id, String imageUrl) {
        logger.info("Updating scene image: {}", id);
        Scene existingScene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("场景不存在: " + id));
        existingScene.setImageUrl(imageUrl);
        return sceneRepository.save(existingScene);
    }

    @Override
    public Scene updateSceneActions(Long id, String availableActions) {
        logger.info("Updating scene actions: {}", id);
        Scene existingScene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("场景不存在: " + id));
        existingScene.setAvailableActions(availableActions);
        return sceneRepository.save(existingScene);
    }

    @Override
    public void deleteScenesBatch(List<Long> ids) {
        logger.info("Deleting scenes batch: {}", ids);
        if (ids != null && !ids.isEmpty()) {
            sceneRepository.deleteAllById(ids);
        }
    }
}