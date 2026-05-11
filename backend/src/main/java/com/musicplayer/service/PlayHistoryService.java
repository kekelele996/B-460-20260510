package com.musicplayer.service;

import com.musicplayer.dto.SongDTO;
import com.musicplayer.entity.PlayHistory;
import com.musicplayer.entity.Song;
import com.musicplayer.repository.PlayHistoryRepository;
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

    @Transactional
    public void recordPlay(Long songId) {
        log.info("记录播放历史，歌曲ID: {}", songId);
        PlayHistory playHistory = playHistoryRepository.findBySongId(songId)
                .orElseGet(() -> {
                    PlayHistory newHistory = new PlayHistory();
                    newHistory.setSongId(songId);
                    return newHistory;
                });
        playHistory.setPlayedAt(LocalDateTime.now());
        playHistoryRepository.save(playHistory);
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getRecentPlayedSongs() {
        log.info("获取最近播放的歌曲");
        List<PlayHistory> historyList = playHistoryRepository.findRecentPlayHistory();
        
        return historyList.stream()
                .map(PlayHistory::getSong)
                .map(this::toDTO)
                .collect(Collectors.toList());
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
