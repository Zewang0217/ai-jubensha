package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.core.exception.AppException;
import org.jubensha.aijubenshabackend.models.dto.ScriptDTO;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.jubensha.aijubenshabackend.repository.script.ScriptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScriptServiceImpl implements ScriptService {

    private static final Logger logger = LoggerFactory.getLogger(ScriptServiceImpl.class);

    private final ScriptRepository scriptRepository;

    @Autowired
    public ScriptServiceImpl(ScriptRepository scriptRepository) {
        this.scriptRepository = scriptRepository;
    }

    @Override
    public ScriptDTO.ScriptResponse createScript(ScriptDTO.ScriptCreateRequest request) {
        logger.info("Creating new script: {}", request.getName());
        try {
            // 数据验证已在DTO中通过注解完成

            Script script = new Script();
            script.setName(request.getName());
            script.setDescription(request.getDescription());
            script.setAuthor(request.getAuthor());
            script.setDifficulty(request.getDifficulty());
            script.setDuration(request.getDuration());
            script.setPlayerCount(request.getPlayerCount());
            script.setCoverImage(request.getCoverImage());

            Script savedScript = scriptRepository.save(script);
            logger.info("Script created successfully with ID: {}", savedScript.getId());
            return convertToResponse(savedScript);
        } catch (Exception e) {
            logger.error("Error creating script: {}", e.getMessage(), e);
            throw new AppException("创建剧本失败: " + e.getMessage());
        }
    }

    @Override
    public Optional<ScriptDTO.ScriptResponse> getScriptById(Long id) {
        logger.info("Getting script by id: {}", id);
        if (id == null || id <= 0) {
            logger.warn("Invalid script ID: {}", id);
            return Optional.empty();
        }
        return scriptRepository.findById(id).map(this::convertToResponse);
    }

    @Override
    public List<ScriptDTO.ScriptResponse> getAllScripts() {
        logger.info("Getting all scripts");
        return scriptRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ScriptDTO.ScriptResponse updateScript(Long id, ScriptDTO.ScriptUpdateRequest request) {
        logger.info("Updating script: {}", id);
        try {
            Script existingScript = scriptRepository.findById(id)
                    .orElseThrow(() -> new AppException("剧本不存在，ID: " + id));

            // 更新字段
            existingScript.setName(request.getName());
            existingScript.setDescription(request.getDescription());
            existingScript.setAuthor(request.getAuthor());
            existingScript.setDifficulty(request.getDifficulty());
            existingScript.setDuration(request.getDuration());
            existingScript.setPlayerCount(request.getPlayerCount());
            existingScript.setCoverImage(request.getCoverImage());

            Script updatedScript = scriptRepository.save(existingScript);
            logger.info("Script updated successfully with ID: {}", updatedScript.getId());
            return convertToResponse(updatedScript);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating script: {}", e.getMessage(), e);
            throw new AppException("更新剧本失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteScript(Long id) {
        logger.info("Deleting script: {}", id);
        try {
            if (!scriptRepository.existsById(id)) {
                logger.warn("Script not found for deletion: {}", id);
                throw new AppException("剧本不存在，无法删除，ID: " + id);
            }
            scriptRepository.deleteById(id);
            logger.info("Script deleted successfully: {}", id);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting script: {}", e.getMessage(), e);
            throw new AppException("删除剧本失败: " + e.getMessage());
        }
    }

    @Override
    public List<ScriptDTO.ScriptResponse> searchScriptsByName(String name) {
        logger.info("Searching scripts by name: {}", name);
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Empty search name provided");
            return getAllScripts();
        }
        return scriptRepository.findByNameContaining(name.trim()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScriptDTO.ScriptResponse> getScriptsByDifficulty(DifficultyLevel difficulty) {
        logger.info("Getting scripts by difficulty: {}", difficulty);
        if (difficulty == null) {
            logger.warn("Null difficulty level provided");
            return getAllScripts();
        }
        return scriptRepository.findByDifficulty(difficulty).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScriptDTO.ScriptResponse> getScriptsByPlayerCount(Integer playerCount) {
        logger.info("Getting scripts by player count: {}", playerCount);
        if (playerCount == null || playerCount <= 0) {
            logger.warn("Invalid player count: {}", playerCount);
            return getAllScripts();
        }
        return scriptRepository.findByPlayerCount(playerCount).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScriptDTO.ScriptResponse> getScriptsByDuration(Integer maxDuration) {
        logger.info("Getting scripts by duration: {}", maxDuration);
        if (maxDuration == null || maxDuration <= 0) {
            logger.warn("Invalid duration: {}", maxDuration);
            return getAllScripts();
        }
        return scriptRepository.findByDurationLessThanEqual(maxDuration).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

//    @Override
//    public Script generateScript(String scriptName, String description, Integer playerCount, DifficultyLevel difficulty, String extraRequirements) {
//        logger.info("Generating script: {}", scriptName);
//
//        try {
//            // 获取剧本生成Agent
//            ScriptGeneratorAgent scriptGeneratorAgent = (ScriptGeneratorAgent) aiService.getScriptGeneratorAgent();
//
//            // 设置剧本信息
//            scriptGeneratorAgent.setScriptInfo(scriptName, playerCount, difficulty);
//
//            // 生成剧本内容
//            String generatedDescription = scriptGeneratorAgent.process(extraRequirements);
//
//            // 创建剧本实体
//            Script script = new Script();
//            script.setName(scriptName);
//            script.setDescription(generatedDescription); // 始终使用AI生成的内容作为description
//            script.setAuthor("AI Generated");
//            script.setDifficulty(difficulty);
//            script.setDuration(120); // 默认时长2小时
//            script.setPlayerCount(playerCount);

    /// /            script.setContent(scriptContent);
//
//            // 保存到数据库
//            Script savedScript = scriptRepository.save(script);
//            logger.info("Script generated and saved: {}", savedScript.getId());
//
//            // 重置Agent状态
//            scriptGeneratorAgent.reset();
//
//            return savedScript;
//         catch (Exception e) {
//            logger.error("Error generating script: {}", e.getMessage());
//            throw new RuntimeException("Failed to generate script: " + e.getMessage(), e);
//        }
//    }

    // DTO转换方法
    private ScriptDTO.ScriptResponse convertToResponse(Script script) {
        ScriptDTO.ScriptResponse response = new ScriptDTO.ScriptResponse();
        response.setId(script.getId());
        response.setName(script.getName());
        response.setDescription(script.getDescription());
        response.setAuthor(script.getAuthor());
        response.setDifficulty(script.getDifficulty());
        response.setDuration(script.getDuration());
        response.setPlayerCount(script.getPlayerCount());
        response.setCoverImage(script.getCoverImage());
        response.setCreateTime(script.getCreateTime());
        response.setUpdateTime(script.getUpdateTime());
        return response;
    }
}