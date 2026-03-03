package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.models.dto.GamePlayerCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.GamePlayerResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.GamePlayerUpdateDTO;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;
import org.jubensha.aijubenshabackend.repository.character.CharacterRepository;
import org.jubensha.aijubenshabackend.repository.game.GameRepository;
import org.jubensha.aijubenshabackend.repository.player.PlayerRepository;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author luobo
 * <p>
 * 游戏玩家控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/game-players")
public class GamePlayerController {

    private final GamePlayerService gamePlayerService;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final CharacterRepository characterRepository;

    public GamePlayerController(GamePlayerService gamePlayerService,
                                GameRepository gameRepository,
                                PlayerRepository playerRepository,
                                CharacterRepository characterRepository) {
        this.gamePlayerService = gamePlayerService;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.characterRepository = characterRepository;
    }

    /**
     * 创建游戏玩家关系
     *
     * @param gamePlayerCreateDTO 游戏玩家创建DTO
     * @return 创建的游戏玩家响应DTO
     */
    @PostMapping
    public ResponseEntity<GamePlayerResponseDTO> createGamePlayer(@Valid @RequestBody GamePlayerCreateDTO gamePlayerCreateDTO) {
        // 验证关联实体是否存在
        Optional<Game> game = gameRepository.findById(gamePlayerCreateDTO.getGameId());
        if (game.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Player> player = playerRepository.findById(gamePlayerCreateDTO.getPlayerId());
        if (player.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Character> character = characterRepository.findById(gamePlayerCreateDTO.getCharacterId());
        if (character.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setGame(game.get());
        gamePlayer.setPlayer(player.get());
        gamePlayer.setCharacter(character.get());
        gamePlayer.setIsDm(gamePlayerCreateDTO.getIsDm());
        gamePlayer.setStatus(gamePlayerCreateDTO.getStatus());

        GamePlayer createdGamePlayer = gamePlayerService.createGamePlayer(gamePlayer);
        GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(createdGamePlayer);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * 更新游戏玩家关系
     *
     * @param id                  游戏玩家关系ID
     * @param gamePlayerUpdateDTO 游戏玩家更新DTO
     * @return 更新后的游戏玩家响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<GamePlayerResponseDTO> updateGamePlayer(@PathVariable Long id, @Valid @RequestBody GamePlayerUpdateDTO gamePlayerUpdateDTO) {
        GamePlayer gamePlayer = new GamePlayer();

        // 设置关联实体
        if (gamePlayerUpdateDTO.getGameId() != null) {
            Optional<Game> game = gameRepository.findById(gamePlayerUpdateDTO.getGameId());
            game.ifPresent(gamePlayer::setGame);
        }
        if (gamePlayerUpdateDTO.getPlayerId() != null) {
            Optional<Player> player = playerRepository.findById(gamePlayerUpdateDTO.getPlayerId());
            player.ifPresent(gamePlayer::setPlayer);
        }
        if (gamePlayerUpdateDTO.getCharacterId() != null) {
            Optional<Character> character = characterRepository.findById(gamePlayerUpdateDTO.getCharacterId());
            character.ifPresent(gamePlayer::setCharacter);
        }
        gamePlayer.setIsDm(gamePlayerUpdateDTO.getIsDm());
        gamePlayer.setStatus(gamePlayerUpdateDTO.getStatus());

        try {
            GamePlayer updatedGamePlayer = gamePlayerService.updateGamePlayer(id, gamePlayer);
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(updatedGamePlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除游戏玩家关系
     *
     * @param id 游戏玩家关系ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGamePlayer(@PathVariable Long id) {
        gamePlayerService.deleteGamePlayer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据ID查询游戏玩家关系
     *
     * @param id 游戏玩家关系ID
     * @return 游戏玩家响应DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<GamePlayerResponseDTO> getGamePlayerById(@PathVariable Long id) {
        Optional<GamePlayer> gamePlayer = gamePlayerService.getGamePlayerById(id);
        return gamePlayer.map(value -> {
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 根据游戏ID和玩家ID查询游戏玩家关系
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 游戏玩家响应DTO
     */
    @GetMapping("/game/{gameId}/player/{playerId}")
    public ResponseEntity<GamePlayerResponseDTO> getGamePlayerByGameIdAndPlayerId(@PathVariable Long gameId, @PathVariable Long playerId) {
        Optional<GamePlayer> gamePlayer = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
        return gamePlayer.map(value -> {
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 查询所有游戏玩家关系
     *
     * @return 游戏玩家响应DTO列表
     */
    @GetMapping
    public ResponseEntity<List<GamePlayerResponseDTO>> getAllGamePlayers() {
        List<GamePlayer> gamePlayers = gamePlayerService.getAllGamePlayers();
        List<GamePlayerResponseDTO> responseDTOs = gamePlayers.stream()
                .map(GamePlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据游戏ID查询游戏玩家关系
     *
     * @param gameId 游戏ID
     * @return 游戏玩家响应DTO列表
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<GamePlayerResponseDTO>> getGamePlayersByGameId(@PathVariable Long gameId) {
        List<GamePlayer> gamePlayers = gamePlayerService.getGamePlayersByGameId(gameId);
        List<GamePlayerResponseDTO> responseDTOs = gamePlayers.stream()
                .map(GamePlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据玩家ID查询游戏玩家关系
     *
     * @param playerId 玩家ID
     * @return 游戏玩家响应DTO列表
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<GamePlayerResponseDTO>> getGamePlayersByPlayerId(@PathVariable Long playerId) {
        List<GamePlayer> gamePlayers = gamePlayerService.getGamePlayersByPlayerId(playerId);
        List<GamePlayerResponseDTO> responseDTOs = gamePlayers.stream()
                .map(GamePlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据角色ID查询游戏玩家关系
     *
     * @param characterId 角色ID
     * @return 游戏玩家响应DTO列表
     */
    @GetMapping("/character/{characterId}")
    public ResponseEntity<List<GamePlayerResponseDTO>> getGamePlayersByCharacterId(@PathVariable Long characterId) {
        List<GamePlayer> gamePlayers = gamePlayerService.getGamePlayersByCharacterId(characterId);
        List<GamePlayerResponseDTO> responseDTOs = gamePlayers.stream()
                .map(GamePlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据游戏ID和角色ID查询游戏玩家关系
     *
     * @param gameId      游戏ID
     * @param characterId 角色ID
     * @return 游戏玩家响应DTO
     */
    @GetMapping("/game/{gameId}/character/{characterId}")
    public ResponseEntity<GamePlayerResponseDTO> getGamePlayerByGameIdAndCharacterId(@PathVariable Long gameId, @PathVariable Long characterId) {
        Optional<GamePlayer> gamePlayer = gamePlayerService.getGamePlayerByGameIdAndCharacterId(gameId, characterId);
        return gamePlayer.map(value -> {
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 根据游戏ID和是否为DM查询游戏玩家关系
     *
     * @param gameId 游戏ID
     * @param isDm   是否为DM
     * @return 游戏玩家响应DTO列表
     */
    @GetMapping("/game/{gameId}/dm/{isDm}")
    public ResponseEntity<List<GamePlayerResponseDTO>> getGamePlayersByGameIdAndIsDm(@PathVariable Long gameId, @PathVariable Boolean isDm) {
        List<GamePlayer> gamePlayers = gamePlayerService.getGamePlayersByGameIdAndIsDm(gameId, isDm);
        List<GamePlayerResponseDTO> responseDTOs = gamePlayers.stream()
                .map(GamePlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据状态查询游戏玩家关系
     *
     * @param status 状态
     * @return 游戏玩家响应DTO列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<GamePlayerResponseDTO>> getGamePlayersByStatus(@PathVariable String status) {
        try {
            GamePlayerStatus gamePlayerStatus = GamePlayerStatus.valueOf(status.toUpperCase());
            List<GamePlayer> allGamePlayers = gamePlayerService.getAllGamePlayers();
            List<GamePlayer> filteredGamePlayers = allGamePlayers.stream()
                    .filter(gp -> gp.getStatus() == gamePlayerStatus)
                    .collect(Collectors.toList());
            List<GamePlayerResponseDTO> responseDTOs = filteredGamePlayers.stream()
                    .map(GamePlayerResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 更新游戏玩家状态
     *
     * @param id     游戏玩家关系ID
     * @param status 状态
     * @return 更新后的游戏玩家响应DTO
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<GamePlayerResponseDTO> updateGamePlayerStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            GamePlayerStatus gamePlayerStatus = GamePlayerStatus.valueOf(status.toUpperCase());
            GamePlayer gamePlayer = gamePlayerService.updateGamePlayerStatus(id, gamePlayerStatus);
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(gamePlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 设置玩家为DM
     *
     * @param id 游戏玩家关系ID
     * @return 更新后的游戏玩家响应DTO
     */
    @PutMapping("/{id}/set-dm")
    public ResponseEntity<GamePlayerResponseDTO> setPlayerAsDm(@PathVariable Long id) {
        try {
            GamePlayer gamePlayer = gamePlayerService.setPlayerAsDm(id);
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(gamePlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 取消玩家DM身份
     *
     * @param id 游戏玩家关系ID
     * @return 更新后的游戏玩家响应DTO
     */
    @PutMapping("/{id}/remove-dm")
    public ResponseEntity<GamePlayerResponseDTO> removePlayerAsDm(@PathVariable Long id) {
        try {
            GamePlayer gamePlayer = gamePlayerService.removePlayerAsDm(id);
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(gamePlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 玩家准备就绪
     *
     * @param id 游戏玩家关系ID
     * @return 更新后的游戏玩家响应DTO
     */
    @PutMapping("/{id}/ready")
    public ResponseEntity<GamePlayerResponseDTO> playerReady(@PathVariable Long id) {
        try {
            GamePlayer gamePlayer = gamePlayerService.playerReady(id);
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(gamePlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 玩家开始游戏
     *
     * @param id 游戏玩家关系ID
     * @return 更新后的游戏玩家响应DTO
     */
    @PutMapping("/{id}/start-playing")
    public ResponseEntity<GamePlayerResponseDTO> playerStartPlaying(@PathVariable Long id) {
        try {
            GamePlayer gamePlayer = gamePlayerService.playerStartPlaying(id);
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(gamePlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 玩家离开游戏
     *
     * @param id 游戏玩家关系ID
     * @return 更新后的游戏玩家响应DTO
     */
    @PutMapping("/{id}/leave")
    public ResponseEntity<GamePlayerResponseDTO> playerLeave(@PathVariable Long id) {
        try {
            GamePlayer gamePlayer = gamePlayerService.playerLeave(id);
            GamePlayerResponseDTO responseDTO = GamePlayerResponseDTO.fromEntity(gamePlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 根据游戏ID删除所有游戏玩家关系
     *
     * @param gameId 游戏ID
     * @return 响应
     */
    @DeleteMapping("/game/{gameId}")
    public ResponseEntity<Void> deleteGamePlayersByGameId(@PathVariable Long gameId) {
        gamePlayerService.deleteGamePlayersByGameId(gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}