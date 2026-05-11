package com.musicplayer.service;

import com.musicplayer.dto.PlayHistoryDTO;
import com.musicplayer.entity.PlayHistory;
import com.musicplayer.entity.Song;
import com.musicplayer.repository.PlayHistoryRepository;
import com.musicplayer.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayHistoryService {

    private final PlayHistoryRepository playHistoryRepository;
    private final SongRepository songRepository;

    @Transactional
    public void recordPlay(Long songId) {
        log.info("记录播放历史，歌曲ID: {}", songId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("歌曲不存在"));

        PlayHistory history = playHistoryRepository.findBySongId(songId)
                .orElseGet(() -> {
                    PlayHistory h = new PlayHistory();
                    h.setSong(song);
                    return h;
                });
        history.setPlayedAt(LocalDateTime.now());
        playHistoryRepository.save(history);
    }

    public List<PlayHistoryDTO> getRecentPlayed() {
        log.info("获取最近播放列表");
        return playHistoryRepository.findAllByOrderByPlayedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private PlayHistoryDTO toDTO(PlayHistory history) {
        PlayHistoryDTO dto = new PlayHistoryDTO();
        dto.setId(history.getId());
        dto.setSongId(history.getSong().getId());
        dto.setTitle(history.getSong().getTitle());
        dto.setArtist(history.getSong().getArtist());
        dto.setAlbum(history.getSong().getAlbum());
        dto.setCoverUrl(history.getSong().getCoverUrl());
        dto.setAudioUrl(history.getSong().getAudioUrl());
        dto.setDuration(history.getSong().getDuration());
        dto.setPlayCount(history.getSong().getPlayCount());
        dto.setLikeCount(history.getSong().getLikeCount());
        dto.setGenre(history.getSong().getGenre());
        dto.setIsLiked(history.getSong().getIsLiked());
        dto.setPlayedAt(history.getPlayedAt());
        return dto;
    }
}
