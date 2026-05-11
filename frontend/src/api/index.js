import axios from 'axios'

const api = axios.create({
  baseURL: '/',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API 请求失败:', error)
    return Promise.reject(error)
  }
)

export const songApi = {
  getAllSongs() {
    return api.get('/api/songs')
  },

  getSongById(id) {
    return api.get(`/api/songs/${id}`)
  },

  searchSongs(keyword) {
    return api.get('/api/songs/search', { params: { keyword } })
  },

  getMostPlayedSongs() {
    return api.get('/api/songs/most-played')
  },

  getLikedSongs() {
    return api.get('/api/songs/liked')
  },

  incrementPlayCount(id) {
    return api.post(`/api/songs/${id}/play`)
  },

  toggleLike(id) {
    return api.post(`/api/songs/${id}/like`)
  }
}

export const playlistApi = {
  getAllPlaylists() {
    return api.get('/api/playlists')
  },

  getPlaylistById(id) {
    return api.get(`/api/playlists/${id}`)
  },

  createPlaylist(name, description) {
    return api.post('/api/playlists', null, { params: { name, description } })
  },

  updatePlaylist(id, name, description) {
    return api.put(`/api/playlists/${id}`, null, { params: { name, description } })
  },

  deletePlaylist(id) {
    return api.delete(`/api/playlists/${id}`)
  },

  addSongToPlaylist(playlistId, songId) {
    return api.post(`/api/playlists/${playlistId}/songs/${songId}`)
  },

  removeSongFromPlaylist(playlistId, songId) {
    return api.delete(`/api/playlists/${playlistId}/songs/${songId}`)
  }
}
