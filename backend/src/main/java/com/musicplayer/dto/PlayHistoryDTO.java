package com.musicplayer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlayHistoryDTO {
    private Long id;
    private Long songId;
    private String title;
    private String artist;
    private String album;
    private String coverUrl;
    private String audioUrl;
    private Integer duration;
    private Long playCount;
    private Integer likeCount;
    private String genre;
    private Boolean isLiked;
    private LocalDateTime playedAt;
}
