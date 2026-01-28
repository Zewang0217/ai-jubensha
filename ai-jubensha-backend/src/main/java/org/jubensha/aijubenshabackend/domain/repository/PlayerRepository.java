package org.jubensha.aijubenshabackend.domain.repository;

import org.jubensha.aijubenshabackend.domain.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 玩家仓库接口
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    // 根据用户名查询玩家
    Optional<Player> findByUsername(String username);
    
    // 根据邮箱查询玩家
    Optional<Player> findByEmail(String email);
    
    // 根据状态查询玩家
    List<Player> findByStatus(String status);
    
    // 根据角色查询玩家
    List<Player> findByRole(String role);
    
    // 根据状态和角色查询玩家
    List<Player> findByStatusAndRole(String status, String role);
}
