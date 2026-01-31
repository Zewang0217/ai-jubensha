package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.models.dto.ScriptDTO;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;

import java.util.List;
import java.util.Optional;

public interface ScriptService {

    /**
     * 创建新剧本（DTO方式）
     */
    ScriptDTO.ScriptResponse createScript(ScriptDTO.ScriptCreateRequest request);

    /**
     * 根据ID获取剧本（返回Optional包装的DTO）
     */
    Optional<ScriptDTO.ScriptResponse> getScriptById(Long id);

    /**
     * 获取所有剧本（返回DTO列表）
     */
    List<ScriptDTO.ScriptResponse> getAllScripts();

    /**
     * 更新剧本（DTO方式）
     */
    ScriptDTO.ScriptResponse updateScript(Long id, ScriptDTO.ScriptUpdateRequest request);

    /**
     * 删除剧本
     */
    void deleteScript(Long id);

    /**
     * 根据名称搜索剧本（返回DTO列表）
     */
    List<ScriptDTO.ScriptResponse> searchScriptsByName(String name);

    /**
     * 根据难度级别筛选剧本（返回DTO列表）
     */
    List<ScriptDTO.ScriptResponse> getScriptsByDifficulty(DifficultyLevel difficulty);

    /**
     * 根据玩家数量筛选剧本（返回DTO列表）
     */
    List<ScriptDTO.ScriptResponse> getScriptsByPlayerCount(Integer playerCount);

    /**
     * 根据时长筛选剧本（返回DTO列表）
     */
    List<ScriptDTO.ScriptResponse> getScriptsByDuration(Integer maxDuration);

    /**
     * 生成并保存剧本（AI功能）
     */
    //ScriptDTO.ScriptResponse generateAndSaveScript(String prompt);
}
