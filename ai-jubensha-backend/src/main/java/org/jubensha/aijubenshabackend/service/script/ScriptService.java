package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.domain.model.Script;

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
    List<Script> getScriptsByDifficulty(String difficulty);
    
    /**
     * 根据玩家数量筛选剧本
     */
    List<Script> getScriptsByPlayerCount(Integer playerCount);
    
    /**
     * 根据时长筛选剧本
     */
    List<Script> getScriptsByDuration(Integer maxDuration);
    
    /**
     * 根据状态获取剧本
     */
    List<Script> getScriptsByStatus(String status);
    
    /**
     * 根据类型获取剧本
     */
    List<Script> getScriptsByGenre(String genre);
    
    /**
     * 根据标题包含的内容搜索剧本
     */
    List<Script> getScriptsByTitleContaining(String title);
}