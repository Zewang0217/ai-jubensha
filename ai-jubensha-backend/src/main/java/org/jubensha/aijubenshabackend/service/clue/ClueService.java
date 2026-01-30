package org.jubensha.aijubenshabackend.service.clue;

import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;

import java.util.List;
import java.util.Optional;

public interface ClueService {
    
    /**
     * 创建新线索
     */
    Clue createClue(Clue clue);
    
    /**
     * 根据ID获取线索
     */
    Optional<Clue> getClueById(Long id);
    
    /**
     * 获取所有线索
     */
    List<Clue> getAllClues();
    
    /**
     * 获取剧本的所有线索
     */
    List<Clue> getCluesByScript(Script script);
    
    /**
     * 根据剧本ID获取线索
     */
    List<Clue> getCluesByScriptId(Long scriptId);
    
    /**
     * 根据类型获取线索
     */
    List<Clue> getCluesByType(ClueType type);
    
    /**
     * 根据可见性获取线索
     */
    List<Clue> getCluesByVisibility(ClueVisibility visibility);
    
    /**
     * 根据场景获取线索
     */
    List<Clue> getCluesByScene(String scene);
    
    /**
     * 获取重要线索
     * @param importanceThreshold 重要度阈值
     */
    List<Clue> getImportantClues(Integer importanceThreshold);
    
    /**
     * 更新线索
     */
    Clue updateClue(Long id, Clue clue);
    
    /**
     * 删除线索
     */
    void deleteClue(Long id);
}