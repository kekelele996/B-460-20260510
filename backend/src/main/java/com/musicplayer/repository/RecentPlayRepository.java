package com.musicplayer.repository;

import com.musicplayer.entity.RecentPlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentPlayRepository extends JpaRepository<RecentPlay, Long> {

    Optional<RecentPlay> findBySongId(Long songId);

    @Query("SELECT rp FROM RecentPlay rp JOIN FETCH rp.song ORDER BY rp.playedAt DESC")
    List<RecentPlay> findAllWithSongOrderByPlayedAtDesc();
}
