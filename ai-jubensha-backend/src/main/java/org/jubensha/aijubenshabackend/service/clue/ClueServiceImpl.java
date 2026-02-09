package org.jubensha.aijubenshabackend.service.clue;

import org.jubensha.aijubenshabackend.core.exception.BusinessException;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.repository.clue.ClueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum.CLUE_NOT_FOUND;

@Service
public class ClueServiceImpl implements ClueService {

    private static final Logger logger = LoggerFactory.getLogger(ClueServiceImpl.class);

    private final ClueRepository clueRepository;

    @Autowired
    public ClueServiceImpl(ClueRepository clueRepository) {
        this.clueRepository = clueRepository;
    }

    @Override
    public Clue createClue(Clue clue) {
        logger.info("Creating new clue: {}", clue.getName());
        return clueRepository.save(clue);
    }

    @Override
    public Optional<Clue> getClueById(Long id) {
        logger.info("Getting clue by id: {}", id);
        return clueRepository.findById(id);
    }

    @Override
    public List<Clue> getAllClues() {
        logger.info("Getting all clues");
        return clueRepository.findAll();
    }

    @Override
    public List<Clue> getCluesByScript(Script script) {
        logger.info("Getting clues by script: {}", script.getName());
        return clueRepository.findByScript(script);
    }

    @Override
    public List<Clue> getCluesByScriptId(Long scriptId) {
        logger.info("Getting clues by script id: {}", scriptId);
        return clueRepository.findByScriptId(scriptId);
    }

    @Override
    public List<Clue> getCluesByType(ClueType type) {
        logger.info("Getting clues by type: {}", type);
        return clueRepository.findByType(type);
    }

    @Override
    public List<Clue> getCluesByVisibility(ClueVisibility visibility) {
        logger.info("Getting clues by visibility: {}", visibility);
        return clueRepository.findByVisibility(visibility);
    }

    @Override
    public List<Clue> getCluesByScene(String scene) {
        logger.info("Getting clues by scene: {}", scene);
        return clueRepository.findByScene(scene);
    }

    @Override
    public List<Clue> getImportantClues(Integer importanceThreshold) {
        logger.info("Getting important clues with threshold: {}", importanceThreshold);
        return clueRepository.findByImportanceGreaterThanEqual(importanceThreshold);
    }

    @Override
    public Clue updateClue(Long id, Clue clue) {
        logger.info("Updating clue: {}", id);
        Optional<Clue> existingClue = clueRepository.findById(id);
        if (existingClue.isPresent()) {
            Clue updatedClue = existingClue.get();

            // 只更新非 null 的字段
            if (clue.getName() != null) {
                updatedClue.setName(clue.getName());
            }
            if (clue.getDescription() != null) {
                updatedClue.setDescription(clue.getDescription());
            }
            if (clue.getType() != null) {
                updatedClue.setType(clue.getType());
            }
            if (clue.getVisibility() != null) {
                updatedClue.setVisibility(clue.getVisibility());
            }
            if (clue.getScene() != null) {
                updatedClue.setScene(clue.getScene());
            }
            if (clue.getImageUrl() != null) {
                updatedClue.setImageUrl(clue.getImageUrl());
            }
            if (clue.getImportance() != null) {
                updatedClue.setImportance(clue.getImportance());
            }

            return clueRepository.save(updatedClue);
        } else {
            throw new IllegalArgumentException("Clue not found with id: " + id);
        }
    }

    @Override
    public void deleteClue(Long id) {
        logger.info("Deleting clue: {}", id);
        clueRepository.deleteById(id);
    }

    @Override
    @Transactional
    public String updateClueImage(Long id, String url) {
        logger.info("Updating clue image for id: {}", id);

        try {
            Optional<Clue> clue = clueRepository.findById(id);
            if (clue.isPresent()) {
                clue.get().setImageUrl(url);
                clueRepository.save(clue.get());
                logger.info("Successfully updated clue image for id: {}, new url: {}", id, url);
                return url;
            } else {
                logger.warn("Clue not found for id: {}", id);
                throw new BusinessException(CLUE_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Failed to update clue image for id: {}", id, e);
            throw new BusinessException("更新线索图片失败: " + e.getMessage());
        }
    }
}