package org.jubensha.aijubenshabackend.repository.script;

import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {

    List<Script> findByNameContaining(String name);

    List<Script> findByPlayerCount(Integer playerCount);

    List<Script> findByDifficulty(DifficultyLevel difficulty);

    List<Script> findByDurationLessThanEqual(Integer duration);
}