package com.musicplayer.service;

import com.musicplayer.dto.PlaylistDTO;
import com.musicplayer.dto.SongDTO;
import com.musicplayer.entity.Playlist;
import com.musicplayer.entity.PlaylistSong;
import com.musicplayer.entity.Song;
import com.musicplayer.repository.PlaylistRepository;
import com.musicplayer.repository.PlaylistSongRepository;
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
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;

    public List<PlaylistDTO> getAllPlaylists() {
        log.info("获取所有播放列表");
        return playlistRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PlaylistDTO getPlaylistById(Long id) {
        log.info("获取播放列表，ID: {}", id);
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));
        return toDTO(playlist);
    }

    @Transactional
    public PlaylistDTO createPlaylist(String name, String description) {
        log.info("创建播放列表，名称: {}", name);
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setDescription(description);
        return toDTO(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistDTO updatePlaylist(Long id, String name, String description) {
        log.info("更新播放列表，ID: {}", id);
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));
        playlist.setName(name);
        playlist.setDescription(description);
        return toDTO(playlistRepository.save(playlist));
    }

    @Transactional
    public void deletePlaylist(Long id) {
        log.info("删除播放列表，ID: {}", id);
        playlistSongRepository.deleteByPlaylistId(id);
        playlistRepository.deleteById(id);
    }

    @Transactional
    public PlaylistDTO addSongToPlaylist(Long playlistId, Long songId) {
        log.info("添加歌曲到播放列表，播放列表ID: {}, 歌曲ID: {}", playlistId, songId);
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("歌曲不存在"));

        List<PlaylistSong> existingSongs = playlistSongRepository.findByPlaylistIdOrderByPosition(playlistId);
        int maxPosition = existingSongs.isEmpty() ? -1 : existingSongs.get(existingSongs.size() - 1).getPosition();

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        playlistSong.setPosition(maxPosition + 1);

        playlistSongRepository.save(playlistSong);

        playlist.setSongCount(playlist.getSongCount() + 1);
        playlistRepository.save(playlist);

        return toDTO(playlist);
    }

    @Transactional
    public PlaylistDTO removeSongFromPlaylist(Long playlistId, Long songId) {
        log.info("从播放列表移除歌曲，播放列表ID: {}, 歌曲ID: {}", playlistId, songId);
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));

        PlaylistSong playlistSong = playlistSongRepository.findByPlaylistIdOrderByPosition(playlistId)
                .stream()
                .filter(ps -> ps.getSong().getId().equals(songId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("歌曲不在播放列表中"));

        playlistSongRepository.delete(playlistSong);

        playlist.setSongCount(playlist.getSongCount() - 1);
        playlistRepository.save(playlist);

        return toDTO(playlist);
    }

    private PlaylistDTO toDTO(Playlist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setSongCount(playlist.getSongCount());
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setUpdatedAt(playlist.getUpdatedAt());

        List<SongDTO> songs = playlistSongRepository.findByPlaylistIdOrderByPosition(playlist.getId())
                .stream()
                .map(ps -> {
                    SongDTO songDTO = new SongDTO();
                    songDTO.setId(ps.getSong().getId());
                    songDTO.setTitle(ps.getSong().getTitle());
                    songDTO.setArtist(ps.getSong().getArtist());
                    songDTO.setAlbum(ps.getSong().getAlbum());
                    songDTO.setCoverUrl(ps.getSong().getCoverUrl());
                    songDTO.setAudioUrl(ps.getSong().getAudioUrl());
                    songDTO.setDuration(ps.getSong().getDuration());
                    songDTO.setPlayCount(ps.getSong().getPlayCount());
                    songDTO.setLikeCount(ps.getSong().getLikeCount());
                    songDTO.setGenre(ps.getSong().getGenre());
                    songDTO.setIsLiked(ps.getSong().getIsLiked());
                    return songDTO;
                })
                .collect(Collectors.toList());
        dto.setSongs(songs);

        return dto;
    }
}
