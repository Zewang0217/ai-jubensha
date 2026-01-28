package org.jubensha.aijubenshabackend.repository.clue;

import org.jubensha.aijubenshabackend.domain.model.Clue;
import org.jubensha.aijubenshabackend.domain.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClueRepository extends JpaRepository<Clue, Long> {
    
    List<Clue> findByScript(Script script);
    
    List<Clue> findByScriptId(Long scriptId);
    
    List<Clue> findByType(String type);
    
    List<Clue> findByStatus(String status);
    
    List<Clue> findByImportanceGreaterThanEqual(Integer importance);
}