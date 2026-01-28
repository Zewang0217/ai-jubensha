package org.jubensha.aijubenshabackend.domain.repository;

import org.jubensha.aijubenshabackend.domain.model.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色仓库接口
 */
@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    // 根据剧本ID查询角色
    List<Character> findByScriptId(Long scriptId);
    
    // 根据状态查询角色
    List<Character> findByStatus(String status);
    
    // 根据性别查询角色
    List<Character> findByGender(String gender);
    
    // 根据剧本ID和状态查询角色
    List<Character> findByScriptIdAndStatus(Long scriptId, String status);
}
