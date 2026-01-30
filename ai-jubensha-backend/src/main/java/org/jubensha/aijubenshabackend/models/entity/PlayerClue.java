package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "player_clues", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"game_id", "player_id", "clue_id"})
})
public class PlayerClue {
    
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
    @JoinColumn(name = "clue_id", nullable = false)
    private Clue clue;
    
    private LocalDateTime discoveredTime;
    
    @PrePersist
    protected void onCreate() {
        discoveredTime = LocalDateTime.now();
    }
}