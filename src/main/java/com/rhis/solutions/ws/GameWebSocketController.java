package com.rhis.solutions.ws;

import com.rhis.solutions.dtos.InviteMessage;
import com.rhis.solutions.entities.Game;
import com.rhis.solutions.entities.Move;
import com.rhis.solutions.entities.User;
import com.rhis.solutions.repositories.GameRepository;
import com.rhis.solutions.repositories.MoveRepository;
import com.rhis.solutions.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
public class GameWebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private MoveRepository moveRepo;

    @Autowired
    private UserRepository userRepo;

    @MessageMapping("/invite")
    public void invite(InviteMessage msg) {
        log.info("Invite: {} -> {}", msg.getFrom(), msg.getTo());
        try {
            template.convertAndSendToUser(msg.getTo(), "/queue/invite", Map.of("from", msg.getFrom()));
            template.convertAndSend("/topic/invites", (Object) Map.of("from", msg.getFrom(), "to", msg.getTo()));
        } catch (Exception e) {
            log.error("Invite send failed: {} -> {}", msg.getFrom(), msg.getTo(), e);
        }
    }

    @MessageMapping("/invite/response")
    public void inviteResponse(Map<String, Object> payload) {
        boolean accepted = Boolean.parseBoolean(String.valueOf(payload.get("accepted")));
        String from = (String) payload.get("from");
        String to = (String) payload.get("to");

        if (accepted) {
            try {
                Game g = new Game();

                Long whiteId = null;
                Long blackId = null;

                if (from != null) {
                    Optional<User> uFrom = userRepo.findByUsername(from);
                    if (uFrom.isPresent()) {
                        whiteId = uFrom.get().getId();
                    } else {
                        log.warn("inviteResponse: user not found for username '{}'", from);
                    }
                }

                if (to != null) {
                    Optional<User> uTo = userRepo.findByUsername(to);
                    if (uTo.isPresent()) {
                        blackId = uTo.get().getId();
                    } else {
                        log.warn("inviteResponse: user not found for username '{}'", to);
                    }
                }

                g.setWhiteUserId(whiteId);
                g.setBlackUserId(blackId);
                g.setStatus("IN_PROGRESS");
                gameRepo.save(g);

                template.convertAndSendToUser(from, "/queue/game", Map.of("gameId", g.getId()));
                template.convertAndSendToUser(to, "/queue/game", Map.of("gameId", g.getId()));
                template.convertAndSend("/topic/game.created", (Object) Map.of("from", from, "to", to, "gameId", g.getId()));

                log.info("Game created: id={} ({}[{}] ↔ {}[{}])",
                        g.getId(),
                        from, whiteId,
                        to, blackId);
            } catch (Exception e) {
                log.error("Game creation failed for invite {} -> {}", from, to, e);
            }
        } else {
            try {
                template.convertAndSendToUser(from, "/queue/invite.response", Map.of("accepted", false, "from", from, "to", to));
                template.convertAndSend("/topic/invite.response", (Object) Map.of("accepted", false, "from", from, "to", to));
                log.info("Invite refused: {} -> {}", to, from);
            } catch (Exception e) {
                log.error("Sending invite refusal failed for {} -> {}", from, to, e);
            }
        }
    }

    @MessageMapping("/game/{gameId}/move")
    public void playMove(@DestinationVariable Long gameId, Map<String, Object> moveMsg, Principal principal) {
        try {
            Move mv = new Move();
            mv.setGameId(gameId);
            Integer last = moveRepo.findTopByGameIdOrderByMoveNumberDesc(gameId)
                    .map(Move::getMoveNumber)
                    .orElse(0);
            mv.setMoveNumber(last + 1);
            mv.setFromCell((String) moveMsg.get("from"));
            mv.setToCell((String) moveMsg.get("to"));
            mv.setPiece((String) moveMsg.getOrDefault("piece", ""));

            // Déterminer playedBy :
            Long playerId = null;
            if (principal != null) {
                String username = principal.getName();
                try {
                    Optional<User> uOpt = userRepo.findByUsername(username);
                    if (uOpt.isPresent()) {
                        playerId = uOpt.get().getId();
                    } else {
                        log.warn("playMove: Principal username '{}' not found in DB", username);
                    }
                } catch (Exception e) {
                    log.warn("playMove: error resolving user by principal '{}': {}", principal.getName(), e.getMessage());
                }
            }

            // fallback : utiliser playedBy fourni dans le message (si présent)
            if (playerId == null) {
                Object pb = moveMsg.get("playedBy");
                if (pb != null) {
                    try {
                        if (pb instanceof Number) {
                            playerId = ((Number) pb).longValue();
                        } else {
                            playerId = Long.parseLong(String.valueOf(pb));
                        }
                    } catch (NumberFormatException nfe) {
                        log.warn("playMove: cannot parse playedBy from message: {}", pb);
                    }
                }
            }

            mv.setPlayedBy(playerId);

            moveRepo.save(mv);

            // diffuser le move (inclut now playedBy si non null)
            template.convertAndSend("/topic/game." + gameId, (Object) Map.of("type", "MOVE_PLAYED", "move", mv));
            log.info("Move saved: game={} move#{} {}->{} by {}",
                    gameId, mv.getMoveNumber(), mv.getFromCell(), mv.getToCell(), mv.getPlayedBy());
        } catch (Exception e) {
            log.error("Failed to save move for game {}", gameId, e);
        }
    }
}
