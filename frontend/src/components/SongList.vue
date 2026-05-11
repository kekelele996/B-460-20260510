<template>
  <div class="song-list">
    <div v-if="songs.length === 0" class="empty-state">
      <el-icon><Document /></el-icon>
      <p>暂无歌曲</p>
    </div>
    <el-table
      v-else
      :data="songs"
      stripe
      style="width: 100%"
      @row-click="handleRowClick"
    >
      <el-table-column type="index" width="50" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img
            :src="row.coverUrl || 'https://picsum.photos/seed/default/50/50'"
            class="song-cover"
            alt="封面"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="歌曲名称" min-width="200" />
      <el-table-column prop="artist" label="歌手" width="150" />
      <el-table-column prop="album" label="专辑" width="150" />
      <el-table-column prop="duration" label="时长" width="80">
        <template #default="{ row }">
          {{ formatTime(row.duration) }}
        </template>
      </el-table-column>
      <el-table-column prop="playCount" label="播放次数" width="100" />
      <el-table-column v-if="showPlayedAt" prop="playedAt" label="播放时间" width="180">
        <template #default="{ row }">
          {{ formatPlayedAt(row.playedAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="喜欢数" width="80" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            :icon="row.isLiked ? StarFilled : Star"
            :type="row.isLiked ? 'danger' : 'default'"
            circle
            size="small"
            @click.stop="handleLike(row)"
          />
          <el-button
            :icon="currentSong?.id === row.id && isPlaying ? VideoPause : VideoPlay"
            type="primary"
            circle
            size="small"
            @click.stop="handlePlay(row)"
          />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { Star, StarFilled, VideoPlay, VideoPause, Document } from '@element-plus/icons-vue'

defineProps({
  songs: {
    type: Array,
    default: () => []
  },
  currentSong: Object,
  isPlaying: Boolean,
  showPlayedAt: Boolean
})

const emit = defineEmits(['play', 'like'])

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const formatPlayedAt = (playedAt) => {
  if (!playedAt) return ''
  const date = new Date(playedAt)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays}天前`
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

const handleRowClick = (row) => {
  emit('play', row)
}

const handlePlay = (song) => {
  emit('play', song)
}

const handleLike = (song) => {
  emit('like', song)
}
</script>

<style scoped>
.song-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  color: #909399;
}

.empty-state .el-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
}

.song-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .song-list {
    padding: 15px;
    border-radius: 6px;
  }

  .empty-state {
    padding: 60px 0;
  }

  .empty-state .el-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }

  .empty-state p {
    font-size: 14px;
  }

  :deep(.el-table) {
    font-size: 13px;
  }

  :deep(.el-table__cell) {
    padding: 8px 0;
  }

  .song-cover {
    width: 40px;
    height: 40px;
  }
}

@media (max-width: 480px) {
  .song-list {
    padding: 10px;
    border-radius: 4px;
  }

  .empty-state {
    padding: 40px 0;
  }

  .empty-state .el-icon {
    font-size: 40px;
    margin-bottom: 8px;
  }

  .empty-state p {
    font-size: 13px;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table__cell) {
    padding: 6px 0;
  }

  .song-cover {
    width: 35px;
    height: 35px;
  }

  :deep(.el-button--small) {
    padding: 5px;
  }
}
</style>
