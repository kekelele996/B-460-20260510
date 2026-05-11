package com.musicplayer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "songs")
@Data
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "歌曲名称不能为空")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "歌手不能为空")
    @Column(nullable = false)
    private String artist;

    @NotBlank(message = "专辑不能为空")
    @Column(nullable = false)
    private String album;

    @Column(length = 500)
    private String coverUrl;

    @NotBlank(message = "音频文件路径不能为空")
    @Column(nullable = false, length = 500)
    private String audioUrl;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Long playCount = 0L;

    @Column(nullable = false)
    private Integer likeCount = 0;

    private String genre;

    @Column(nullable = false)
    private Boolean isLiked = false;
}
