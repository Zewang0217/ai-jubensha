package org.jubensha.aijubenshabackend.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏实体类
 */
@Data
@Entity
@Table(name = "games")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "game_code", nullable = false, length = 20, unique = true)
    private String gameCode; // 游戏房间码
    
    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;
    
    @Column(name = "current_phase", nullable = false, length = 20)
    private String currentPhase; // 当前游戏阶段
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // 状态：waiting, playing, completed, cancelled
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GamePlayer> gamePlayers;
}
