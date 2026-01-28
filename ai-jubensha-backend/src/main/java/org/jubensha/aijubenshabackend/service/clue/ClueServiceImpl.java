package org.jubensha.aijubenshabackend.service.clue;

import org.jubensha.aijubenshabackend.domain.model.Clue;
import org.jubensha.aijubenshabackend.domain.model.Script;
import org.jubensha.aijubenshabackend.repository.clue.ClueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        logger.info("Creating new clue: {}", clue.getTitle());
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
        logger.info("Getting clues by script: {}", script.getTitle());
        return clueRepository.findByScript(script);
    }
    
    @Override
    public List<Clue> getCluesByScriptId(Long scriptId) {
        logger.info("Getting clues by script id: {}", scriptId);
        return clueRepository.findByScriptId(scriptId);
    }
    
    @Override
    public List<Clue> getCluesByType(String type) {
        logger.info("Getting clues by type: {}", type);
        return clueRepository.findByType(type);
    }
    
    @Override
    public List<Clue> getCluesByStatus(String status) {
        logger.info("Getting clues by status: {}", status);
        return clueRepository.findByStatus(status);
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
            updatedClue.setTitle(clue.getTitle());
            updatedClue.setDescription(clue.getDescription());
            updatedClue.setType(clue.getType());
            updatedClue.setStatus(clue.getStatus());
            updatedClue.setImportance(clue.getImportance());
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
}