package ma.projet.patternai.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            return Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token", e);
            throw new RuntimeException("Could not generate token", e);
        }
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired", e);
            throw new JwtException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported", e);
            throw new JwtException("JWT token is unsupported");
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token", e);
            throw new JwtException("Invalid JWT token");
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature", e);
            throw new JwtException("Invalid JWT signature");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty", e);
            throw new JwtException("JWT claims string is empty");
        } catch (Exception e) {
            logger.error("Error extracting email from token", e);
            throw new JwtException("Error processing JWT token");
        }
    }

    public boolean validateToken(String token, String email) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                logger.error("JWT token is expired");
                return false;
            }

            // Validate email
            return email.equals(claims.getSubject());
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return false;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("Error getting expiration date from token", e);
            throw new JwtException("Error processing JWT token");
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration", e);
            return true;
        }
    }
}