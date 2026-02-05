package org.jubensha.aijubenshabackend.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scenes")
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", insertable = false, updatable = false)
    @JsonIgnore
    private Script script;

    @Column(name = "script_id", nullable = false)
    private Long scriptId;

    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String availableActions;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}