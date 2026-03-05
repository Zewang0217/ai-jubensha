package org.jubensha.aijubenshabackend.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", insertable = false, updatable = false)
    @JsonIgnore
    private Script script;

    @Column(name = "script_id", nullable = true)
    private Long scriptId;

    @Column(name = "game_code", nullable = true, unique = true)
    private String gameCode;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private GamePhase currentPhase;

    /**
     * 当前工作流节点名称
     */
    @Column(name = "workflow_node", length = 100)
    private String workflowNode;

    /**
     * 节点是否就绪
     */
    @Column(name = "node_ready")
    private Boolean nodeReady;

    /**
     * 真人玩家数量，用于前端恢复玩家模式配置
     */
    @Column(name = "real_player_count")
    private Integer realPlayerCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}