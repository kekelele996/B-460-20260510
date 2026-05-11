package com.musicplayer.repository;

import com.musicplayer.entity.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    @Query("SELECT ph FROM PlayHistory ph JOIN FETCH ph.song ORDER BY ph.playedAt DESC")
    List<PlayHistory> findRecentPlayHistory();

    Optional<PlayHistory> findBySongId(Long songId);

    @Modifying
    @Query("DELETE FROM PlayHistory ph WHERE ph.songId = :songId")
    void deleteBySongId(@Param("songId") Long songId);
}
