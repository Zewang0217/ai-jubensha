package org.jubensha.aijubenshabackend.repository.scene;

import org.jubensha.aijubenshabackend.domain.model.Scene;
import org.jubensha.aijubenshabackend.domain.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    
    List<Scene> findByScript(Script script);
    
    List<Scene> findByScriptId(Long scriptId);
    
    List<Scene> findByNameContaining(String name);
    
    List<Scene> findByStatus(String status);
    
    List<Scene> findByScriptIdAndStatus(Long scriptId, String status);
    
    List<Scene> findByScriptIdOrderByOrderIndexAsc(Long scriptId);
}