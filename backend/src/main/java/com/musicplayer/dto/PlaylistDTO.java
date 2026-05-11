package com.musicplayer.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlaylistDTO {
    private Long id;
    private String name;
    private String description;
    private Integer songCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SongDTO> songs;
}
