package org.jubensha.aijubenshabackend.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(name = "CharacterEntity")
@Table(name = "characters")
public class Character implements Serializable {
    
    private static final long serialVersionUID = 1L;

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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String backgroundStory;

    @Column(columnDefinition = "TEXT")
    private String secret;

    @Column(name = "avatar")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String timeline;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Character{id=" + id + ", name='" + name + "', scriptId=" + scriptId + '}';
    }
}