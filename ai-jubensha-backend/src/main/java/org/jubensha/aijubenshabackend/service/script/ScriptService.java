package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;

import java.util.List;
import java.util.Optional;

public interface ScriptService {
    
    /**
     * 创建新剧本
     */
    Script createScript(Script script);
    
    /**
     * 根据ID获取剧本
     */
    Optional<Script> getScriptById(Long id);
    
    /**
     * 获取所有剧本
     */
    List<Script> getAllScripts();
    
    /**
     * 更新剧本
     */
    Script updateScript(Long id, Script script);
    
    /**
     * 删除剧本
     */
    void deleteScript(Long id);
    
    /**
     * 根据名称搜索剧本
     */
    List<Script> searchScriptsByName(String name);
    
    /**
     * 根据难度级别筛选剧本
     */
    List<Script> getScriptsByDifficulty(DifficultyLevel difficulty);
    
    /**
     * 根据玩家数量筛选剧本
     */
    List<Script> getScriptsByPlayerCount(Integer playerCount);
    
    /**
     * 根据时长筛选剧本
     */
    List<Script> getScriptsByDuration(Integer maxDuration);
    
    /**
     * 生成剧本
     */
    Script generateScript(String scriptName, String description, Integer playerCount, DifficultyLevel difficulty, String extraRequirements);
}