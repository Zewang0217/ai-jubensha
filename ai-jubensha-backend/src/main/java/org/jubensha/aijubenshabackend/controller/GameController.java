package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.workflow.jubenshaWorkflow;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.models.dto.GameCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.GameResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.GameUpdateDTO;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 游戏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final jubenshaWorkflow workflow;

    public GameController(GameService gameService, jubenshaWorkflow workflow) {
        this.gameService = gameService;
        this.workflow = workflow;
    }

    /**
     * 创建游戏
     *
     * @param gameCreateDTO 游戏创建DTO
     * @return 创建的游戏响应DTO
     */
    @PostMapping
    public ResponseEntity<GameResponseDTO> createGame(@Valid @RequestBody GameCreateDTO gameCreateDTO) {
        Game game = new Game();
        game.setScriptId(gameCreateDTO.getScriptId());
        game.setGameCode(gameCreateDTO.getGameCode());
        game.setStatus(gameCreateDTO.getStatus());
        game.setCurrentPhase(gameCreateDTO.getCurrentPhase());
        game.setStartTime(gameCreateDTO.getStartTime());
        game.setEndTime(gameCreateDTO.getEndTime());

        Game createdGame = gameService.createGame(game);
        GameResponseDTO responseDTO = GameResponseDTO.fromEntity(createdGame);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * 更新游戏
     *
     * @param id            游戏ID
     * @param gameUpdateDTO 游戏更新DTO
     * @return 更新后的游戏响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDTO> updateGame(@PathVariable Long id, @Valid @RequestBody GameUpdateDTO gameUpdateDTO) {
        Game game = new Game();
        game.setGameCode(gameUpdateDTO.getGameCode());
        game.setStatus(gameUpdateDTO.getStatus());
        game.setCurrentPhase(gameUpdateDTO.getCurrentPhase());
        game.setStartTime(gameUpdateDTO.getStartTime());
        game.setEndTime(gameUpdateDTO.getEndTime());

        try {
            Game updatedGame = gameService.updateGame(id, game);
            GameResponseDTO responseDTO = GameResponseDTO.fromEntity(updatedGame);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除游戏
     *
     * @param id 游戏ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据ID查询游戏
     *
     * @param id 游戏ID
     * @return 游戏响应DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getGameById(@PathVariable Long id) {
        Optional<Game> game = gameService.getGameById(id);
        return game.map(value -> {
            GameResponseDTO responseDTO = GameResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 根据游戏房间码查询游戏
     *
     * @param gameCode 游戏房间码
     * @return 游戏响应DTO
     */
    @GetMapping("/code/{gameCode}")
    public ResponseEntity<GameResponseDTO> getGameByGameCode(@PathVariable String gameCode) {
        Optional<Game> game = gameService.getGameByGameCode(gameCode);
        return game.map(value -> {
            GameResponseDTO responseDTO = GameResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 查询所有游戏
     *
     * @return 游戏响应DTO列表
     */
    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        List<GameResponseDTO> responseDTOs = games.stream()
                .map(GameResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据状态查询游戏
     *
     * @param status 状态
     * @return 游戏响应DTO列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<GameResponseDTO>> getGamesByStatus(@PathVariable String status) {
        try {
            GameStatus gameStatus = GameStatus.valueOf(status.toUpperCase());
            List<Game> games = gameService.getGamesByStatus(gameStatus);
            List<GameResponseDTO> responseDTOs = games.stream()
                    .map(GameResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据当前阶段查询游戏
     *
     * @param currentPhase 当前阶段
     * @return 游戏响应DTO列表
     */
    @GetMapping("/phase/{currentPhase}")
    public ResponseEntity<List<GameResponseDTO>> getGamesByCurrentPhase(@PathVariable String currentPhase) {
        try {
            GamePhase gamePhase = GamePhase.valueOf(currentPhase.toUpperCase());
            List<Game> games = gameService.getGamesByCurrentPhase(gamePhase);
            List<GameResponseDTO> responseDTOs = games.stream()
                    .map(GameResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据剧本ID查询游戏
     *
     * @param scriptId 剧本ID
     * @return 游戏响应DTO列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<GameResponseDTO>> getGamesByScriptId(@PathVariable Long scriptId) {
        List<Game> games = gameService.getGamesByScriptId(scriptId);
        List<GameResponseDTO> responseDTOs = games.stream()
                .map(GameResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据状态和剧本ID查询游戏
     *
     * @param status   状态
     * @param scriptId 剧本ID
     * @return 游戏响应DTO列表
     */
    @GetMapping("/script/{scriptId}/status/{status}")
    public ResponseEntity<List<GameResponseDTO>> getGamesByStatusAndScriptId(@PathVariable Long scriptId, @PathVariable String status) {
        try {
            GameStatus gameStatus = GameStatus.valueOf(status.toUpperCase());
            List<Game> games = gameService.getGamesByScriptIdAndStatus(scriptId, gameStatus);
            List<GameResponseDTO> responseDTOs = games.stream()
                    .map(GameResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 开始游戏
     *
     * @param id 游戏ID
     * @return 更新后的游戏响应DTO
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<GameResponseDTO> startGame(@PathVariable Long id) {
        Game game = gameService.startGame(id);
        GameResponseDTO responseDTO = GameResponseDTO.fromEntity(game);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    /**
     * 结束游戏
     *
     * @param id 游戏ID
     * @return 更新后的游戏响应DTO
     */
    @PutMapping("/{id}/end")
    public ResponseEntity<GameResponseDTO> endGame(@PathVariable Long id) {
        Game game = gameService.endGame(id);
        GameResponseDTO responseDTO = GameResponseDTO.fromEntity(game);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    /**
     * 取消游戏
     *
     * @param id 游戏ID
     * @return 更新后的游戏响应DTO
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<GameResponseDTO> cancelGame(@PathVariable Long id) {
        Game game = gameService.cancelGame(id);
        GameResponseDTO responseDTO = GameResponseDTO.fromEntity(game);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    /**
     * 更新游戏阶段
     *
     * @param id    游戏ID
     * @param phase 游戏阶段
     * @return 更新后的游戏响应DTO
     */
    @PutMapping("/{id}/phase/{phase}")
    public ResponseEntity<GameResponseDTO> updateGamePhase(@PathVariable Long id, @PathVariable String phase) {
        try {
            GamePhase gamePhase = GamePhase.valueOf(phase.toUpperCase());
            Game game = gameService.updateGamePhase(id, gamePhase);
            GameResponseDTO responseDTO = GameResponseDTO.fromEntity(game);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 启动工作流
     *
     * @param request 包含原始提示词的请求
     * @return 工作流执行结果
     */
    @PostMapping("/start-workflow")
    public ResponseEntity<?> startWorkflow(@RequestBody Map<String, String> request) {
        try {
            String originalPrompt = request.get("originalPrompt");
            if (originalPrompt == null || originalPrompt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "originalPrompt is required"));
            }

            WorkflowContext result = workflow.executeWorkflow(originalPrompt);

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "scriptId", result.getScriptId(),
                    "scriptName", result.getScriptName(),
                    "currentStep", result.getCurrentStep(),
                    "playerAssignments", result.getPlayerAssignments(),
                    "dmId", result.getDmId(),
                    "judgeId", result.getJudgeId(),
                    "realPlayerCount", result.getRealPlayerCount(),
                    "aiPlayerCount", result.getAiPlayerCount(),
                    "totalPlayerCount", result.getTotalPlayerCount()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("启动工作流失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start workflow", "message", e.getMessage()));
        }
    }
}
