package org.jubensha.aijubenshabackend.repository.character;

import org.jubensha.aijubenshabackend.domain.model.Character;
import org.jubensha.aijubenshabackend.domain.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    List<Character> findByScript(Script script);
    
    List<Character> findByScriptId(Long scriptId);
    
    List<Character> findByStatus(String status);
    
    List<Character> findByGender(String gender);
    
    List<Character> findByScriptIdAndStatus(Long scriptId, String status);
}