package com.musicplayer.controller;

import com.musicplayer.dto.ApiResponse;
import com.musicplayer.dto.SongDTO;
import com.musicplayer.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SongController {

    private final SongService songService;

    @GetMapping
    public ApiResponse<List<SongDTO>> getAllSongs() {
        return ApiResponse.success(songService.getAllSongs());
    }

    @GetMapping("/{id}")
    public ApiResponse<SongDTO> getSongById(@PathVariable Long id) {
        return ApiResponse.success(songService.getSongById(id));
    }

    @GetMapping("/search")
    public ApiResponse<List<SongDTO>> searchSongs(@RequestParam String keyword) {
        return ApiResponse.success(songService.searchSongs(keyword));
    }

    @GetMapping("/most-played")
    public ApiResponse<List<SongDTO>> getMostPlayedSongs() {
        return ApiResponse.success(songService.getMostPlayedSongs());
    }

    @GetMapping("/liked")
    public ApiResponse<List<SongDTO>> getLikedSongs() {
        return ApiResponse.success(songService.getLikedSongs());
    }

    @PostMapping("/{id}/play")
    public ApiResponse<Void> incrementPlayCount(@PathVariable Long id) {
        songService.incrementPlayCount(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/like")
    public ApiResponse<SongDTO> toggleLike(@PathVariable Long id) {
        return ApiResponse.success(songService.toggleLike(id));
    }
}
