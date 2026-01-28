package org.jubensha.aijubenshabackend.domain.repository;

import org.jubensha.aijubenshabackend.domain.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧本仓库接口
 */
@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {
    
    // 根据状态查询剧本
    List<Script> findByStatus(String status);
    
    // 根据类型查询剧本
    List<Script> findByGenre(String genre);
    
    // 根据难度查询剧本
    List<Script> findByDifficulty(Integer difficulty);
    
    // 根据玩家人数查询剧本
    List<Script> findByPlayerCount(Integer playerCount);
    
    // 根据标题模糊查询剧本
    List<Script> findByTitleContaining(String title);
}
