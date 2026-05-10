package com.musicplayer.service;

import com.musicplayer.dto.RecentPlayDTO;
import com.musicplayer.entity.RecentPlay;
import com.musicplayer.entity.Song;
import com.musicplayer.repository.RecentPlayRepository;
import com.musicplayer.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentPlayService {

    private final RecentPlayRepository recentPlayRepository;
    private final SongRepository songRepository;

    @Transactional
    public void addRecentPlay(Long songId) {
        log.info("记录最近播放，歌曲ID: {}", songId);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("歌曲不存在"));

        Optional<RecentPlay> existingRecentPlay = recentPlayRepository.findBySongId(songId);

        if (existingRecentPlay.isPresent()) {
            RecentPlay recentPlay = existingRecentPlay.get();
            recentPlay.preUpdate();
            recentPlayRepository.save(recentPlay);
            log.info("更新最近播放时间，歌曲ID: {}", songId);
        } else {
            RecentPlay recentPlay = new RecentPlay();
            recentPlay.setSong(song);
            recentPlayRepository.save(recentPlay);
            log.info("新增最近播放记录，歌曲ID: {}", songId);
        }
    }

    public List<RecentPlayDTO> getRecentPlays() {
        log.info("获取最近播放列表");
        return recentPlayRepository.findAllWithSongOrderByPlayedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearRecentPlays() {
        log.info("清空最近播放列表");
        recentPlayRepository.deleteAll();
    }

    private RecentPlayDTO toDTO(RecentPlay recentPlay) {
        RecentPlayDTO dto = new RecentPlayDTO();
        dto.setId(recentPlay.getId());
        dto.setPlayedAt(recentPlay.getPlayedAt());

        Song song = recentPlay.getSong();
        if (song != null) {
            dto.setSongId(song.getId());
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
        }

        return dto;
    }
}
