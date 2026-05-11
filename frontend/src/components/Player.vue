<template>
  <div class="player">
    <div class="player-left">
      <img
        v-if="currentSong"
        :src="currentSong.coverUrl || 'https://picsum.photos/seed/default/60/60'"
        class="album-cover"
        alt="专辑封面"
      />
      <div v-if="currentSong" class="song-info">
        <div class="song-title">{{ currentSong.title }}</div>
        <div class="song-artist">{{ currentSong.artist }}</div>
      </div>
      <div v-else class="no-song">未选择歌曲</div>
    </div>

    <div class="player-center">
      <div class="control-buttons">
        <el-button
          circle
          :icon="DArrowLeft"
          @click="$emit('prev')"
          :disabled="!currentSong"
          class="player-btn player-btn-prev"
        />
        <el-button
          circle
          :icon="isPlaying ? VideoPause : VideoPlay"
          @click="$emit('play-pause')"
          :disabled="!currentSong"
          class="player-btn player-btn-play"
        />
        <el-button
          circle
          :icon="DArrowRight"
          @click="$emit('next')"
          :disabled="!currentSong"
          class="player-btn player-btn-next"
        />
      </div>

      <div class="progress-container">
        <span class="time">{{ formatTime(isDragging ? sliderValue : currentTime) }}</span>
        <el-slider
          v-model="sliderValue"
          :max="currentSong?.duration || 0"
          @change="handleSliderChange"
          @start="handleDragStart"
          @stop="handleDragEnd"
          class="progress-slider"
          :disabled="!currentSong"
          :format-tooltip="formatTime"
          :show-input="false"
        />
        <span class="time">{{ formatTime(currentSong?.duration || 0) }}</span>
      </div>
    </div>

    <div class="player-right">
      <el-icon class="volume-icon"><Headset /></el-icon>
      <el-slider
        v-model="volumeValue"
        :max="100"
        @change="handleVolumeChange"
        class="volume-slider"
      />
    </div>

    <audio
      v-if="currentSong"
      ref="audioRef"
      :src="processedAudioUrl"
      :key="currentSong?.id" 
      @seeking="handleSeeking"
      @seeked="handleSeeked"
      @timeupdate="handleTimeUpdate"
      @ended="handleEnded"
      @loadedmetadata="handleLoadedMetadata"
      @error="handleAudioError"
      @abort="handleAudioAbort"
      @canplay="handleCanPlay"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { DArrowLeft, DArrowRight, VideoPlay, VideoPause, Headset } from '@element-plus/icons-vue'

const props = defineProps({
  currentSong: Object,
  isPlaying: Boolean,
  volume: Number
})

const emit = defineEmits(['play-pause', 'next', 'prev', 'volume-change'])

// 播放器状态
const audioRef = ref(null)
const currentTime = ref(0) // 实际音频播放进度
const sliderValue = ref(0) // 进度条显示的值
const volumeValue = ref(props.volume)

// 交互状态
const isDragging = ref(false) // 是否正在拖拽
const isSeeking = ref(false) // 是否正在跳转（音频引擎状态）
let lastSeekTime = 0 // 上次跳转的时间戳（非响应式即可）

const processedAudioUrl = computed(() => {
  if (!props.currentSong?.audioUrl) return ''
  // 添加时间戳参数以强制浏览器避开旧的（可能损坏的）缓存
  // 这对于解决之前的“代理头问题”遗留的缓存污染至关重要
  const separator = props.currentSong.audioUrl.includes('?') ? '&' : '?'
  return `${props.currentSong.audioUrl}${separator}_t=${Date.now()}`
})

