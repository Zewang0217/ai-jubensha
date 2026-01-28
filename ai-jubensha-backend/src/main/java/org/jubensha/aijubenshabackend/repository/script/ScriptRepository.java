package org.jubensha.aijubenshabackend.repository.script;

import org.jubensha.aijubenshabackend.domain.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {
    
    List<Script> findByTitleContaining(String title);
    
    List<Script> findByPlayerCount(Integer playerCount);
    
    List<Script> findByDifficulty(Integer difficulty);
    
    List<Script> findByDurationLessThanEqual(Integer duration);
    
    List<Script> findByStatus(String status);
    
    List<Script> findByGenre(String genre);
}