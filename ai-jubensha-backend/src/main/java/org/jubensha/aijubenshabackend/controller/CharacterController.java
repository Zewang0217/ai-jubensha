package org.jubensha.aijubenshabackend.controller;

import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
     * @param character 角色实体
     * @return 创建的角色
     */
    @PostMapping
    public ResponseEntity<Character> createCharacter(@RequestBody Character character) {
        Character createdCharacter = characterService.createCharacter(character);
        return new ResponseEntity<>(createdCharacter, HttpStatus.CREATED);
    }
    
    /**
     * 更新角色
     * @param id 角色ID
     * @param character 角色实体
     * @return 更新后的角色
     */
    @PutMapping("/{id}")
    public ResponseEntity<Character> updateCharacter(@PathVariable Long id, @RequestBody Character character) {
        Character updatedCharacter = characterService.updateCharacter(id, character);
        return new ResponseEntity<>(updatedCharacter, HttpStatus.OK);
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
     * @return 角色实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Character> getCharacterById(@PathVariable Long id) {
        Optional<Character> character = characterService.getCharacterById(id);
        return character.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 查询所有角色
     * @return 角色列表
     */
    @GetMapping
    public ResponseEntity<List<Character>> getAllCharacters() {
        List<Character> characters = characterService.getAllCharacters();
        return new ResponseEntity<>(characters, HttpStatus.OK);
    }
    
    /**
     * 根据剧本ID查询角色
     * @param scriptId 剧本ID
     * @return 角色列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<Character>> getCharactersByScriptId(@PathVariable Long scriptId) {
        List<Character> characters = characterService.getCharactersByScriptId(scriptId);
        return new ResponseEntity<>(characters, HttpStatus.OK);
    }

    /**
     * 根据角色名查询角色
     *
     * @param name 角色名
     * @return 角色列表
     */
    @GetMapping("/{name}")
    public ResponseEntity<List<Character>> getCharactersByScriptId(@PathVariable String name) {
        List<Character> characters = characterService.getAiCharactersByName(name);
        return new ResponseEntity<>(characters, HttpStatus.OK);
    }
}
