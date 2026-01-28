package org.jubensha.aijubenshabackend.service.clue;

import org.jubensha.aijubenshabackend.domain.model.Clue;
import org.jubensha.aijubenshabackend.domain.model.Script;

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
    List<Clue> getCluesByType(String type);
    
    /**
     * 根据状态获取线索
     */
    List<Clue> getCluesByStatus(String status);
    
    /**
     * 获取重要线索
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