package com.musicplayer.controller;

import com.musicplayer.dto.ApiResponse;
import com.musicplayer.dto.PlayHistoryDTO;
import com.musicplayer.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/play-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayHistoryController {

    private final PlayHistoryService playHistoryService;

    @GetMapping
    public ApiResponse<List<PlayHistoryDTO>> getRecentPlayed() {
        return ApiResponse.success(playHistoryService.getRecentPlayed());
    }
}
