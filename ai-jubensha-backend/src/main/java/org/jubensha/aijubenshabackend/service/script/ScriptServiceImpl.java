package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.core.exception.AppException;
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
    public Script createScript(Script script) {
        logger.info("Saving script: {}", script.getName());
        try {
            Script savedScript = scriptRepository.save(script);
            logger.info("Script saved successfully with ID: {}", savedScript.getId());
            return savedScript;
        } catch (Exception e) {
            logger.error("Error saving script: {}", e.getMessage(), e);
            throw new AppException("保存剧本失败: " + e.getMessage());
        }
    }

    @Override
    public Optional<Script> getScriptById(Long id) {
        logger.info("Getting script by id: {}", id);
        if (id == null || id <= 0) {
            logger.warn("Invalid script ID: {}", id);
            return Optional.empty();
        }
        return scriptRepository.findById(id);
    }

    @Override
    public List<Script> getAllScripts() {
        logger.info("Getting all scripts");
        return scriptRepository.findAll();
    }

    @Override
    public Script updateScript(Long id, Script script) {
        logger.info("Updating script: {}", id);
        try {
            Script existingScript = scriptRepository.findById(id)
                    .orElseThrow(() -> new AppException("剧本不存在，ID: " + id));

            // 更新字段
            if (script.getName() != null && !script.getName().trim().isEmpty()) {
                existingScript.setName(script.getName());
            }
            if (script.getDescription() != null) {
                existingScript.setDescription(script.getDescription());
            }
            if (script.getAuthor() != null) {
                existingScript.setAuthor(script.getAuthor());
            }
            if (script.getDifficulty() != null) {
                existingScript.setDifficulty(script.getDifficulty());
            }
            if (script.getDuration() != null && script.getDuration() > 0) {
                existingScript.setDuration(script.getDuration());
            }
            if (script.getPlayerCount() != null && script.getPlayerCount() > 0) {
                existingScript.setPlayerCount(script.getPlayerCount());
            }
            if (script.getCoverImageUrl() != null) {
                existingScript.setCoverImageUrl(script.getCoverImageUrl());
            }
            if (script.getTimeline() != null) {
                existingScript.setTimeline(script.getTimeline());
            }

            Script updatedScript = scriptRepository.save(existingScript);
            logger.info("Script updated successfully with ID: {}", updatedScript.getId());
            return updatedScript;
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
    public List<Script> getScriptsByAuthor(String author) {
        logger.info("Getting scripts by author: {}", author);
        // 这里可以根据需要添加具体的查询逻辑
        return getAllScripts();
    }

    @Override
    public List<Script> getScriptsByDifficulty(DifficultyLevel difficulty) {
        logger.info("Getting scripts by difficulty: {}", difficulty);
        if (difficulty == null) {
            logger.warn("Null difficulty level provided");
            return getAllScripts();
        }
        return scriptRepository.findByDifficulty(difficulty);
    }

    @Override
    public List<Script> getScriptsByPlayerCount(Integer playerCount) {
        logger.info("Getting scripts by player count: {}", playerCount);
        if (playerCount == null || playerCount <= 0) {
            logger.warn("Invalid player count: {}", playerCount);
            return getAllScripts();
        }
        return scriptRepository.findByPlayerCount(playerCount);
    }

    @Override
    public List<Script> getScriptsByDuration(Integer duration) {
        logger.info("Getting scripts by duration: {}", duration);
        if (duration == null || duration <= 0) {
            logger.warn("Invalid duration: {}", duration);
            return getAllScripts();
        }
        return scriptRepository.findByDurationLessThanEqual(duration);
    }

    @Override
    public List<Script> searchScriptsByName(String name) {
        logger.info("Searching scripts by name: {}", name);
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Empty search name provided");
            return getAllScripts();
        }
        return scriptRepository.findByNameContaining(name.trim());
    }

    @Override
    public List<Script> searchScriptsByDescription(String keyword) {
        logger.info("Searching scripts by description keyword: {}", keyword);
        // 这里可以添加具体的实现逻辑，目前返回所有剧本
        return getAllScripts();
    }

    @Override
    public List<Script> getScriptsByDifficultyAndPlayerCount(DifficultyLevel difficulty, Integer playerCount) {
        logger.info("Getting scripts by difficulty: {} and player count: {}", difficulty, playerCount);
        // 这里可以根据需要添加更复杂的查询逻辑
        List<Script> scripts = getScriptsByDifficulty(difficulty);
        if (playerCount != null && playerCount > 0) {
            return scripts.stream()
                    .filter(script -> script.getPlayerCount() != null && script.getPlayerCount().equals(playerCount))
                    .toList();
        }
        return scripts;
    }

    @Override
    public List<Script> getScriptsByCreateTimeRange(String startTime, String endTime) {
        logger.info("Getting scripts by create time range: {} to {}", startTime, endTime);
        // 这里可以添加具体的实现逻辑，目前返回所有剧本
        return getAllScripts();
    }

    @Override
    public Script updateScriptCoverImage(Long id, String coverImageUrl) {
        logger.info("Updating script cover image: {}", id);
        Script existingScript = scriptRepository.findById(id)
                .orElseThrow(() -> new AppException("剧本不存在: " + id));
        existingScript.setCoverImageUrl(coverImageUrl);
        return scriptRepository.save(existingScript);
    }

    @Override
    public Script updateScriptTimeline(Long id, String timeline) {
        logger.info("Updating script timeline: {}", id);
        Script existingScript = scriptRepository.findById(id)
                .orElseThrow(() -> new AppException("剧本不存在: " + id));
        existingScript.setTimeline(timeline);
        return scriptRepository.save(existingScript);
    }

    @Override
    public void deleteScriptsBatch(List<Long> ids) {
        logger.info("Deleting scripts batch: {}", ids);
        if (ids != null && !ids.isEmpty()) {
            scriptRepository.deleteAllById(ids);
        }
    }
}