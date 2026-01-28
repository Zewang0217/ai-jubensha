package org.jubensha.aijubenshabackend.service.game;

import org.jubensha.aijubenshabackend.domain.model.Game;
import org.jubensha.aijubenshabackend.repository.game.GameRepository;
import org.jubensha.aijubenshabackend.core.constant.GameStatus;
import org.jubensha.aijubenshabackend.core.constant.GamePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    
    private final GameRepository gameRepository;
    
    @Autowired
    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    
    @Override
    public Game createGame(Game game) {
        logger.info("Creating new game: {}", game.getGameCode());
        return gameRepository.save(game);
    }
    
    @Override
    public Optional<Game> getGameById(Long id) {
        logger.info("Getting game by id: {}", id);
        return gameRepository.findById(id);
    }
    
    @Override
    public Optional<Game> getGameByGameCode(String gameCode) {
        logger.info("Getting game by game code: {}", gameCode);
        return gameRepository.findByGameCode(gameCode);
    }
    
    @Override
    public List<Game> getAllGames() {
        logger.info("Getting all games");
        return gameRepository.findAll();
    }
    
    @Override
    public List<Game> getGamesByStatus(GameStatus status) {
        logger.info("Getting games by status: {}", status);
        return gameRepository.findByStatus(status.toString());
    }
    
    @Override
    public List<Game> getGamesByCurrentPhase(GamePhase currentPhase) {
        logger.info("Getting games by current phase: {}", currentPhase);
        return gameRepository.findByCurrentPhase(currentPhase.toString());
    }
    
    @Override
    public Game updateGame(Long id, Game game) {
        logger.info("Updating game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setGameCode(game.getGameCode());
            updatedGame.setScript(game.getScript());
            updatedGame.setStatus(game.getStatus());
            updatedGame.setCurrentPhase(game.getCurrentPhase());
            updatedGame.setStartTime(game.getStartTime());
            updatedGame.setEndTime(game.getEndTime());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }
    
    @Override
    public Game updateGameStatus(Long id, GameStatus status) {
        logger.info("Updating game status: {} to {}", id, status);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus(status.toString());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }
    
    @Override
    public Game updateGamePhase(Long id, GamePhase phase) {
        logger.info("Updating game phase: {} to {}", id, phase);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setCurrentPhase(phase.toString());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }
    
    @Override
    public void deleteGame(Long id) {
        logger.info("Deleting game: {}", id);
        gameRepository.deleteById(id);
    }
    
    @Override
    public List<Game> getGamesByScriptId(Long scriptId) {
        logger.info("Getting games by script id: {}", scriptId);
        return gameRepository.findByScriptId(scriptId);
    }
    
    @Override
    public List<Game> getGamesByStatusAndScriptId(GameStatus status, Long scriptId) {
        logger.info("Getting games by status: {} and script id: {}", status, scriptId);
        return gameRepository.findByStatusAndScriptId(status.toString(), scriptId);
    }
    
    @Override
    public Game startGame(Long id) {
        logger.info("Starting game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus("active");
            updatedGame.setCurrentPhase("initialization");
            updatedGame.setStartTime(java.time.LocalDateTime.now());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }
    
    @Override
    public Game endGame(Long id) {
        logger.info("Ending game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus("completed");
            updatedGame.setEndTime(java.time.LocalDateTime.now());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }
    
    @Override
    public Game cancelGame(Long id) {
        logger.info("Canceling game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus("canceled");
            updatedGame.setEndTime(java.time.LocalDateTime.now());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }
}