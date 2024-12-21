package ma.projet.patternai.controller;

import ma.projet.patternai.entities.Space;
import ma.projet.patternai.service.SpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/spaces")
public class SpaceController {
    private static final Logger logger = LoggerFactory.getLogger(SpaceController.class);

    @Autowired
    private SpaceService spaceService;

    @GetMapping
    public ResponseEntity<List<Space>> getUserSpaces(Authentication auth) {
        String userEmail = auth.getName();
        logger.debug("Getting spaces for user: {}", userEmail);
        return ResponseEntity.ok(spaceService.getUserSpaces(userEmail));
    }

    @PostMapping
    public ResponseEntity<Space> createSpace(@RequestBody Space space, Authentication auth) {
        String userEmail = auth.getName();
        logger.debug("Creating space for user: {}", userEmail);
        return ResponseEntity.ok(spaceService.createSpace(space, userEmail));
    }

    @DeleteMapping("/{spaceId}")
    public ResponseEntity<?> deleteSpace(@PathVariable UUID spaceId, Authentication auth) {
        String userEmail = auth.getName();
        logger.debug("Deleting space {} for user: {}", spaceId, userEmail);
        spaceService.deleteSpace(spaceId, userEmail);
        return ResponseEntity.ok().build();
    }
}