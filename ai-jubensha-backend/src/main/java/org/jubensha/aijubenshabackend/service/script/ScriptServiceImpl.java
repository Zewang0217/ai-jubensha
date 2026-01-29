package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.ai.agent.ScriptGeneratorAgent;
import org.jubensha.aijubenshabackend.ai.service.AIService;
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
    private final AIService aiService;
    
    @Autowired
    public ScriptServiceImpl(ScriptRepository scriptRepository, AIService aiService) {
        this.scriptRepository = scriptRepository;
        this.aiService = aiService;
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
    
    @Override
    public Script generateScript(String scriptName, String description, Integer playerCount, DifficultyLevel difficulty, String extraRequirements) {
        logger.info("Generating script: {}", scriptName);
        
        try {
            // 获取剧本生成Agent
            ScriptGeneratorAgent scriptGeneratorAgent = (ScriptGeneratorAgent) aiService.getScriptGeneratorAgent();
            
            // 设置剧本信息
            scriptGeneratorAgent.setScriptInfo(scriptName, playerCount, difficulty);
            
            // 生成剧本内容
            String generatedDescription = scriptGeneratorAgent.process(extraRequirements);
            
            // 创建剧本实体
            Script script = new Script();
            script.setName(scriptName);
            script.setDescription(generatedDescription); // 始终使用AI生成的内容作为description
            script.setAuthor("AI Generated");
            script.setDifficulty(difficulty);
            script.setDuration(120); // 默认时长2小时
            script.setPlayerCount(playerCount);
//            script.setContent(scriptContent);
            
            // 保存到数据库
            Script savedScript = scriptRepository.save(script);
            logger.info("Script generated and saved: {}", savedScript.getId());
            
            // 重置Agent状态
            scriptGeneratorAgent.reset();
            
            return savedScript;
        } catch (Exception e) {
            logger.error("Error generating script: {}", e.getMessage());
            throw new RuntimeException("Failed to generate script: " + e.getMessage(), e);
        }
    }
}