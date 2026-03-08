package org.jubensha.aijubenshabackend.service.game;

import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;
import org.jubensha.aijubenshabackend.repository.game.GamePlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GamePlayerServiceImpl implements GamePlayerService {

    private static final Logger logger = LoggerFactory.getLogger(GamePlayerServiceImpl.class);

    private final GamePlayerRepository gamePlayerRepository;

    @Autowired
    public GamePlayerServiceImpl(GamePlayerRepository gamePlayerRepository) {
        this.gamePlayerRepository = gamePlayerRepository;
    }

    @Override
    public GamePlayer createGamePlayer(GamePlayer gamePlayer) {
        logger.info("Creating new game player relationship: gameId={}, playerId={}",
                gamePlayer.getGame() != null ? gamePlayer.getGame().getId() : null,
                gamePlayer.getPlayer() != null ? gamePlayer.getPlayer().getId() : null);
        return gamePlayerRepository.save(gamePlayer);
    }

    @Override
    public Optional<GamePlayer> getGamePlayerById(Long id) {
        logger.info("Getting game player by id: {}", id);
        return gamePlayerRepository.findById(id);
    }

    @Override
    public Optional<GamePlayer> getGamePlayerByGameIdAndPlayerId(Long gameId, Long playerId) {
//        logger.info("Getting game player by gameId: {} and playerId: {}", gameId, playerId);
        return gamePlayerRepository.findByGameIdAndPlayerId(gameId, playerId);
    }

    @Override
    public List<GamePlayer> getGamePlayersByGameId(Long gameId) {
        logger.info("Getting game players by gameId: {}", gameId);
        return gamePlayerRepository.findByGameId(gameId);
    }

    @Override
    public List<GamePlayer> getGamePlayersByPlayerId(Long playerId) {
        logger.info("Getting game players by playerId: {}", playerId);
        return gamePlayerRepository.findByPlayerId(playerId);
    }

    @Override
    public Optional<GamePlayer> getCharacterByPlayerId(Long playerId) {
        logger.info("Getting game player by playerId (alias method): {}", playerId);
        List<GamePlayer> gamePlayers = gamePlayerRepository.findByPlayerId(playerId);
        // 返回第一个找到的游戏玩家关系（通常一个玩家在同一时间只参与一个游戏）
        return gamePlayers.isEmpty() ? Optional.empty() : Optional.of(gamePlayers.get(0));
    }

    @Override
    public List<GamePlayer> getGamePlayersByCharacterId(Long characterId) {
        logger.info("Getting game players by characterId: {}", characterId);
        return gamePlayerRepository.findByCharacterId(characterId);
    }

    @Override
    public Optional<GamePlayer> getGamePlayerByGameIdAndCharacterId(Long gameId, Long characterId) {
        logger.info("Getting game player by gameId: {} and characterId: {}", gameId, characterId);
        return gamePlayerRepository.findByGameIdAndCharacterId(gameId, characterId);
    }

    @Override
    public List<GamePlayer> getGamePlayersByGameIdAndIsDm(Long gameId, Boolean isDm) {
        logger.info("Getting game players by gameId: {} and isDm: {}", gameId, isDm);
        return gamePlayerRepository.findByGameIdAndIsDm(gameId, isDm);
    }

    @Override
    public List<GamePlayer> getRealPlayersByGameId(Long gameId) {
        logger.info("Getting real players by gameId: {}", gameId);
        List<GamePlayer> allPlayers = gamePlayerRepository.findByGameId(gameId);
        // 过滤出Real玩家（非AI、非DM的玩家）
        return allPlayers.stream()
                .filter(gp -> gp.getPlayer() != null
                        && gp.getPlayer().getRole() != null
                        && gp.getPlayer().getRole().name().equals("REAL"))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<GamePlayer> getAllGamePlayers() {
        logger.info("Getting all game players");
        return gamePlayerRepository.findAll();
    }

    @Override
    public GamePlayer updateGamePlayer(Long id, GamePlayer gamePlayer) {
        logger.info("Updating game player: {}", id);
        Optional<GamePlayer> existingGamePlayer = gamePlayerRepository.findById(id);
        if (existingGamePlayer.isPresent()) {
            GamePlayer updatedGamePlayer = existingGamePlayer.get();

            // 只更新非 null 的字段
            if (gamePlayer.getGame() != null) {
                updatedGamePlayer.setGame(gamePlayer.getGame());
            }
            if (gamePlayer.getPlayer() != null) {
                updatedGamePlayer.setPlayer(gamePlayer.getPlayer());
            }
            if (gamePlayer.getCharacter() != null) {
                updatedGamePlayer.setCharacter(gamePlayer.getCharacter());
            }
            if (gamePlayer.getIsDm() != null) {
                updatedGamePlayer.setIsDm(gamePlayer.getIsDm());
            }
            if (gamePlayer.getStatus() != null) {
                updatedGamePlayer.setStatus(gamePlayer.getStatus());
            }

            return gamePlayerRepository.save(updatedGamePlayer);
        } else {
            throw new IllegalArgumentException("GamePlayer not found with id: " + id);
        }
    }

    @Override
    public GamePlayer updateGamePlayerStatus(Long id, GamePlayerStatus status) {
        logger.info("Updating game player status: {} to {}", id, status);
        Optional<GamePlayer> existingGamePlayer = gamePlayerRepository.findById(id);
        if (existingGamePlayer.isPresent()) {
            GamePlayer updatedGamePlayer = existingGamePlayer.get();
            updatedGamePlayer.setStatus(status);
            return gamePlayerRepository.save(updatedGamePlayer);
        } else {
            throw new IllegalArgumentException("GamePlayer not found with id: " + id);
        }
    }

    @Override
    public void deleteGamePlayer(Long id) {
        logger.info("Deleting game player: {}", id);
        gamePlayerRepository.deleteById(id);
    }

    @Override
    public void deleteGamePlayersByGameId(Long gameId) {
        logger.info("Deleting all game players by gameId: {}", gameId);
        List<GamePlayer> gamePlayers = gamePlayerRepository.findByGameId(gameId);
        gamePlayerRepository.deleteAll(gamePlayers);
    }

    @Override
    public GamePlayer setPlayerAsDm(Long id) {
        logger.info("Setting player as DM: {}", id);
        Optional<GamePlayer> existingGamePlayer = gamePlayerRepository.findById(id);
        if (existingGamePlayer.isPresent()) {
            GamePlayer updatedGamePlayer = existingGamePlayer.get();
            updatedGamePlayer.setIsDm(true);
            return gamePlayerRepository.save(updatedGamePlayer);
        } else {
            throw new IllegalArgumentException("GamePlayer not found with id: " + id);
        }
    }

    @Override
    public GamePlayer removePlayerAsDm(Long id) {
        logger.info("Removing player as DM: {}", id);
        Optional<GamePlayer> existingGamePlayer = gamePlayerRepository.findById(id);
        if (existingGamePlayer.isPresent()) {
            GamePlayer updatedGamePlayer = existingGamePlayer.get();
            updatedGamePlayer.setIsDm(false);
            return gamePlayerRepository.save(updatedGamePlayer);
        } else {
            throw new IllegalArgumentException("GamePlayer not found with id: " + id);
        }
    }

    @Override
    public GamePlayer playerReady(Long id) {
        logger.info("Setting player ready: {}", id);
        return updateGamePlayerStatus(id, GamePlayerStatus.READY);
    }

    @Override
    public GamePlayer playerStartPlaying(Long id) {
        logger.info("Setting player playing: {}", id);
        return updateGamePlayerStatus(id, GamePlayerStatus.PLAYING);
    }

    @Override
    public GamePlayer playerLeave(Long id) {
        logger.info("Setting player left: {}", id);
        return updateGamePlayerStatus(id, GamePlayerStatus.LEFT);
    }
}