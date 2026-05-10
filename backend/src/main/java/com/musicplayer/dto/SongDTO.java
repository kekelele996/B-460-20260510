package com.musicplayer.dto;

import lombok.Data;

@Data
public class SongDTO {
    private Long id;
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
}
