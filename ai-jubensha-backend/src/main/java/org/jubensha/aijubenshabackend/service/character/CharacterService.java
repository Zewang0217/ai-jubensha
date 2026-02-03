package org.jubensha.aijubenshabackend.service.character;

import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Script;

import java.util.List;
import java.util.Optional;

public interface CharacterService {
    
    /**
     * 创建新角色
     */
    Character createCharacter(Character character);
    
    /**
     * 根据ID获取角色
     */
    Optional<Character> getCharacterById(Long id);

    /**
     * 获取所有角色
     */
    List<Character> getAllCharacters();
    
    /**
     * 获取剧本的所有角色
     */
    List<Character> getCharactersByScript(Script script);
    
    /**
     * 根据剧本ID获取角色
     */
    List<Character> getCharactersByScriptId(Long scriptId);

    /**
     * 根据名字获取AI角色
     */
    List<Character> getCharactersByName(String name);

    /**
     * 更新角色
     */
    Character updateCharacter(Long id, Character character);
    
    /**
     * 删除角色
     */
    void deleteCharacter(Long id);
}