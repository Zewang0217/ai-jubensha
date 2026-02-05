package org.jubensha.aijubenshabackend.service.script;

import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;

import java.util.List;
import java.util.Optional;

public interface ScriptService {

    /**
     * 保存剧本
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
     * 根据作者查询剧本
     */
    List<Script> getScriptsByAuthor(String author);

    /**
     * 根据难度查询剧本
     */
    List<Script> getScriptsByDifficulty(DifficultyLevel difficulty);

    /**
     * 根据玩家人数查询剧本
     */
    List<Script> getScriptsByPlayerCount(Integer playerCount);

    /**
     * 根据时长查询剧本
     */
    List<Script> getScriptsByDuration(Integer duration);

    /**
     * 根据名称搜索剧本
     */
    List<Script> searchScriptsByName(String name);

    /**
     * 根据描述关键词搜索剧本
     */
    List<Script> searchScriptsByDescription(String keyword);

    /**
     * 根据难度和玩家人数查询剧本
     */
    List<Script> getScriptsByDifficultyAndPlayerCount(DifficultyLevel difficulty, Integer playerCount);

    /**
     * 根据创建时间范围查询剧本
     */
    List<Script> getScriptsByCreateTimeRange(String startTime, String endTime);

    /**
     * 更新剧本封面图片
     */
    Script updateScriptCoverImage(Long id, String coverImageUrl);

    /**
     * 更新剧本时间线
     */
    Script updateScriptTimeline(Long id, String timeline);

    /**
     * 批量删除剧本
     */
    void deleteScriptsBatch(List<Long> ids);
}
