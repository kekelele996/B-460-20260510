package com.musicplayer.repository;

import com.musicplayer.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByArtistContaining(String artist);

    List<Song> findByAlbumContaining(String album);

    List<Song> findByGenre(String genre);

    @Query("SELECT s FROM Song s ORDER BY s.playCount DESC")
    List<Song> findMostPlayed();

    @Query("SELECT s FROM Song s WHERE s.isLiked = true")
    List<Song> findLikedSongs();
}
