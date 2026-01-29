package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.jubensha.aijubenshabackend.repository.script.ScriptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScriptServiceImpl implements ScriptService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScriptServiceImpl.class);
    
    private final ScriptRepository scriptRepository;
    
    @Autowired
    public ScriptServiceImpl(ScriptRepository scriptRepository) {
        this.scriptRepository = scriptRepository;
    }
    
    @Override
    public Script createScript(Script script) {
        logger.info("Creating new script: {}", script.getName());
        return scriptRepository.save(script);
    }
    
    @Override
    public Optional<Script> getScriptById(Long id) {
        logger.info("Getting script by id: {}", id);
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
        Optional<Script> existingScript = scriptRepository.findById(id);
        if (existingScript.isPresent()) {
            Script updatedScript = existingScript.get();
            updatedScript.setName(script.getName());
            updatedScript.setDescription(script.getDescription());
            updatedScript.setAuthor(script.getAuthor());
            updatedScript.setDifficulty(script.getDifficulty());
            updatedScript.setDuration(script.getDuration());
            updatedScript.setPlayerCount(script.getPlayerCount());
            updatedScript.setCoverImage(script.getCoverImage());
            return scriptRepository.save(updatedScript);
        } else {
            throw new IllegalArgumentException("Script not found with id: " + id);
        }
    }
    
    @Override
    public void deleteScript(Long id) {
        logger.info("Deleting script: {}", id);
        scriptRepository.deleteById(id);
    }
    
    @Override
    public List<Script> searchScriptsByName(String name) {
        logger.info("Searching scripts by name: {}", name);
        return scriptRepository.findByNameContaining(name);
    }
    
    @Override
    public List<Script> getScriptsByDifficulty(DifficultyLevel difficulty) {
        logger.info("Getting scripts by difficulty: {}", difficulty);
        return scriptRepository.findByDifficulty(difficulty);
    }
    
    @Override
    public List<Script> getScriptsByPlayerCount(Integer playerCount) {
        logger.info("Getting scripts by player count: {}", playerCount);
        return scriptRepository.findByPlayerCount(playerCount);
    }
    
    @Override
    public List<Script> getScriptsByDuration(Integer maxDuration) {
        logger.info("Getting scripts by duration: {}", maxDuration);
        return scriptRepository.findByDurationLessThanEqual(maxDuration);
    }
}