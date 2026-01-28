package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.domain.model.Script;
import org.jubensha.aijubenshabackend.repository.script.ScriptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        logger.info("Creating new script: {}", script.getTitle());
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
            updatedScript.setTitle(script.getTitle());
            updatedScript.setDescription(script.getDescription());
            updatedScript.setAuthor(script.getAuthor());
            updatedScript.setDifficulty(script.getDifficulty());
            updatedScript.setDuration(script.getDuration());
            updatedScript.setPlayerCount(script.getPlayerCount());
            updatedScript.setCoverUrl(script.getCoverUrl());
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
        return scriptRepository.findByTitleContaining(name);
    }
    
    @Override
    public List<Script> getScriptsByDifficulty(String difficulty) {
        logger.info("Getting scripts by difficulty: {}", difficulty);
        try {
            Integer difficultyInt = Integer.parseInt(difficulty);
            return scriptRepository.findByDifficulty(difficultyInt);
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
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
    
    @Override
    public List<Script> getScriptsByStatus(String status) {
        logger.info("Getting scripts by status: {}", status);
        return scriptRepository.findByStatus(status);
    }
    
    @Override
    public List<Script> getScriptsByGenre(String genre) {
        logger.info("Getting scripts by genre: {}", genre);
        return scriptRepository.findByGenre(genre);
    }
    
    @Override
    public List<Script> getScriptsByTitleContaining(String title) {
        logger.info("Getting scripts by title containing: {}", title);
        return scriptRepository.findByTitleContaining(title);
    }
}