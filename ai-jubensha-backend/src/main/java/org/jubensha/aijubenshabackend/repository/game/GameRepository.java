package org.jubensha.aijubenshabackend.repository.game;

import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByGameCode(String gameCode);

    List<Game> findByScript(Script script);

    List<Game> findByScriptId(Long scriptId);

    List<Game> findByStatus(GameStatus status);

    List<Game> findByCurrentPhase(GamePhase currentPhase);

    List<Game> findByScriptIdAndStatus(Long scriptId, GameStatus status);
}