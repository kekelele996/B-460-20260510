package com.musicplayer.repository;

import com.musicplayer.entity.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    Optional<PlayHistory> findBySongId(Long songId);

    List<PlayHistory> findAllByOrderByPlayedAtDesc();
}
