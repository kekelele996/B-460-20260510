-- 创建数据库
CREATE DATABASE IF NOT EXISTS music_player DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE music_player;

-- 创建歌曲表
CREATE TABLE IF NOT EXISTS songs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    album VARCHAR(255) NOT NULL,
    cover_url VARCHAR(500),
    audio_url VARCHAR(500) NOT NULL,
    duration INT NOT NULL,
    play_count BIGINT DEFAULT 0,
    like_count INT DEFAULT 0,
    genre VARCHAR(100),
    is_liked BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建播放列表表
CREATE TABLE IF NOT EXISTS playlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    song_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建播放列表歌曲关联表
CREATE TABLE IF NOT EXISTS playlist_songs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    playlist_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    position INT NOT NULL,
    FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE,
    UNIQUE KEY uk_playlist_song_position (playlist_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建播放历史表
CREATE TABLE IF NOT EXISTS play_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    song_id BIGINT NOT NULL,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE,
    UNIQUE KEY uk_song_id (song_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入示例歌曲数据（使用本地MP3文件）
INSERT INTO songs (title, artist, album, cover_url, audio_url, duration, play_count, like_count, genre, is_liked) VALUES
('晴天', '周杰伦', '叶惠美', 'https://picsum.photos/seed/song1/300/300', '/api/proxy/audio/1', 269, 12500, 856, '流行', true),
('稻香', '周杰伦', '魔杰座', 'https://picsum.photos/seed/song2/300/300', '/api/proxy/audio/2', 223, 9800, 642, '流行', true),
('夜曲', '周杰伦', '十一月的萧邦', 'https://picsum.photos/seed/song3/300/300', '/api/proxy/audio/3', 247, 8900, 523, '流行', false),
('青花瓷', '周杰伦', '我很忙', 'https://picsum.photos/seed/song4/300/300', '/api/proxy/audio/4', 259, 7600, 489, '中国风', true),
('七里香', '周杰伦', '七里香', 'https://picsum.photos/seed/song5/300/300', '/api/proxy/audio/5', 289, 6500, 378, '流行', false),
('告白气球', '周杰伦', '周杰伦的床边故事', 'https://picsum.photos/seed/song6/300/300', '/api/proxy/audio/6', 207, 5400, 312, '流行', true),
('等你下课', '周杰伦', '不能说的秘密', 'https://picsum.photos/seed/song7/300/300', '/api/proxy/audio/7', 254, 4300, 256, '流行', false),
('说好不哭', '周杰伦', '说好不哭', 'https://picsum.photos/seed/song8/300/300', '/api/proxy/audio/8', 236, 3800, 198, '流行', true),
('Mojito', '周杰伦', 'Mojito', 'https://picsum.photos/seed/song9/300/300', '/api/proxy/audio/9', 280, 3200, 156, '流行', false),
('最伟大的作品', '周杰伦', '最伟大的作品', 'https://picsum.photos/seed/song10/300/300', '/api/proxy/audio/10', 300, 2800, 123, '流行', true);

-- 插入示例播放列表数据
INSERT INTO playlists (name, description, song_count) VALUES
('我喜欢的音乐', '收藏我喜欢的歌曲', 6),
('流行热歌', '当下最流行的歌曲', 10),
('周杰伦精选', '周杰伦的经典歌曲', 10);

-- 为播放列表添加歌曲
INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES
-- 我喜欢的音乐
(1, 1, 0), (1, 2, 1), (1, 4, 2), (1, 6, 3), (1, 8, 4), (1, 10, 5),
-- 流行热歌
(2, 1, 0), (2, 2, 1), (2, 3, 2), (2, 4, 3), (2, 5, 4), (2, 6, 5), (2, 7, 6), (2, 8, 7), (2, 9, 8), (2, 10, 9),
-- 周杰伦精选
(3, 1, 0), (3, 2, 1), (3, 3, 2), (3, 4, 3), (3, 5, 4), (3, 6, 5), (3, 7, 6), (3, 8, 7), (3, 9, 8), (3, 10, 9);

-- 更新播放列表的歌曲数量
UPDATE playlists SET song_count = 6 WHERE id = 1;
UPDATE playlists SET song_count = 10 WHERE id = 2;
UPDATE playlists SET song_count = 10 WHERE id = 3;
