package ma.projet.patternai.service;

import ma.projet.patternai.entities.Space;
import ma.projet.patternai.entities.User;
import ma.projet.patternai.repo.SpaceRepository;
import ma.projet.patternai.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SpaceService {
    private static final Logger logger = LoggerFactory.getLogger(SpaceService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Transactional(readOnly = true)
    public List<Space> getUserSpaces(String userEmail) {
        logger.debug("Fetching spaces for user email: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
        return spaceRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Space createSpace(Space space, String userEmail) {
        logger.debug("Creating space for user email: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        space.setUser(user);
        space.setCreatedAt(LocalDateTime.now());
        return spaceRepository.save(space);
    }

    @Transactional
    public void deleteSpace(UUID spaceId, String userEmail) {
        logger.debug("Deleting space {} for user {}", spaceId, userEmail);
        Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                .orElseThrow(() -> new RuntimeException("Space not found or unauthorized access"));
        spaceRepository.delete(space);
    }
}