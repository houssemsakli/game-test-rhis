package com.rhis.solutions.repositories;

import com.rhis.solutions.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

}
