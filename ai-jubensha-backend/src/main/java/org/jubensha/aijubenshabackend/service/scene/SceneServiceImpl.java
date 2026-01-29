package org.jubensha.aijubenshabackend.service.scene;

import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.repository.scene.SceneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SceneServiceImpl implements SceneService {
    
    private static final Logger logger = LoggerFactory.getLogger(SceneServiceImpl.class);
    
    private final SceneRepository sceneRepository;
    
    @Autowired
    public SceneServiceImpl(SceneRepository sceneRepository) {
        this.sceneRepository = sceneRepository;
    }
    
    @Override
    public Scene createScene(Scene scene) {
        logger.info("Creating new scene: {}", scene.getName());
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
        Optional<Scene> existingScene = sceneRepository.findById(id);
        if (existingScene.isPresent()) {
            Scene updatedScene = existingScene.get();
            updatedScene.setName(scene.getName());
            updatedScene.setDescription(scene.getDescription());
            updatedScene.setImage(scene.getImage());
            return sceneRepository.save(updatedScene);
        } else {
            throw new IllegalArgumentException("Scene not found with id: " + id);
        }
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
}