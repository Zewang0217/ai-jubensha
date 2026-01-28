package org.jubensha.aijubenshabackend.api.controller;

import org.jubensha.aijubenshabackend.domain.model.Game;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.core.constant.GameStatus;
import org.jubensha.aijubenshabackend.core.constant.GamePhase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/api/games")
public class GameController {
    
    private final GameService gameService;
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    /**
     * 创建游戏
     * @param game 游戏实体
     * @return 创建的游戏
     */
    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody Game game) {
        Game createdGame = gameService.createGame(game);
        return new ResponseEntity<>(createdGame, HttpStatus.CREATED);
    }
    
    /**
     * 更新游戏
     * @param id 游戏ID
     * @param game 游戏实体
     * @return 更新后的游戏
     */
    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable Long id, @RequestBody Game game) {
        Game updatedGame = gameService.updateGame(id, game);
        return new ResponseEntity<>(updatedGame, HttpStatus.OK);
    }
    
    /**
     * 删除游戏
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
     * @param id 游戏ID
     * @return 游戏实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        Optional<Game> game = gameService.getGameById(id);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据游戏房间码查询游戏
     * @param gameCode 游戏房间码
     * @return 游戏实体
     */
    @GetMapping("/code/{gameCode}")
    public ResponseEntity<Game> getGameByGameCode(@PathVariable String gameCode) {
        Optional<Game> game = gameService.getGameByGameCode(gameCode);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 查询所有游戏
     * @return 游戏列表
     */
    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return new ResponseEntity<>(games, HttpStatus.OK);
    }
    
    /**
     * 根据状态查询游戏
     * @param status 状态
     * @return 游戏列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Game>> getGamesByStatus(@PathVariable String status) {
        List<Game> games = gameService.getGamesByStatus(GameStatus.valueOf(status));
        return new ResponseEntity<>(games, HttpStatus.OK);
    }
    
    /**
     * 根据当前阶段查询游戏
     * @param currentPhase 当前阶段
     * @return 游戏列表
     */
    @GetMapping("/phase/{currentPhase}")
    public ResponseEntity<List<Game>> getGamesByCurrentPhase(@PathVariable String currentPhase) {
        List<Game> games = gameService.getGamesByCurrentPhase(GamePhase.valueOf(currentPhase));
        return new ResponseEntity<>(games, HttpStatus.OK);
    }
    
    /**
     * 根据剧本ID查询游戏
     * @param scriptId 剧本ID
     * @return 游戏列表
     */
    @GetMapping("/script/{scriptId}")
    public ResponseEntity<List<Game>> getGamesByScriptId(@PathVariable Long scriptId) {
        List<Game> games = gameService.getGamesByScriptId(scriptId);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }
    
    /**
     * 根据状态和剧本ID查询游戏
     * @param status 状态
     * @param scriptId 剧本ID
     * @return 游戏列表
     */
    @GetMapping("/status/{status}/script/{scriptId}")
    public ResponseEntity<List<Game>> getGamesByStatusAndScriptId(@PathVariable String status, @PathVariable Long scriptId) {
        List<Game> games = gameService.getGamesByStatusAndScriptId(GameStatus.valueOf(status), scriptId);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }
    
    /**
     * 开始游戏
     * @param id 游戏ID
     * @return 更新后的游戏
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<Game> startGame(@PathVariable Long id) {
        Game game = gameService.startGame(id);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }
    
    /**
     * 结束游戏
     * @param id 游戏ID
     * @return 更新后的游戏
     */
    @PutMapping("/{id}/end")
    public ResponseEntity<Game> endGame(@PathVariable Long id) {
        Game game = gameService.endGame(id);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }
    
    /**
     * 取消游戏
     * @param id 游戏ID
     * @return 更新后的游戏
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Game> cancelGame(@PathVariable Long id) {
        Game game = gameService.cancelGame(id);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }
    
    /**
     * 更新游戏阶段
     * @param id 游戏ID
     * @param phase 游戏阶段
     * @return 更新后的游戏
     */
    @PutMapping("/{id}/phase/{phase}")
    public ResponseEntity<Game> updateGamePhase(@PathVariable Long id, @PathVariable String phase) {
        Game game = gameService.updateGamePhase(id, GamePhase.valueOf(phase));
        return new ResponseEntity<>(game, HttpStatus.OK);
    }
}
