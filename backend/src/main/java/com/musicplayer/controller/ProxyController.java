package com.musicplayer.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
@RequestMapping("/api/proxy")
@CrossOrigin(origins = "*")
public class ProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/audio/{songId}")
    public ResponseEntity<?> proxyAudio(@PathVariable String songId, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        // 1. 尝试使用本地 MP3 文件 (利用 Spring 内置的 Range 支持)
        try {
            Resource resource = new ClassPathResource("audio/song" + songId + ".mp3");
            if (resource.exists() && resource.isReadable()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "audio/mpeg");
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            }
        } catch (Exception e) {
            System.out.println("本地文件读取失败: " + e.getMessage());
        }
        
        // 2. 本地不存在，代理远程音频
        String originalUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + songId + ".mp3";
        
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set("Accept-Encoding", "identity"); // 强制要求上游不压缩，防止 Content-Length/Range 不匹配
            if (rangeHeader != null) {
                requestHeaders.set("Range", rangeHeader);
            }
            HttpEntity<String> entity = new HttpEntity<>(requestHeaders);

            // 使用 RestTemplate 请求远程资源
            ResponseEntity<byte[]> response = restTemplate.exchange(
                originalUrl,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            
            // 构建安全的响应头 (只转发关键头，过滤掉可能导致冲突的头)
            HttpHeaders responseHeaders = new HttpHeaders();
            
            // 核心头
            responseHeaders.set("Content-Type", "audio/mpeg");
            responseHeaders.set("Access-Control-Allow-Origin", "*");
            responseHeaders.set("Access-Control-Allow-Methods", "GET, OPTIONS");
            
            // Range 相关
            if (response.getHeaders().containsKey("Content-Range")) {
                responseHeaders.set("Content-Range", response.getHeaders().getFirst("Content-Range"));
            }
            if (response.getHeaders().containsKey("Accept-Ranges")) {
                responseHeaders.set("Accept-Ranges", response.getHeaders().getFirst("Accept-Ranges"));
            }
            
            // 重新计算长度 (RestTemplate 可能已经自动解压了 gzip，所以 header 里的长度可能是错的)
            if (response.getBody() != null) {
                responseHeaders.setContentLength(response.getBody().length);
            }

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("远程服务器错误");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("播放失败");
        }
    }
}
