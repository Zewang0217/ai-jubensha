package org.jubensha.aijubenshabackend.repository.game;

import org.jubensha.aijubenshabackend.domain.model.Game;
import org.jubensha.aijubenshabackend.domain.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    Optional<Game> findByGameCode(String gameCode);
    
    List<Game> findByStatus(String status);
    
    List<Game> findByScript(Script script);
    
    List<Game> findByScriptId(Long scriptId);
    
    List<Game> findByCurrentPhase(String currentPhase);
    
    List<Game> findByStatusAndScriptId(String status, Long scriptId);
}