package org.jubensha.aijubenshabackend.service.scene;

import org.jubensha.aijubenshabackend.models.dto.SceneDTO;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.repository.scene.SceneRepository;
import org.jubensha.aijubenshabackend.repository.script.ScriptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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
    public SceneDTO.SceneResponse createScene(SceneDTO.SceneCreateRequest request) {
        logger.info("Creating new scene: {}", request.getName());
        
        // 验证剧本是否存在
        Script script = scriptRepository.findById(request.getScriptId())
                .orElseThrow(() -> new IllegalArgumentException("剧本不存在: " + request.getScriptId()));
        
        Scene scene = new Scene();
        scene.setScript(script);
        scene.setName(request.getName());
        scene.setDescription(request.getDescription());
        scene.setImage(request.getImage());
        scene.setAvailableActions(request.getAvailableActions());
        
        Scene savedScene = sceneRepository.save(scene);
        return convertToResponse(savedScene);
    }
    
    @Override
    public Optional<SceneDTO.SceneResponse> getSceneById(Long id) {
        logger.info("Getting scene by id: {}", id);
        return sceneRepository.findById(id)
                .map(this::convertToResponse);
    }
    
    @Override
    public List<SceneDTO.SceneResponse> getAllScenes() {
        logger.info("Getting all scenes");
        return sceneRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SceneDTO.SceneResponse> getScenesByScript(Script script) {
        logger.info("Getting scenes by script: {}", script.getName());
        return sceneRepository.findByScript(script).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SceneDTO.SceneResponse> getScenesByScriptId(Long scriptId) {
        logger.info("Getting scenes by script id: {}", scriptId);
        return sceneRepository.findByScriptId(scriptId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public SceneDTO.SceneResponse updateScene(Long id, SceneDTO.SceneUpdateRequest request) {
        logger.info("Updating scene: {}", id);
        Scene existingScene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("场景不存在: " + id));
        
        // 验证剧本是否存在
        Script script = scriptRepository.findById(request.getScriptId())
                .orElseThrow(() -> new IllegalArgumentException("剧本不存在: " + request.getScriptId()));
        
        existingScene.setScript(script);
        existingScene.setName(request.getName());
        existingScene.setDescription(request.getDescription());
        existingScene.setImage(request.getImage());
        existingScene.setAvailableActions(request.getAvailableActions());
        
        Scene updatedScene = sceneRepository.save(existingScene);
        return convertToResponse(updatedScene);
    }
    
    @Override
    public void deleteScene(Long id) {
        logger.info("Deleting scene: {}", id);
        sceneRepository.deleteById(id);
    }
    
    // DTO转换方法
    private SceneDTO.SceneResponse convertToResponse(Scene scene) {
        SceneDTO.SceneResponse response = new SceneDTO.SceneResponse();
        response.setId(scene.getId());
        response.setScriptId(scene.getScript().getId());
        response.setScriptName(scene.getScript().getName());
        response.setName(scene.getName());
        response.setDescription(scene.getDescription());
        response.setImage(scene.getImage());
        response.setAvailableActions(scene.getAvailableActions());
        response.setCreateTime(scene.getCreateTime());
        return response;
    }

    
    @Override
    public List<SceneDTO.SceneResponse> searchScenesByName(String name) {
        logger.info("Searching scenes by name: {}", name);
        return sceneRepository.findByNameContaining(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}