package org.jubensha.aijubenshabackend.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clues")
public class Clue implements Serializable {
    
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

    // 线索的内容
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ClueType type;

    @Enumerated(EnumType.STRING)
    private ClueVisibility visibility;

    private String scene;

    private String imageUrl;

    // 面向DM用于控场
    private Integer importance;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Clue{id=" + id + ", name='" + name + "', scriptId=" + scriptId + '}';
    }
}