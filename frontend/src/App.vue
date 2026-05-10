<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="header-left">
          <el-button
            class="menu-toggle"
            :icon="Fold"
            circle
            @click="toggleSidebar"
            v-show="isMobile"
          />
          <h1>🎵 音乐播放器</h1>
        </div>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索歌曲..."
          class="search-input"
          @keyup.enter="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </header>

    <!-- 中间内容区域 -->
    <div class="main-container">
      <!-- 左侧边栏 -->
      <aside class="sidebar" :class="{ 'sidebar-mobile': isMobile, 'sidebar-open': sidebarOpen }">
        <div class="menu-title">
          <el-icon><Menu /></el-icon>
          <span>菜单</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          @select="handleMenuSelect"
          class="sidebar-menu"
        >
          <el-menu-item index="all">
            <el-icon><List /></el-icon>
            <span>全部歌曲</span>
          </el-menu-item>
          <el-menu-item index="liked">
            <el-icon><Star /></el-icon>
            <span>我喜欢的</span>
          </el-menu-item>
          <el-menu-item index="recent-played">
            <el-icon><Clock /></el-icon>
            <span>最近播放</span>
          </el-menu-item>
          <el-menu-item index="most-played">
            <el-icon><TrendCharts /></el-icon>
            <span>热门歌曲</span>
          </el-menu-item>
          <el-divider>播放列表</el-divider>
          <el-menu-item
            v-for="playlist in playlists"
            :key="playlist.id"
            :index="'playlist-' + playlist.id"
          >
            <el-icon><Folder /></el-icon>
            <span>{{ playlist.name }}</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 侧边栏遮罩层（移动端） -->
      <div
        v-if="isMobile && sidebarOpen"
        class="sidebar-overlay"
        @click="toggleSidebar"
      ></div>

      <!-- 主内容区域 -->
      <main class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else>
          <SongList
            :songs="songs"
            :current-song="currentSong"
            :is-playing="isPlaying"
            :show-played-at="showPlayedAt"
            @play="handlePlay"
            @like="handleLike"
          />
        </div>
      </main>
    </div>

    <!-- 固定在底部的播放器 -->
    <div class="fixed-player">
      <Player
        :current-song="currentSong"
        :is-playing="isPlaying"
        :volume="volume"
        @play-pause="togglePlayPause"
        @next="playNext"
        @prev="playPrev"
        @volume-change="handleVolumeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Fold } from '@element-plus/icons-vue'
import { songApi, playlistApi, playHistoryApi } from './api'
import Player from './components/Player.vue'
import SongList from './components/SongList.vue'

const searchKeyword = ref('')
const activeMenu = ref('all')
const loading = ref(false)
const songs = ref([])
const showPlayedAt = ref(false)
const playlists = ref([])
const currentSong = ref(null)
const isPlaying = ref(false)
const volume = ref(80)

const isMobile = ref(false)
const sidebarOpen = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const loadSongs = async () => {
  loading.value = true
  showPlayedAt.value = false
  try {
    let response
    if (activeMenu.value === 'all') {
      response = await songApi.getAllSongs()
    } else if (activeMenu.value === 'liked') {
      response = await songApi.getLikedSongs()
    } else if (activeMenu.value === 'recent-played') {
      response = await playHistoryApi.getRecentPlayed()
      showPlayedAt.value = true
      songs.value = response.data.map(item => ({
        id: item.songId,
        title: item.title,
        artist: item.artist,
        album: item.album,
        coverUrl: item.coverUrl,
        audioUrl: item.audioUrl,
        duration: item.duration,
        playCount: item.playCount,
        likeCount: item.likeCount,
        genre: item.genre,
        isLiked: item.isLiked,
        playedAt: item.playedAt
      }))
      loading.value = false
      return
    } else if (activeMenu.value === 'most-played') {
      response = await songApi.getMostPlayedSongs()
    } else if (activeMenu.value.startsWith('playlist-')) {
      const playlistId = activeMenu.value.replace('playlist-', '')
      const playlist = await playlistApi.getPlaylistById(playlistId)
      songs.value = playlist.data.songs
      loading.value = false
      return
    } else {
      response = await songApi.searchSongs(searchKeyword.value)
    }
    songs.value = response.data
  } catch (error) {
    ElMessage.error('加载歌曲失败')
  } finally {
    loading.value = false
  }
}

