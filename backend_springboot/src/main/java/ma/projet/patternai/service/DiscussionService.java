package ma.projet.patternai.service;

import ma.projet.patternai.entities.Discussion;
import ma.projet.patternai.entities.Space;
import ma.projet.patternai.repo.DiscussionRepository;
import ma.projet.patternai.repo.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscussionService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussionService.class);

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Transactional
    public Discussion saveDiscussion(UUID spaceId, String message, String userEmail, String messageType) {
        try {
            Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                    .orElseThrow(() -> new RuntimeException("Space not found or unauthorized access"));

            Discussion discussion = new Discussion();
            discussion.setMessage(message);
            discussion.setCreatedAt(LocalDateTime.now());
            discussion.setSpace(space);
            discussion.setMessageType(messageType);

            return discussionRepository.save(discussion);
        } catch (Exception e) {
            logger.error("Error saving discussion for space {}: {}", spaceId, e.getMessage());
            throw new RuntimeException("Failed to save discussion", e);
        }
    }

    /**
     * Get complete conversation history as pairs of user queries and model responses
     */
    public List<Map<String, Object>> getConversationHistory(UUID spaceId, String userEmail) {
        try {
            Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                    .orElseThrow(() -> new RuntimeException("Space not found or unauthorized access"));

            List<Discussion> discussions = discussionRepository.findBySpaceAndMessageTypeOrderByCreatedAtDesc(space, "CHAT");
            List<Map<String, Object>> conversationPairs = new ArrayList<>();

            // Process discussions into pairs
            for (int i = 0; i < discussions.size(); i += 2) {
                Map<String, Object> pair = new HashMap<>();
                // Add user message
                if (i < discussions.size()) {
                    Discussion userMsg = discussions.get(i);
                    pair.put("userMessage", userMsg);
                }
                // Add corresponding model response
                if (i + 1 < discussions.size()) {
                    Discussion modelMsg = discussions.get(i + 1);
                    pair.put("assistantMessage", modelMsg);
                }
                conversationPairs.add(pair);
            }

            return conversationPairs;
        } catch (Exception e) {
            logger.error("Error fetching conversation history for space {}: {}", spaceId, e.getMessage());
            throw new RuntimeException("Failed to fetch conversation history", e);
        }
    }

    /**
     * Get the last model response for a specific user query
     */
    public Optional<String> getLastModelResponse(UUID spaceId, String userEmail) {
        try {
            Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                    .orElseThrow(() -> new RuntimeException("Space not found or unauthorized access"));

            return discussionRepository
                    .findFirstBySpaceAndMessageTypeOrderByCreatedAtDesc(space, "CHAT")
                    .map(Discussion::getMessage)
                    .filter(msg -> msg.startsWith("Assistant: "));
        } catch (Exception e) {
            logger.error("Error fetching last model response for space {}: {}", spaceId, e.getMessage());
            throw new RuntimeException("Failed to fetch last model response", e);
        }
    }

    public List<Discussion> getSpaceDiscussions(UUID spaceId, String userEmail) {
        try {
            Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                    .orElseThrow(() -> new RuntimeException("Space not found or unauthorized access"));

            return discussionRepository.findBySpaceOrderByCreatedAtDesc(space);
        } catch (Exception e) {
            logger.error("Error fetching discussions for space {}: {}", spaceId, e.getMessage());
            throw new RuntimeException("Failed to fetch discussions", e);
        }
    }

    public List<String> getPreviousDiscussions(UUID spaceId, String userEmail) {
        try {
            Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                    .orElseThrow(() -> new RuntimeException("Space not found or unauthorized access"));

            return discussionRepository.findBySpaceAndMessageTypeOrderByCreatedAtDesc(space, "CHAT")
                    .stream()
                    .map(Discussion::getMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching previous discussions for space {}: {}", spaceId, e.getMessage());
            throw new RuntimeException("Failed to fetch previous discussions", e);
        }
    }

    @Transactional
    public void deleteSpaceDiscussions(UUID spaceId, String userEmail) {
        try {
            Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                    .orElseThrow(() -> new RuntimeException("Space not found or unauthorized access"));

            discussionRepository.deleteBySpace(space);
        } catch (Exception e) {
            logger.error("Error deleting discussions for space {}: {}", spaceId, e.getMessage());
            throw new RuntimeException("Failed to delete discussions", e);
        }
    }
}