const handleCanPlay = () => {
  // 当音频准备好播放且应该播放时
  if (props.isPlaying && audioRef.value && audioRef.value.paused) {
    audioRef.value.play().catch(e => console.warn('自动播放受阻:', e))
  }
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// ------ 1. 音频引擎事件 -> 同步到 UI ------

const handleTimeUpdate = () => {
  if (!audioRef.value) return
  
  // 更新真实的音频时间
  currentTime.value = audioRef.value.currentTime
  
  // 核心修复：引入时间锁
  // 1. 如果用户正在拖拽，绝不更新
  // 2. 如果距离上次跳转不足 1.2 秒（给音频引擎足够的缓冲稳定期），绝不更新
  if (isDragging.value) return
  if (Date.now() - lastSeekTime < 1200) return

  sliderValue.value = currentTime.value
}

// ------ 2. UI 交互事件 -> 控制音频引擎 ------

// 拖拽开始
const handleDragStart = () => {
  isDragging.value = true
}

// 拖拽结束
const handleDragEnd = () => {
  isDragging.value = false
}

// 进度条值改变（核心跳转逻辑）
const handleSliderChange = (val) => {
  if (!audioRef.value) return
  
  // 1. 记录跳转时间，开启 "保护罩"
  lastSeekTime = Date.now()
  
  // 2. 立即更新音频时间
  audioRef.value.currentTime = val
  
  // 3. 强制 UI 停留在用户选择的位置（防止被偶尔的脏数据覆盖）
  sliderValue.value = val
  
  // 4. 如果暂停中，跳转后尝试自动播放（可选优化体验）
  if (props.isPlaying) {
    audioRef.value.play().catch(() => {})
  }
}

// ------ 3. 其他业务逻辑 ------

const handleVolumeChange = () => {
  if (audioRef.value) {
    audioRef.value.volume = volumeValue.value / 100
    emit('volume-change', volumeValue.value)
  }
}

const handleEnded = () => {
  // 防抖：如果刚刚跳转过（1.5秒内）或正在跳转中，忽略 ended 事件
  // 这防止浏览器在 Seek 过程中因缓冲中断误报“播放结束”
  if (isSeeking.value || Date.now() - lastSeekTime < 1500) {
    console.warn('忽略跳转引发的 ended 事件', { isSeeking: isSeeking.value, timeDiff: Date.now() - lastSeekTime })
    return
  }
  emit('next')
}

const handleLoadedMetadata = () => {
  if (audioRef.value) {
    audioRef.value.volume = volumeValue.value / 100
    // 加载元数据后，重置滑块
    sliderValue.value = audioRef.value.currentTime
  }
}

const handleAudioError = (event) => {
  console.error('音频加载错误:', event.target.error)
  ElMessage.error({
    message: '歌曲加载失败，请尝试其他歌曲',
    duration: 3000,
    showClose: true
  })
}

const handleAudioAbort = (event) => {
  console.warn('音频加载被中止:', event)
}

// 新增：监听 Seek 状态
const handleSeeking = () => {
  isSeeking.value = true
}

const handleSeeked = () => {
  isSeeking.value = false
}

// 监听播放状态
watch(() => props.isPlaying, async (newVal) => {
  await nextTick() // 等待 DOM 更新，确保操作的是最新的 audio 元素
  if (audioRef.value) {
    if (newVal) {
      audioRef.value.play().catch(e => {
        // 忽略中止错误（通常发生在中途切歌时）
        if (e.name !== 'AbortError') {
          console.error('播放失败:', e)
        }
      })
    } else {
      audioRef.value.pause()
    }
  }
})

// 监听歌曲切换
watch(() => props.currentSong, (newSong) => {
  // 重置 UI 状态
  currentTime.value = 0
  sliderValue.value = 0
  isDragging.value = false
  lastSeekTime = 0 // 重置时间锁
  
  // 由于使用了 key，Vue 会自动重新创建 audio 元素
  // 所以这里不需要手动 pause 或重置 src
  // 新元素创建后会触发 mounted/canplay，由那里处理播放
})

onMounted(() => {
  if (audioRef.value) {
    audioRef.value.volume = volumeValue.value / 100
  }
})
</script>

<style scoped>
.player {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.player > div {
  margin-right: 0;
}

.player > div:last-child {
  margin-right: 0;
}

.player-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 250px;
}

.album-cover {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);
  margin-right: 15px;
  transition: transform 0.3s ease;
}

.album-cover:hover {
  transform: scale(1.05);
}

.song-info {
  flex: 1;
  overflow: hidden;
}

.song-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.song-artist {
  font-size: 13px;
  color: #7f8c8d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-song {
  color: #909399;
  font-size: 14px;
  font-style: italic;
}

.player-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 2;
  min-width: 350px;
  padding: 0;
}

.player-center > div {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
  padding: 0;
}

.control-buttons {
  display: flex;
  align-items: center;
  padding: 0;
  margin: 0;
}

/* 播放器按钮独立样式 */
.player-btn {
  width: 36px !important;
  height: 36px !important;
  font-size: 16px !important;
  border: 2px solid #e0e0e0 !important;
  background: white !important;
  color: #546e7a !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.3s ease !important;
  margin: 0 !important;
}

.player-btn:hover:not(:disabled) {
  border-color: #667eea !important;
  color: #667eea !important;
  transform: scale(1.1) !important;
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.3) !important;
}

.player-btn:disabled {
  border-color: #f0f0f0 !important;
  background: #f5f5f5 !important;
  color: #bdbdbd !important;
  cursor: not-allowed !important;
}

/* 前后按钮样式 */
.player-btn-prev {
  margin-right: 30px !important;
}

.player-btn-next {
  margin-left: 30px !important;
}

/* 播放按钮独立样式 */
.player-btn-play {
  width: 45px !important;
  height: 45px !important;
  font-size: 22px !important;
  border: none !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4) !important;
}

.player-btn-play:hover:not(:disabled) {
  border: none !important;
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%) !important;
  transform: scale(1.1) !important;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6) !important;
}

.player-btn-play:disabled {
  background: #f5f5f5 !important;
  color: #bdbdbd !important;
  box-shadow: none !important;
}

.progress-container {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 550px;
  gap: 15px;
  margin: 0;
  padding: 0;
}

