package org.jubensha.aijubenshabackend.repository.clue;

import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClueRepository extends JpaRepository<Clue, Long> {

    List<Clue> findByScript(Script script);

    List<Clue> findByScriptId(Long scriptId);

    List<Clue> findByType(ClueType type);

    List<Clue> findByVisibility(ClueVisibility visibility);

    List<Clue> findByImportanceGreaterThanEqual(Integer importance);

    List<Clue> findByScene(String scene);
}