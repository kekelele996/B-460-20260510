package com.musicplayer.controller;

import com.musicplayer.dto.ApiResponse;
import com.musicplayer.dto.RecentPlayDTO;
import com.musicplayer.service.RecentPlayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recent-plays")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecentPlayController {

    private final RecentPlayService recentPlayService;

    @GetMapping
    public ApiResponse<List<RecentPlayDTO>> getRecentPlays() {
        return ApiResponse.success(recentPlayService.getRecentPlays());
    }

    @DeleteMapping
    public ApiResponse<Void> clearRecentPlays() {
        recentPlayService.clearRecentPlays();
        return ApiResponse.success(null);
    }
}