.time {
  font-size: 12px;
  color: #546e7a;
  min-width: 45px;
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.progress-slider {
  flex: 1;
  --el-slider-runway-height: 6px;
  --el-slider-bar-height: 6px;
  --el-slider-button-size: 16px;
  --el-slider-button-bg-color: #667eea;
  --el-slider-bar-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.player-right {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 180px;
  justify-content: flex-end;
  gap: 10px;
}

.volume-icon {
  font-size: 20px;
  color: #546e7a;
  transition: color 0.3s ease;
}

.volume-icon:hover {
  color: #667eea;
}

.volume-slider {
  width: 120px;
  --el-slider-runway-height: 4px;
  --el-slider-bar-height: 4px;
  --el-slider-button-size: 12px;
  --el-slider-button-bg-color: #667eea;
  --el-slider-bar-color: #667eea;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .player {
    padding: 10px 15px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf0 100%);
  }
  
  .player > div {
    margin-right: 0;
  }
  
  .player-left {
    min-width: 160px;
    flex: 1;
  }
  
  .player-center {
    min-width: 200px;
    padding: 0;
    flex: 2;
  }
  
  .player-center > div {
    margin-bottom: 0;
    padding: 0;
  }
  
  .player-right {
    min-width: 120px;
    flex: 1;
  }
  
  .album-cover {
    width: 45px;
    height: 45px;
    margin-right: 10px;
    border-radius: 6px;
  }
  
  .song-title {
    font-size: 13px;
  }
  
  .song-artist {
    font-size: 11px;
  }
  
  .control-buttons {
    padding: 0;
    margin: 0;
  }
  
  .player-btn {
    width: 28px !important;
    height: 28px !important;
    font-size: 13px !important;
  }
  
  .player-btn-prev {
    margin-right: 15px !important;
  }
  
  .player-btn-next {
    margin-left: 15px !important;
  }
  
  .player-btn-play {
    width: 35px !important;
    height: 35px !important;
    font-size: 16px !important;
  }
  
  .progress-container {
    max-width: 350px;
    padding: 0;
    margin: 0;
    gap: 8px;
  }
  
  .time {
    font-size: 10px;
    min-width: 35px;
  }
  
  .progress-slider {
    --el-slider-runway-height: 4px;
    --el-slider-bar-height: 4px;
    --el-slider-button-size: 12px;
  }
  
  .volume-slider {
    width: 80px;
    --el-slider-runway-height: 3px;
    --el-slider-bar-height: 3px;
    --el-slider-button-size: 10px;
  }
  
  .volume-icon {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .player {
    flex-wrap: nowrap;
    padding: 8px 10px;
    gap: 8px;
  }
  
  .player > div {
    margin-right: 0;
    margin-bottom: 0;
  }
  
  .player-left {
    flex: 1;
    min-width: 120px;
  }
  
  .player-center {
    flex: 2;
    min-width: 150px;
    padding: 5px 0;
  }
  
  .player-right {
    flex: 0 0 auto;
    min-width: 80px;
    justify-content: center;
  }
  
  .album-cover {
    width: 40px;
    height: 40px;
    margin-right: 8px;
  }
  
  .song-title {
    font-size: 12px;
  }
  
  .song-artist {
    font-size: 10px;
  }
  
  .player-btn {
    width: 26px !important;
    height: 26px !important;
    font-size: 12px !important;
  }
  
  .player-btn-prev {
    margin-right: 12px !important;
  }
  
  .player-btn-next {
    margin-left: 12px !important;
  }
  
  .player-btn-play {
    width: 32px !important;
    height: 32px !important;
    font-size: 14px !important;
  }
  
  .progress-container {
    max-width: 250px;
    gap: 6px;
  }
  
  .time {
    font-size: 9px;
    min-width: 30px;
  }
  
  .progress-slider {
    --el-slider-runway-height: 3px;
    --el-slider-bar-height: 3px;
    --el-slider-button-size: 10px;
  }
  
  .volume-slider {
    width: 60px;
    --el-slider-runway-height: 2px;
    --el-slider-bar-height: 2px;
    --el-slider-button-size: 8px;
  }
  
  .volume-icon {
    font-size: 14px;
  }
}

@media (max-width: 360px) {
  .player {
    padding: 6px 8px;
  }
  
  .player-left {
    min-width: 100px;
  }
  
  .player-center {
    min-width: 120px;
  }
  
  .album-cover {
    width: 35px;
    height: 35px;
    margin-right: 6px;
  }
  
  .song-title {
    font-size: 11px;
  }
  
  .song-artist {
    font-size: 9px;
  }
  
  .player-btn {
    width: 24px !important;
    height: 24px !important;
    font-size: 11px !important;
  }
  
  .player-btn-prev {
    margin-right: 10px !important;
  }
  
  .player-btn-next {
    margin-left: 10px !important;
  }
  
  .player-btn-play {
    width: 30px !important;
    height: 30px !important;
    font-size: 13px !important;
  }
  
  .progress-container {
    max-width: 200px;
    gap: 5px;
  }
  
  .time {
    font-size: 8px;
    min-width: 25px;
  }
  
  .volume-slider {
    width: 50px;
  }
  
  .volume-icon {
    font-size: 12px;
  }
}
</style>
