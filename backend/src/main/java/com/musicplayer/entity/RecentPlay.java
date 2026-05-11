package com.musicplayer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "recent_plays")
@Data
public class RecentPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false, unique = true)
    private Song song;

    @Column(nullable = false)
    private LocalDateTime playedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.playedAt = LocalDateTime.now();
    }
}
