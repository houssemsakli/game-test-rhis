package com.rhis.solutions.controllers;

import com.rhis.solutions.entities.Game;
import com.rhis.solutions.entities.Move;
import com.rhis.solutions.repositories.GameRepository;
import com.rhis.solutions.repositories.MoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameRestController {
    @Autowired
    private MoveRepository moveRepo;

    @Autowired
    private GameRepository gameRepo;

    @GetMapping("/{id}/moves")
    public List<Move> getMoves(@PathVariable Long id) {
        return moveRepo.findByGameIdOrderByMoveNumberAsc(id);
    }

    @GetMapping("/{id}")
    public Game getGame(@PathVariable Long id) {
        return gameRepo.findById(id).orElse(null);
    }
}

