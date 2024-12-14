package ma.projet.patternai.controller;

import ma.projet.patternai.config.JwtUtil;
import ma.projet.patternai.entities.User;
import ma.projet.patternai.repo.UserRepository;
import ma.projet.patternai.requests.LoginRequest;
import ma.projet.patternai.requests.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Map<String, String> login(@RequestBody LoginRequest request) {
        Map<String, String> response = new HashMap<>();

        // Find the user by email
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty() || !passwordEncoder.matches(request.getMotdepasse(), user.get().getMotdepasse())) {
            response.put("message", "Invalid email or password.");
            return response;
        }

        // Generate JWT Token
        String token = jwtUtil.generateToken(user.get().getEmail());
        response.put("message", "Login successful.");
        response.put("token", token);
        return response;
    }
}