package com.rhis.solutions.repositories;

import com.rhis.solutions.entities.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoveRepository extends JpaRepository<Move, Long> {
    List<Move> findByGameIdOrderByMoveNumberAsc(Long gameId);
    Optional<Move> findTopByGameIdOrderByMoveNumberDesc(Long gameId);
}
