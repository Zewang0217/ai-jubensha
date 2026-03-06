package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.DiscussionService;
import org.jubensha.aijubenshabackend.ai.service.WorkflowStatusService;
import org.jubensha.aijubenshabackend.ai.workflow.jubenshaWorkflow;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.models.dto.CharacterSelectDTO;
import org.jubensha.aijubenshabackend.models.dto.GameCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.GameResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.GameUpdateDTO;
import org.jubensha.aijubenshabackend.models.dto.PhaseConfirmDTO;
import org.jubensha.aijubenshabackend.models.dto.PhaseStatusDTO;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    private final DiscussionService discussionService;
    private final WorkflowStatusService workflowStatusService;
    private final InvestigationService investigationService;
    private final PlayerService playerService;
    private final CharacterService characterService;
    private final GamePlayerService gamePlayerService;

    public GameController(GameService gameService, jubenshaWorkflow workflow, DiscussionService discussionService, WorkflowStatusService workflowStatusService, InvestigationService investigationService, PlayerService playerService, CharacterService characterService, GamePlayerService gamePlayerService) {
        this.gameService = gameService;
        this.workflow = workflow;
        this.discussionService = discussionService;
        this.workflowStatusService = workflowStatusService;
        this.investigationService = investigationService;
        this.playerService = playerService;
        this.characterService = characterService;
        this.gamePlayerService = gamePlayerService;
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
     * 优雅退出游戏
     * <p>
     * 停止游戏相关的所有后台任务（讨论服务、计时器等），
     * 更新游戏状态为已结束。
     * </p>
     *
     * @param id 游戏ID
     * @return 更新后的游戏响应DTO
     */
    @PostMapping("/{id}/exit")
    public ResponseEntity<GameResponseDTO> exitGame(@PathVariable Long id) {
        try {
            log.info("收到优雅退出游戏请求，游戏ID: {}", id);
            Game game = gameService.exitGame(id);
            GameResponseDTO responseDTO = GameResponseDTO.fromEntity(game);
            log.info("游戏优雅退出成功，游戏ID: {}", id);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("游戏不存在，游戏ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("优雅退出游戏失败，游戏ID: {}, 错误: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public ResponseEntity<?> startWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String originalPrompt = (String) request.get("originalPrompt");
            if (originalPrompt == null || originalPrompt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "originalPrompt is required"));
            }

            // 获取剧本选择参数
            Boolean createNewScript = (Boolean) request.getOrDefault("createNewScript", true);
            Long scriptId = null;
            if (!createNewScript) {
                Object scriptIdObj = request.get("scriptId");
                if (scriptIdObj == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "scriptId is required when createNewScript is false"));
                }
                scriptId = scriptIdObj instanceof Number ? ((Number) scriptIdObj).longValue() : Long.parseLong(scriptIdObj.toString());
            }
            
            // 获取游戏参数
            Long gameId = null;
            Object gameIdObj = request.get("gameId");
            if (gameIdObj != null) {
                gameId = gameIdObj instanceof Number ? ((Number) gameIdObj).longValue() : Long.parseLong(gameIdObj.toString());
            } else {
                // 创建新游戏，gameCode 可以为 null，由系统自动生成
                Game newGame = new Game();
                // 不设置 gameCode，让系统根据需要自动生成
                gameId = gameService.createGame(newGame).getId();
            }
            
            // 获取流式生成参数
            Boolean useStreaming = (Boolean) request.getOrDefault("useStreaming", false);
            log.info("使用流式生成: {}", useStreaming);

            // 获取真人玩家数量参数
            Integer realPlayerCount = null;
            Object realPlayerCountObj = request.get("realPlayerCount");
            if (realPlayerCountObj != null) {
                if (realPlayerCountObj instanceof Number) {
                    realPlayerCount = ((Number) realPlayerCountObj).intValue();
                } else if (realPlayerCountObj instanceof String) {
                    try {
                        realPlayerCount = Integer.parseInt((String) realPlayerCountObj);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Invalid realPlayerCount format"));
                    }
                }
                // 验证真人玩家数量必须是非负整数
                if (realPlayerCount < 0) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "realPlayerCount must be non-negative"));
                }
                log.info("真人玩家数量: {}", realPlayerCount);
                
                // 保存真人玩家数量到数据库
                try {
                    Game game = gameService.getGameById(gameId).orElse(null);
                    if (game != null) {
                        game.setRealPlayerCount(realPlayerCount);
                        gameService.updateGame(gameId, game);
                        log.info("已保存真人玩家数量到数据库，游戏ID: {}, realPlayerCount: {}", gameId, realPlayerCount);
                    }
                } catch (Exception e) {
                    log.error("保存真人玩家数量失败: {}", e.getMessage());
                }
            } else {
                log.info("未设置真人玩家数量，使用默认值");
            }

            // 创建工作流状态
            WorkflowStatusService.WorkflowStatus status = workflowStatusService.createWorkflowStatus(gameId);
            
            // 立即返回响应，工作流在后台执行
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("gameId", gameId);
            response.put("workflowId", status.getWorkflowId());
            response.put("createNewScript", createNewScript);
            response.put("scriptId", scriptId);
            response.put("useStreaming", useStreaming);
            response.put("realPlayerCount", realPlayerCount);
            response.put("message", "工作流已在后台启动");

            // 复制变量为final，以便在lambda表达式中使用
            final Long finalGameId = gameId;
            final String finalOriginalPrompt = originalPrompt;
            final Boolean finalCreateNewScript = createNewScript;
            final Long finalScriptId = scriptId;
            final Boolean finalUseStreaming = useStreaming;
            final Integer finalRealPlayerCount = realPlayerCount;
            final WorkflowStatusService.WorkflowStatus finalStatus = status;
            
            // 异步执行工作流
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("开始异步执行工作流，游戏ID: {}, 工作流ID: {}", finalGameId, finalStatus.getWorkflowId());
                    workflowStatusService.updateWorkflowRunning(finalGameId, "工作流启动");
                    
                    WorkflowContext result = workflow.executeWorkflow(finalOriginalPrompt, finalCreateNewScript, finalScriptId, finalUseStreaming, finalGameId, finalRealPlayerCount);
                    
                    workflowStatusService.updateWorkflowCompleted(finalGameId, result);
                    log.info("工作流执行完成，游戏ID: {}, 工作流ID: {}", finalGameId, finalStatus.getWorkflowId());
                } catch (Exception e) {
                    log.error("工作流执行失败: {}", e.getMessage(), e);
                    workflowStatusService.updateWorkflowFailed(finalGameId, e.getMessage());
                }
            });

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.error("ID格式错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid ID format", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("启动工作流失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start workflow", "message", e.getMessage()));
        }
    }

    /**
     * 单独验证答题环节
     *
     * @param request 包含游戏ID、玩家ID列表、DM ID和Judge ID的请求
     * @return 验证结果，包含答案生成和评分信息
     */
    @PostMapping("/verify-answer-phase")
    public ResponseEntity<?> verifyAnswerPhase(@RequestBody Map<String, Object> request) {
        try {
            // 获取游戏参数
            Object gameIdObj = request.get("gameId");
            if (gameIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "gameId is required"));
            }
            Long gameId = gameIdObj instanceof Number ? ((Number) gameIdObj).longValue() : Long.parseLong(gameIdObj.toString());

            // 获取玩家ID列表
            List<Long> playerIds = new java.util.ArrayList<>();
            Object playerIdsObj = request.get("playerIds");
            if (playerIdsObj instanceof List) {
                for (Object obj : (List<?>) playerIdsObj) {
                    if (obj instanceof Number) {
                        playerIds.add(((Number) obj).longValue());
                    } else if (obj instanceof String) {
                        playerIds.add(Long.parseLong((String) obj));
                    }
                }
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "playerIds is required and must be a list"));
            }

            // 获取DM ID
            Object dmIdObj = request.get("dmId");
            if (dmIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dmId is required"));
            }
            Long dmId = dmIdObj instanceof Number ? ((Number) dmIdObj).longValue() : Long.parseLong(dmIdObj.toString());

            // 获取Judge ID
            Object judgeIdObj = request.get("judgeId");
            if (judgeIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "judgeId is required"));
            }
            Long judgeId = judgeIdObj instanceof Number ? ((Number) judgeIdObj).longValue() : Long.parseLong(judgeIdObj.toString());

            log.info("开始验证答题环节，游戏ID: {}, 玩家数量: {}, DM ID: {}, Judge ID: {}", gameId, playerIds.size(), dmId, judgeId);

            // 调用DiscussionService的verifyAnswerPhase方法
            Map<String, Object> result = discussionService.verifyAnswerPhase(gameId, playerIds, dmId, judgeId);

            // 构建响应
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("gameId", gameId);
            response.put("playerCount", playerIds.size());

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.error("ID格式错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid ID format", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("验证答题环节失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify answer phase", "message", e.getMessage()));
        }
    }

    /**
     * 测试DM评分功能
     *
     * @param request 包含DM ID和玩家答案列表的请求
     * @return 评分结果，包含每个玩家的评分和评论
     */
    @PostMapping("/test-dm-score")
    public ResponseEntity<?> testDMScore(@RequestBody Map<String, Object> request) {
        try {
            // 获取DM ID
            Object dmIdObj = request.get("dmId");
            if (dmIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dmId is required"));
            }
            Long dmId = dmIdObj instanceof Number ? ((Number) dmIdObj).longValue() : Long.parseLong(dmIdObj.toString());

            // 获取玩家答案列表
            List<Map<String, Object>> answers = new java.util.ArrayList<>();
            Object answersObj = request.get("answers");
            if (answersObj instanceof List) {
                for (Object obj : (List<?>) answersObj) {
                    if (obj instanceof Map) {
                        Map<String, Object> answerMap = new java.util.HashMap<>();
                        Map<?, ?> sourceMap = (Map<?, ?>) obj;
                        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                            if (entry.getKey() instanceof String) {
                                answerMap.put((String) entry.getKey(), entry.getValue());
                            }
                        }
                        answers.add(answerMap);
                    }
                }
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "answers is required and must be a list"));
            }

            log.info("开始测试DM评分功能，DM ID: {}, 玩家答案数量: {}", dmId, answers.size());

            // 调用DiscussionService的testDMScore方法
            String scoreResult = discussionService.testDMScore(dmId, answers);

            // 构建响应
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("scoreResult", scoreResult);
            response.put("dmId", dmId);
            response.put("answerCount", answers.size());

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.error("ID格式错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid ID format", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("测试DM评分功能失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to test DM score", "message", e.getMessage()));
        }
    }

    /**
     * 直接测试自由讨论和答题环节
     *
     * @param request 包含游戏ID、玩家ID列表、DM ID和Judge ID的请求
     * @return 测试结果，包含讨论和答题的完整流程信息
     */
    @PostMapping("/test-discussion-answer")
    public ResponseEntity<?> testDiscussionAnswer(@RequestBody Map<String, Object> request) {
        try {
            // 获取游戏参数
            Object gameIdObj = request.get("gameId");
            if (gameIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "gameId is required"));
            }
            Long gameId = gameIdObj instanceof Number ? ((Number) gameIdObj).longValue() : Long.parseLong(gameIdObj.toString());

            // 获取玩家ID列表
            List<Long> playerIds = new java.util.ArrayList<>();
            Object playerIdsObj = request.get("playerIds");
            if (playerIdsObj instanceof List) {
                for (Object obj : (List<?>) playerIdsObj) {
                    if (obj instanceof Number) {
                        playerIds.add(((Number) obj).longValue());
                    } else if (obj instanceof String) {
                        playerIds.add(Long.parseLong((String) obj));
                    }
                }
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "playerIds is required and must be a list"));
            }

            // 获取DM ID
            Object dmIdObj = request.get("dmId");
            if (dmIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dmId is required"));
            }
            Long dmId = dmIdObj instanceof Number ? ((Number) dmIdObj).longValue() : Long.parseLong(dmIdObj.toString());

            // 获取Judge ID
            Object judgeIdObj = request.get("judgeId");
            if (judgeIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "judgeId is required"));
            }
            Long judgeId = judgeIdObj instanceof Number ? ((Number) judgeIdObj).longValue() : Long.parseLong(judgeIdObj.toString());

            log.info("开始测试自由讨论和答题环节，游戏ID: {}, 玩家数量: {}, DM ID: {}, Judge ID: {}", gameId, playerIds.size(), dmId, judgeId);

            // 初始化讨论服务
            discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

            // 直接进入自由讨论阶段
            log.info("进入自由讨论阶段");
            discussionService.startFreeDiscussionPhase();

            // 等待一段时间让自由讨论进行
            log.info("等待自由讨论进行...");
            Thread.sleep(60000); // 等待60秒

            // 手动进入答题阶段
            log.info("进入答题阶段");
            discussionService.startAnswerPhase();

            // 等待一段时间让答题进行
            log.info("等待答题进行...");
            Thread.sleep(30000); // 等待30秒

            // 结束讨论，获取结果
            Map<String, Object> result = discussionService.endDiscussion();

            // 构建响应
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("gameId", gameId);
            response.put("playerCount", playerIds.size());

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.error("ID格式错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid ID format", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("测试自由讨论和答题环节失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to test discussion and answer phases", "message", e.getMessage()));
        }
    }

    /**
     * 查询工作流状态
     *
     * @param gameId 游戏ID
     * @return 工作流状态信息
     */
    @GetMapping("/{gameId}/workflow/status")
    public ResponseEntity<?> getWorkflowStatus(@PathVariable Long gameId) {
        try {
            WorkflowStatusService.WorkflowStatus status = workflowStatusService.getWorkflowStatus(gameId);
            if (status == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // 构建响应
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("workflowId", status.getWorkflowId());
            response.put("gameId", status.getGameId());
            response.put("state", status.getState().name());
            response.put("currentStep", status.getCurrentStep());
            response.put("errorMessage", status.getErrorMessage());
            
            // 如果工作流已完成，添加工作流上下文信息
            if (status.getWorkflowContext() != null) {
                WorkflowContext context = status.getWorkflowContext();
                Map<String, Object> contextInfo = new java.util.HashMap<>();
                contextInfo.put("scriptId", context.getScriptId());
                contextInfo.put("scriptName", context.getScriptName());
                contextInfo.put("playerAssignments", context.getPlayerAssignments());
                contextInfo.put("dmId", context.getDmId());
                contextInfo.put("judgeId", context.getJudgeId());
                contextInfo.put("realPlayerCount", context.getRealPlayerCount());
                contextInfo.put("aiPlayerCount", context.getAiPlayerCount());
                contextInfo.put("totalPlayerCount", context.getTotalPlayerCount());
                response.put("workflowContext", contextInfo);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询工作流状态失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get workflow status", "message", e.getMessage()));
        }
    }

    /**
     * 查询游戏阶段状态
     *
     * @param gameId 游戏ID
     * @return 阶段状态信息
     */
    @GetMapping("/{gameId}/phase-status")
    public ResponseEntity<PhaseStatusDTO> getPhaseStatus(@PathVariable Long gameId) {
        try {
            // 1. 获取游戏信息
            Optional<Game> gameOptional = gameService.getGameById(gameId);
            if (gameOptional.isEmpty()) {
                log.warn("游戏不存在，游戏ID: {}", gameId);
                return ResponseEntity.notFound().build();
            }
            Game game = gameOptional.get();

            // 2. 获取工作流状态
            WorkflowStatusService.WorkflowStatus status = workflowStatusService.getWorkflowStatus(gameId);

            // 3. 构建 PhaseStatusDTO 响应
            PhaseStatusDTO.PhaseStatusDTOBuilder builder = PhaseStatusDTO.builder()
                    .currentPhase(game.getCurrentPhase() != null ? game.getCurrentPhase().name() : "UNKNOWN");

            if (status != null) {
                // 设置工作流节点信息
                builder.workflowNode(status.getCurrentStep());

                // 根据工作流状态判断是否就绪
                boolean isReadyForNext = status.getState() == WorkflowStatusService.WorkflowState.COMPLETED;
                builder.isReadyForNext(isReadyForNext);

                // 设置提示信息
                String message;
                switch (status.getState()) {
                    case PENDING:
                        message = "工作流等待执行中";
                        break;
                    case RUNNING:
                        message = "工作流正在执行: " + status.getCurrentStep();
                        break;
                    case COMPLETED:
                        message = "工作流执行完成，可以进入下一阶段";
                        break;
                    case FAILED:
                        message = "工作流执行失败: " + status.getErrorMessage();
                        break;
                    default:
                        message = "未知状态";
                }
                builder.message(message);
            } else {
                // 没有工作流状态时，返回默认值
                builder.workflowNode("未启动")
                        .isReadyForNext(false)
                        .message("游戏尚未启动工作流")
                        .waitingForPlayers(List.of());
            }

            PhaseStatusDTO responseDTO = builder.build();
            log.info("查询阶段状态成功，游戏ID: {}, 当前阶段: {}", gameId, responseDTO.getCurrentPhase());
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            log.error("查询阶段状态失败，游戏ID: {}, 错误: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 确认阶段完成
     * <p>
     * 前端调用此 API 确认当前阶段完成。
     * 如果所有玩家都已确认，后端自动推进到下一阶段并广播 PHASE_CHANGE 消息。
     * </p>
     *
     * @param gameId     游戏ID
     * @param confirmDTO 确认请求
     * @return 确认结果，包含是否推进到下一阶段等信息
     */
    @PostMapping("/{gameId}/confirm-phase")
    public ResponseEntity<?> confirmPhase(@PathVariable Long gameId, @Valid @RequestBody PhaseConfirmDTO confirmDTO) {
        try {
            // 1. 验证游戏存在
            Optional<Game> gameOptional = gameService.getGameById(gameId);
            if (gameOptional.isEmpty()) {
                log.warn("游戏不存在，游戏ID: {}", gameId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "游戏不存在", "gameId", gameId));
            }
            Game game = gameOptional.get();

            // 2. 直接从 InvestigationService 获取工作流上下文（这是工作流节点使用的缓存）
            WorkflowContext context = investigationService.getWorkflowContext(gameId);
            if (context == null) {
                log.warn("工作流上下文不存在，游戏ID: {}", gameId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "工作流尚未启动或上下文未初始化"));
            }
            
            // 3. 处理确认逻辑
            // 如果 playerId 为 null，则是观察者确认
            if (confirmDTO.getPlayerId() == null) {
                context.setObserverConfirmed(true);
                log.info("[阶段同步] 观察者确认阶段完成，游戏ID: {}", gameId);
            } else {
                // 玩家确认
                context.confirmPhase(confirmDTO.getPlayerId());
                log.info("[阶段同步] 玩家 {} 确认阶段完成，游戏ID: {}", confirmDTO.getPlayerId(), gameId);
            }
            
            // 4. 同步更新 InvestigationService 缓存中的上下文
            investigationService.saveWorkflowContext(gameId, context);
            log.info("[阶段同步] 已同步更新 InvestigationService 缓存，游戏ID: {}, observerConfirmed: {}", gameId, context.isObserverConfirmed());

            // 5. 检查是否所有玩家都已确认
            boolean allConfirmed = context.isAllPlayersConfirmed();
            boolean isObserverMode = context.getRealPlayerCount() == null || context.getRealPlayerCount() == 0;
            boolean shouldAdvance = allConfirmed || (isObserverMode && context.isObserverConfirmed());

            // 6. 构建确认成功响应
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("gameId", gameId);
            response.put("playerId", confirmDTO.getPlayerId());
            response.put("phase", confirmDTO.getPhase());
            response.put("isObserver", confirmDTO.getPlayerId() == null);
            response.put("message", "阶段确认成功");
            response.put("observerConfirmed", context.isObserverConfirmed());
            response.put("allConfirmed", allConfirmed);
            response.put("unconfirmedPlayers", context.getUnconfirmedPlayers());
            response.put("currentPhase", game.getCurrentPhase() != null ? game.getCurrentPhase().name() : null);

            // 7. 如果所有玩家都已确认，自动推进阶段
            if (shouldAdvance) {
                log.info("[阶段同步] 所有玩家已确认，自动推进阶段，游戏ID: {}", gameId);
                
                // 推进阶段并广播通知
                Map<String, Object> advanceResult = gameService.advancePhaseWithNotification(gameId);
                
                response.put("phaseAdvanced", true);
                response.put("nextPhase", advanceResult.get("newPhase"));
                response.put("advanceMessage", advanceResult.get("message"));
                
                // 重置下一阶段的确认状态
                // 只获取真人玩家ID并重新初始化确认状态
                java.util.List<Long> realPlayerIds = new java.util.ArrayList<>();
                if (context.getPlayerAssignments() != null) {
                    log.info("[阶段同步] 重置确认状态 - 玩家分配数量: {}", context.getPlayerAssignments().size());
                    for (Map<String, Object> assignment : context.getPlayerAssignments()) {
                        Object playerTypeObj = assignment.get("playerType");
                        String playerType = playerTypeObj != null ? playerTypeObj.toString() : "AI";
                        Object playerIdObj = assignment.get("playerId");
                        // 只添加真人玩家到确认列表
                        if ("REAL".equals(playerType) && playerIdObj instanceof Number) {
                            realPlayerIds.add(((Number) playerIdObj).longValue());
                            log.info("[阶段同步] 重置确认状态 - 添加真人玩家: {}", playerIdObj);
                        } else {
                            log.info("[阶段同步] 重置确认状态 - 跳过 AI 玩家: playerType={}, playerId={}", playerType, playerIdObj);
                        }
                    }
                }
                context.initPhaseConfirmations(realPlayerIds);
                context.setObserverConfirmed(false);
                investigationService.saveWorkflowContext(gameId, context);
                log.info("[阶段同步] 已重置下一阶段的确认状态，游戏ID: {}, 真人玩家数量: {}", gameId, realPlayerIds.size());
            } else {
                response.put("phaseAdvanced", false);
                response.put("nextPhase", null);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("确认阶段失败，游戏ID: {}, 错误: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "确认阶段失败", "message", e.getMessage()));
        }
    }

    /**
     * 推进游戏到下一阶段
     * <p>
     * 手动推进游戏到下一阶段，并广播 PHASE_CHANGE 消息通知所有客户端。
     * 通常由前端确认阶段完成后自动调用，也可用于管理目的。
     * </p>
     *
     * @param gameId 游戏ID
     * @return 推进结果
     */
    @PostMapping("/{gameId}/advance-phase")
    public ResponseEntity<?> advancePhase(@PathVariable Long gameId) {
        try {
            log.info("收到阶段推进请求，游戏ID: {}", gameId);
            
            Map<String, Object> result = gameService.advancePhaseWithNotification(gameId);
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("阶段推进成功，游戏ID: {}, 新阶段: {}", gameId, result.get("newPhase"));
                return ResponseEntity.ok(result);
            } else {
                log.warn("阶段推进失败，游戏ID: {}, 原因: {}", gameId, result.get("message"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (IllegalArgumentException e) {
            log.error("游戏不存在，游戏ID: {}", gameId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "游戏不存在", "gameId", gameId));
        } catch (Exception e) {
            log.error("阶段推进失败，游戏ID: {}, 错误: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "阶段推进失败", "message", e.getMessage()));
        }
    }

    /**
     * 真人玩家选择角色
     * <p>
     * 在角色分配阶段，真人玩家选择一个角色。
     * 后端创建真人玩家记录并关联选择的角色。
     * </p>
     *
     * @param gameId 游戏ID
     * @param selectDTO 角色选择请求
     * @return 选择结果，包含玩家信息
     */
    @PostMapping("/{gameId}/select-character")
    public ResponseEntity<?> selectCharacter(@PathVariable Long gameId, @Valid @RequestBody CharacterSelectDTO selectDTO) {
        try {
            log.info("收到角色选择请求，游戏ID: {}, 角色ID: {}", gameId, selectDTO.getCharacterId());
            
            // 1. 验证游戏存在
            Optional<Game> gameOptional = gameService.getGameById(gameId);
            if (gameOptional.isEmpty()) {
                log.warn("游戏不存在，游戏ID: {}", gameId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "游戏不存在", "gameId", gameId));
            }
            Game game = gameOptional.get();
            
            // 2. 验证角色存在
            Optional<Character> characterOptional = characterService.getCharacterById(selectDTO.getCharacterId());
            if (characterOptional.isEmpty()) {
                log.warn("角色不存在，角色ID: {}", selectDTO.getCharacterId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "角色不存在", "characterId", selectDTO.getCharacterId()));
            }
            Character character = characterOptional.get();
            
            // 3. 检查是否已有真人玩家
            List<GamePlayer> existingGamePlayers = gamePlayerService.getGamePlayersByGameId(gameId);
            for (GamePlayer gp : existingGamePlayers) {
                // 检查是否已有真人玩家
                if (gp.getPlayer() != null && gp.getPlayer().getRole() == PlayerRole.REAL) {
                    log.warn("游戏已有真人玩家，游戏ID: {}, 玩家ID: {}", gameId, gp.getPlayer().getId());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "游戏已有真人玩家", "existingPlayerId", gp.getPlayer().getId()));
                }
                // 检查角色是否已被选择
                if (gp.getCharacter() != null && gp.getCharacter().getId().equals(selectDTO.getCharacterId())) {
                    log.warn("角色已被选择，角色ID: {}", selectDTO.getCharacterId());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "角色已被选择", "characterId", selectDTO.getCharacterId()));
                }
            }
            
            // 4. 创建真人玩家
            String nickname = selectDTO.getNickname();
            if (nickname == null || nickname.isEmpty()) {
                nickname = "玩家_" + System.currentTimeMillis();
            }
            
            Player realPlayer = new Player();
            realPlayer.setUsername("real_" + System.currentTimeMillis());
            realPlayer.setNickname(nickname);
            realPlayer.setPassword("123456");
            realPlayer.setEmail("real_" + System.currentTimeMillis() + "@example.com");
            realPlayer.setRole(PlayerRole.REAL);
            realPlayer.setStatus(PlayerStatus.ONLINE);
            realPlayer = playerService.createPlayer(realPlayer);
            log.info("创建真人玩家成功，玩家ID: {}, 昵称: {}", realPlayer.getId(), realPlayer.getNickname());
            
            // 5. 创建 GamePlayer 关联
            GamePlayer gamePlayer = new GamePlayer();
            gamePlayer.setGame(game);
            gamePlayer.setPlayer(realPlayer);
            gamePlayer.setCharacter(character);
            gamePlayer.setIsDm(false);
            gamePlayer.setStatus(GamePlayerStatus.PLAYING);
            gamePlayerService.createGamePlayer(gamePlayer);
            log.info("创建游戏玩家关联成功，游戏ID: {}, 玩家ID: {}, 角色ID: {}", gameId, realPlayer.getId(), character.getId());
            
            // 6. 更新 WorkflowContext
            WorkflowContext context = investigationService.getWorkflowContext(gameId);
            if (context != null) {
                // 添加真人玩家分配信息
                List<Map<String, Object>> assignments = context.getPlayerAssignments();
                if (assignments == null) {
                    assignments = new java.util.ArrayList<>();
                }
                
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("playerType", "REAL");
                assignment.put("playerId", realPlayer.getId());
                assignment.put("characterId", character.getId());
                assignment.put("characterName", character.getName());
                assignments.add(assignment);
                context.setPlayerAssignments(assignments);
                
                // 设置角色选择完成标志
                context.setCharacterSelected(true);
                context.setSelectedCharacterId(character.getId());
                context.setSelectedPlayerId(realPlayer.getId());
                
                investigationService.saveWorkflowContext(gameId, context);
                log.info("更新工作流上下文成功，游戏ID: {}", gameId);
            }
            
            // 7. 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gameId", gameId);
            response.put("playerId", realPlayer.getId());
            response.put("playerNickname", realPlayer.getNickname());
            response.put("characterId", character.getId());
            response.put("characterName", character.getName());
            response.put("playerRole", "REAL");
            response.put("message", "角色选择成功");
            
            log.info("角色选择成功，游戏ID: {}, 玩家ID: {}, 角色ID: {}", gameId, realPlayer.getId(), character.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("角色选择失败，游戏ID: {}, 错误: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "角色选择失败", "message", e.getMessage()));
        }
    }
}
