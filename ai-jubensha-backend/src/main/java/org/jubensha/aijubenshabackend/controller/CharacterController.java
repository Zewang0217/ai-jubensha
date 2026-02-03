package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import org.jubensha.aijubenshabackend.models.dto.CharacterCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.CharacterResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.CharacterUpdateDTO;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/characters")
public class CharacterController {
    
    private final CharacterService characterService;
    
    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }
    
    /**
     * 创建角色
     * @param characterCreateDTO 角色创建DTO
     * @return 创建的角色响应DTO
     */
    @PostMapping
    public ResponseEntity<CharacterResponseDTO> createCharacter(@Valid @RequestBody CharacterCreateDTO characterCreateDTO) {
        Character character = new Character();
        character.setScriptId(characterCreateDTO.getScriptId());
        character.setName(characterCreateDTO.getName());
        character.setDescription(characterCreateDTO.getDescription());
        character.setBackgroundStory(characterCreateDTO.getBackgroundStory());
        character.setSecret(characterCreateDTO.getSecret());
        character.setAvatarUrl(characterCreateDTO.getAvatarUrl());
        
        Character createdCharacter = characterService.createCharacter(character);
        CharacterResponseDTO responseDTO = CharacterResponseDTO.fromEntity(createdCharacter);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
    
    /**
     * 更新角色
     * @param id 角色ID
     * @param characterUpdateDTO 角色更新DTO
     * @return 更新后的角色响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CharacterResponseDTO> updateCharacter(@PathVariable Long id, @Valid @RequestBody CharacterUpdateDTO characterUpdateDTO) {
        Character character = new Character();
        character.setName(characterUpdateDTO.getName());
        character.setDescription(characterUpdateDTO.getDescription());
        character.setBackgroundStory(characterUpdateDTO.getBackgroundStory());
        character.setSecret(characterUpdateDTO.getSecret());
        character.setAvatarUrl(characterUpdateDTO.getAvatarUrl());

        try {
            Character updatedCharacter = characterService.updateCharacter(id, character);
            CharacterResponseDTO responseDTO = CharacterResponseDTO.fromEntity(updatedCharacter);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色响应DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponseDTO> getCharacterById(@PathVariable Long id) {
        Optional<Character> character = characterService.getCharacterById(id);
        return character.map(value -> {
            CharacterResponseDTO responseDTO = CharacterResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 查询所有角色
     * @return 角色响应DTO列表
     */
    @GetMapping
    public ResponseEntity<List<CharacterResponseDTO>> getAllCharacters() {
        List<Character> characters = characterService.getAllCharacters();
        List<CharacterResponseDTO> responseDTOs = characters.stream()
                .map(CharacterResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }
    
    /**
     * 根据剧本ID查询角色
     * @param scriptId 剧本ID
     * @return 角色响应DTO列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<CharacterResponseDTO>> getCharactersByScriptId(@PathVariable Long scriptId) {
        List<Character> characters = characterService.getCharactersByScriptId(scriptId);
        List<CharacterResponseDTO> responseDTOs = characters.stream()
                .map(CharacterResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据角色名查询角色
     *
     * @param name 角色名
     * @return 角色响应DTO列表
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<List<CharacterResponseDTO>> getCharactersByName(@PathVariable String name) {
        List<Character> characters = characterService.getCharactersByName(name);
        List<CharacterResponseDTO> responseDTOs = characters.stream()
                .map(CharacterResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }
}
