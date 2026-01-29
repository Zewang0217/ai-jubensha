package org.jubensha.aijubenshabackend.repository.character;

import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    List<Character> findByScript(Script script);
    
    List<Character> findByScriptId(Long scriptId);

    List<Character> findByScriptIdAndIsAi(Long scriptId, Boolean isAi);

    List<Character> findByIsAi(Boolean isAi);
}