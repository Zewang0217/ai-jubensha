package org.jubensha.aijubenshabackend.service.character;

import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.repository.character.CharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        logger.info("Getting characters by script: {}", script.getName());
        return characterRepository.findByScript(script);
    }

    @Override
    public List<Character> getCharactersByScriptId(Long scriptId) {
        logger.info("Getting characters by script id: {}", scriptId);
        return characterRepository.findByScriptId(scriptId);
    }

    @Override
    public List<Character> getCharactersByName(String name) {
        logger.info("Getting characters by name: {}", name);
        return characterRepository.findByName(name);
    }

    @Override
    public Character updateCharacter(Long id, Character character) {
        logger.info("Updating character: {}", id);
        Optional<Character> existingCharacter = characterRepository.findById(id);
        if (existingCharacter.isPresent()) {
            Character updatedCharacter = existingCharacter.get();

            // 只更新非 null 的字段
            if (character.getName() != null) {
                updatedCharacter.setName(character.getName());
            }
            if (character.getDescription() != null) {
                updatedCharacter.setDescription(character.getDescription());
            }
            if (character.getBackgroundStory() != null) {
                updatedCharacter.setBackgroundStory(character.getBackgroundStory());
            }
            if (character.getSecret() != null) {
                updatedCharacter.setSecret(character.getSecret());
            }
            if (character.getAvatarUrl() != null) {
                updatedCharacter.setAvatarUrl(character.getAvatarUrl());
            }

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
}