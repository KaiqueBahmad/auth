package kaiquebt.dev.auth.service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import kaiquebt.dev.auth.model.BaseUser;

@Component
public class JwtTokenProvider {

    @Value("${kaiquebt.dev.auth.jwt-secret}")
    private String jwtSecret;

    @Value("${kaiquebt.dev.auth.jwt-expiration-milliseconds}")
    private long jwtExpirationInMs;

    public static class GeneratedTokenResponse {
        public GeneratedTokenResponse(String token, BaseUser user) {
            this.user = user;
            this.token = token;
        }

        public String token;
        public BaseUser user;
    }

    public GeneratedTokenResponse generateToken(BaseUser user ) {
        return generateToken(user, Map.of());
    }

    public GeneratedTokenResponse generateToken(BaseUser user,  Map<String, Object> extraClaims ) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationInMs);
        
        
        String token = Jwts.builder()
            .setSubject(user.getEmail())
            .setIssuedAt(currentDate)
            .setExpiration(expireDate)
            .claim("username", user.getUsername())
            .claim("roles", user.getRoles())
            .claim("email", user.getEmail())
            .addClaims(extraClaims)
            .signWith(key())
        .compact();
        return new GeneratedTokenResponse(token, user);
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException ex) {
            throw new IllegalArgumentException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new IllegalArgumentException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new IllegalArgumentException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT claims string is empty.");
        }
    }
}
