package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.DiscussionService;
import org.jubensha.aijubenshabackend.ai.service.WorkflowStatusService;
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

    public GameController(GameService gameService, jubenshaWorkflow workflow, DiscussionService discussionService, WorkflowStatusService workflowStatusService) {
        this.gameService = gameService;
        this.workflow = workflow;
        this.discussionService = discussionService;
        this.workflowStatusService = workflowStatusService;
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
}
