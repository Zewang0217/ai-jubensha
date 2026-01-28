package org.jubensha.aijubenshabackend.service.character;

import org.jubensha.aijubenshabackend.domain.model.Character;
import org.jubensha.aijubenshabackend.domain.model.Script;
import org.jubensha.aijubenshabackend.repository.character.CharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CharacterServiceImpl implements CharacterService {
    
    private static final Logger logger = LoggerFactory.getLogger(CharacterServiceImpl.class);
    
    private final CharacterRepository characterRepository;
    
    @Autowired
    public CharacterServiceImpl(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }
    
    @Override
    public Character createCharacter(Character character) {
        logger.info("Creating new character: {}", character.getName());
        return characterRepository.save(character);
    }
    
    @Override
    public Optional<Character> getCharacterById(Long id) {
        logger.info("Getting character by id: {}", id);
        return characterRepository.findById(id);
    }
    
    @Override
    public List<Character> getAllCharacters() {
        logger.info("Getting all characters");
        return characterRepository.findAll();
    }
    
    @Override
    public List<Character> getCharactersByScript(Script script) {
        logger.info("Getting characters by script: {}", script.getTitle());
        return characterRepository.findByScript(script);
    }
    
    @Override
    public List<Character> getAICharactersByScript(Script script) {
        logger.info("Getting AI characters by script: {}", script.getTitle());
        return new ArrayList<>();
    }
    
    @Override
    public List<Character> getAICharacters() {
        logger.info("Getting AI characters");
        return new ArrayList<>();
    }
    
    @Override
    public List<Character> getCharactersByScriptId(Long scriptId) {
        logger.info("Getting characters by script id: {}", scriptId);
        return characterRepository.findByScriptId(scriptId);
    }
    
    @Override
    public Character updateCharacter(Long id, Character character) {
        logger.info("Updating character: {}", id);
        Optional<Character> existingCharacter = characterRepository.findById(id);
        if (existingCharacter.isPresent()) {
            Character updatedCharacter = existingCharacter.get();
            updatedCharacter.setName(character.getName());
            updatedCharacter.setDescription(character.getDescription());
            updatedCharacter.setBackground(character.getBackground());
            updatedCharacter.setSecret(character.getSecret());
            updatedCharacter.setAvatarUrl(character.getAvatarUrl());
            return characterRepository.save(updatedCharacter);
        } else {
            throw new IllegalArgumentException("Character not found with id: " + id);
        }
    }
    
    @Override
    public void deleteCharacter(Long id) {
        logger.info("Deleting character: {}", id);
        characterRepository.deleteById(id);
    }
    
    @Override
    public List<Character> getCharactersByStatus(String status) {
        logger.info("Getting characters by status: {}", status);
        return characterRepository.findByStatus(status);
    }
    
    @Override
    public List<Character> getCharactersByGender(String gender) {
        logger.info("Getting characters by gender: {}", gender);
        return characterRepository.findByGender(gender);
    }
    
    @Override
    public List<Character> getCharactersByScriptIdAndStatus(Long scriptId, String status) {
        logger.info("Getting characters by script id: {} and status: {}", scriptId, status);
        return characterRepository.findByScriptIdAndStatus(scriptId, status);
    }
}