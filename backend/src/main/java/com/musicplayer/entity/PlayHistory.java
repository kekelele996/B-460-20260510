package com.musicplayer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "play_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = "song_id")
})
@Data
public class PlayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "song_id", nullable = false, unique = true)
    private Long songId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", insertable = false, updatable = false)
    private Song song;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;
}
