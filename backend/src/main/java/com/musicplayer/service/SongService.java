package com.musicplayer.service;

import com.musicplayer.dto.SongDTO;
import com.musicplayer.entity.Song;
import com.musicplayer.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    public List<SongDTO> getAllSongs() {
        log.info("获取所有歌曲");
        return songRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SongDTO getSongById(Long id) {
        log.info("获取歌曲，ID: {}", id);
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("歌曲不存在"));
        return toDTO(song);
    }

    public List<SongDTO> searchSongs(String keyword) {
        log.info("搜索歌曲，关键词: {}", keyword);
        return songRepository.findAll().stream()
                .filter(song -> song.getTitle().contains(keyword) ||
                        song.getArtist().contains(keyword) ||
                        song.getAlbum().contains(keyword))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SongDTO> getMostPlayedSongs() {
        log.info("获取最常播放的歌曲");
        return songRepository.findMostPlayed().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SongDTO> getLikedSongs() {
        log.info("获取喜欢的歌曲");
        return songRepository.findLikedSongs().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementPlayCount(Long id) {
        log.info("增加播放次数，歌曲ID: {}", id);
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("歌曲不存在"));
        song.setPlayCount(song.getPlayCount() + 1);
        songRepository.save(song);
    }

    @Transactional
    public SongDTO toggleLike(Long id) {
        log.info("切换喜欢状态，歌曲ID: {}", id);
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("歌曲不存在"));
        song.setIsLiked(!song.getIsLiked());
        song.setLikeCount(song.getIsLiked() ? song.getLikeCount() + 1 : song.getLikeCount() - 1);
        return toDTO(songRepository.save(song));
    }

    private SongDTO toDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setArtist(song.getArtist());
        dto.setAlbum(song.getAlbum());
        dto.setCoverUrl(song.getCoverUrl());
        dto.setAudioUrl(song.getAudioUrl());
        dto.setDuration(song.getDuration());
        dto.setPlayCount(song.getPlayCount());
        dto.setLikeCount(song.getLikeCount());
        dto.setGenre(song.getGenre());
        dto.setIsLiked(song.getIsLiked());
        return dto;
    }
}
