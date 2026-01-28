package org.jubensha.aijubenshabackend.domain.repository;

import org.jubensha.aijubenshabackend.domain.model.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 场景仓库接口
 */
@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    
    // 根据剧本ID查询场景
    List<Scene> findByScriptId(Long scriptId);
    
    // 根据状态查询场景
    List<Scene> findByStatus(String status);
    
    // 根据剧本ID和状态查询场景
    List<Scene> findByScriptIdAndStatus(Long scriptId, String status);
    
    // 根据剧本ID和顺序查询场景
    List<Scene> findByScriptIdOrderByOrderIndexAsc(Long scriptId);
}
