package com.rhis.solutions.controllers;

import com.rhis.solutions.entities.Move;
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

    @GetMapping("/{id}/moves")
    public List<Move> getMoves(@PathVariable Long id) {
        return moveRepo.findByGameIdOrderByMoveNumberAsc(id);
    }
}

