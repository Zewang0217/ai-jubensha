package org.jubensha.aijubenshabackend.repository.player;

import org.jubensha.aijubenshabackend.domain.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByUsername(String username);
    
    Optional<Player> findByEmail(String email);
    
    List<Player> findByStatus(String status);
    
    List<Player> findByRole(String role);
    
    List<Player> findByStatusAndRole(String status, String role);
    
    List<Player> findByNicknameContaining(String nickname);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}