const loadPlaylists = async () => {
  try {
    const response = await playlistApi.getAllPlaylists()
    playlists.value = response.data
  } catch (error) {
    ElMessage.error('加载播放列表失败')
  }
}

const handleSearch = () => {
  activeMenu.value = 'search'
  loadSongs()
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  loadSongs()
  if (isMobile.value) {
    sidebarOpen.value = false
  }
}

const handlePlay = async (song) => {
  currentSong.value = song
  isPlaying.value = true
  try {
    await songApi.incrementPlayCount(song.id)
    // 只更新当前歌曲的播放次数，不重新加载整个列表
    // 这样可以避免页面闪烁
    const updatedSong = songs.value.find(s => s.id === song.id)
    if (updatedSong) {
      updatedSong.playCount++
    }
  } catch (error) {
    console.error('更新播放次数失败', error)
  }
}

const handleLike = async (song) => {
  try {
    await songApi.toggleLike(song.id)
    await loadSongs()
    ElMessage.success(song.isLiked ? '已取消喜欢' : '已添加到喜欢')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const togglePlayPause = () => {
  isPlaying.value = !isPlaying.value
}

const playNext = () => {
  if (!currentSong.value) return
  const currentIndex = songs.value.findIndex(s => s.id === currentSong.value.id)
  const nextIndex = (currentIndex + 1) % songs.value.length
  handlePlay(songs.value[nextIndex])
}

const playPrev = () => {
  if (!currentSong.value) return
  const currentIndex = songs.value.findIndex(s => s.id === currentSong.value.id)
  const prevIndex = currentIndex === 0 ? songs.value.length - 1 : currentIndex - 1
  handlePlay(songs.value[prevIndex])
}

const handleVolumeChange = (value) => {
  volume.value = value
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadSongs()
  loadPlaylists()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
#app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 顶部导航栏 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 80px;
  padding: 0;
  min-height: 80px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 30px;
  width: 100%;
  max-width: 1200px;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.menu-toggle {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.menu-toggle:hover {
  background: rgba(255, 255, 255, 0.3);
}

.search-input {
  width: 300px;
}

/* 中间内容区域 */
.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  padding-bottom: 100px;
  position: relative;
}

/* 左侧边栏 */
.sidebar {
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  width: 250px;
  overflow-y: auto;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.menu-title {
  padding: 20px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sidebar-menu {
  border-right: none;
}

/* 侧边栏遮罩层 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 40px 20px 20px;
  overflow-y: auto;
  background: #f0f2f5;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #909399;
  gap: 10px;
}

.loading-container .el-icon {
  font-size: 48px;
}

/* 固定在底部的播放器 */
.fixed-player {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 1000;
  display: flex;
  align-items: center;
  height: 100px;
  margin: 0;
  padding: 0;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .header {
    height: 60px;
    min-height: 60px;
  }

  .header-content {
    gap: 15px;
    padding: 0 15px;
  }

  .header h1 {
    font-size: 18px;
  }

  .menu-toggle {
    width: 32px !important;
    height: 32px !important;
    padding: 0;
  }

  .search-input {
    width: 150px;
  }

  .main-container {
    padding-bottom: 80px;
  }

  .sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    bottom: 80px;
    width: 250px;
    z-index: 100;
    transform: translateX(-100%);
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  }

  .sidebar.sidebar-open {
    transform: translateX(0);
  }

  .main-content {
    padding: 20px 15px 15px;
  }

  .fixed-player {
    height: 80px;
  }
}

@media (max-width: 480px) {
  .header {
    height: 50px;
    min-height: 50px;
  }

  .header-content {
    gap: 10px;
    padding: 0 10px;
  }

  .header h1 {
    font-size: 16px;
  }

  .menu-toggle {
    width: 28px !important;
    height: 28px !important;
  }

  .search-input {
    width: 120px;
  }

  .main-container {
    padding-bottom: 70px;
  }

  .sidebar {
    top: 50px;
    bottom: 70px;
    width: 240px;
  }

  .menu-title {
    padding: 15px;
    font-size: 14px;
  }

  .main-content {
    padding: 15px 10px 10px;
  }

  .loading-container {
    height: 300px;
  }

  .fixed-player {
    height: 70px;
  }
}
</style>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  background: #f0f2f5;
}
</style>
