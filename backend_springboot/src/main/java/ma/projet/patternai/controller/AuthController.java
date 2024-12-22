package ma.projet.patternai.controller;

import ma.projet.patternai.config.JwtUtil;
import ma.projet.patternai.entities.User;
import ma.projet.patternai.repo.UserRepository;
import ma.projet.patternai.requests.LoginRequest;
import ma.projet.patternai.requests.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Signup Endpoint
    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody SignupRequest request) {
        Map<String, String> response = new HashMap<>();

        // Check if the email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.put("message", "Email already exists.");
            return response;
        }

        // Save the new user
        User newUser = new User();
        newUser.setNom(request.getNom());
        newUser.setEmail(request.getEmail());
        newUser.setMotdepasse(passwordEncoder.encode(request.getMotdepasse()));
        userRepository.save(newUser);

        response.put("message", "Signup successful.");
        return response;
    }

    // Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Find the user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getMotdepasse(), userOpt.get().getMotdepasse())) {
            response.put("message", "Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail());

        // Add user details to response
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("email", user.getEmail());
        userDetails.put("nom", user.getNom());

        response.put("message", "Login successful.");
        response.put("token", token);
        response.put("user", userDetails);

        return ResponseEntity.ok(response);
    }
}