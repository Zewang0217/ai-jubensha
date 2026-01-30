package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "game_players")
public class GamePlayer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;
    
    private Boolean isDm;
    
    @Enumerated(EnumType.STRING)
    private GamePlayerStatus status;
    
    private LocalDateTime joinTime;
    
    @PrePersist
    protected void onCreate() {
        joinTime = LocalDateTime.now();
    }
}