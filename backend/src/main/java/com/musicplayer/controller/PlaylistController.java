package com.musicplayer.controller;

import com.musicplayer.dto.ApiResponse;
import com.musicplayer.dto.PlaylistDTO;
import com.musicplayer.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public ApiResponse<List<PlaylistDTO>> getAllPlaylists() {
        return ApiResponse.success(playlistService.getAllPlaylists());
    }

    @GetMapping("/{id}")
    public ApiResponse<PlaylistDTO> getPlaylistById(@PathVariable Long id) {
        return ApiResponse.success(playlistService.getPlaylistById(id));
    }

    @PostMapping
    public ApiResponse<PlaylistDTO> createPlaylist(@RequestParam String name,
                                                     @RequestParam(required = false) String description) {
        return ApiResponse.success(playlistService.createPlaylist(name, description));
    }

    @PutMapping("/{id}")
    public ApiResponse<PlaylistDTO> updatePlaylist(@PathVariable Long id,
                                                    @RequestParam String name,
                                                    @RequestParam(required = false) String description) {
        return ApiResponse.success(playlistService.updatePlaylist(id, name, description));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public ApiResponse<PlaylistDTO> addSongToPlaylist(@PathVariable Long playlistId,
                                                        @PathVariable Long songId) {
        return ApiResponse.success(playlistService.addSongToPlaylist(playlistId, songId));
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ApiResponse<PlaylistDTO> removeSongFromPlaylist(@PathVariable Long playlistId,
                                                           @PathVariable Long songId) {
        return ApiResponse.success(playlistService.removeSongFromPlaylist(playlistId, songId));
    }
}
