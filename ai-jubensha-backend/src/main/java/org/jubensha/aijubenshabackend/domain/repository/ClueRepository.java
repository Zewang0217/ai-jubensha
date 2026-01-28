package org.jubensha.aijubenshabackend.domain.repository;

import org.jubensha.aijubenshabackend.domain.model.Clue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 线索仓库接口
 */
@Repository
public interface ClueRepository extends JpaRepository<Clue, Long> {
    
    // 根据角色ID查询线索
    List<Clue> findByCharacterId(Long characterId);
    
    // 根据场景ID查询线索
    List<Clue> findBySceneId(Long sceneId);
    
    // 根据状态查询线索
    List<Clue> findByStatus(String status);
    
    // 根据类型查询线索
    List<Clue> findByType(String type);
    
    // 根据重要性查询线索
    List<Clue> findByImportance(Integer importance);
    
    // 根据场景ID和状态查询线索
    List<Clue> findBySceneIdAndStatus(Long sceneId, String status);
    
    // 根据角色ID和状态查询线索
    List<Clue> findByCharacterIdAndStatus(Long characterId, String status);
}
