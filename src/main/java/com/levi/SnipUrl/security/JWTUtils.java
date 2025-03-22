package com.levi.SnipUrl.security;

import com.levi.SnipUrl.services.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

public class JWTUtils {

    @Value("${token.expiry-duration}")
    public static Long TOKEN_EXPIRY_DURATION ;

    @Value("${token.secret}")
    public static String KEY_SECRET ;

    public String getJwtToken(HttpServletRequest request) {
        if (request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer ")) {
            return request.getHeader("Authorization").substring(7);

        }
        return null;
    }

    public String generateJwtToken(UserDetailsImpl userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(TOKEN_EXPIRY_DURATION)))
                .signWith(getKey())
                .compact();
    }

    public String getUserNameFromJWTToken(String token){
        return Jwts.parser().verifyWith((SecretKey) getKey())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public boolean validateJWTtoken(String token){
        try {
            Jwts.parser().verifyWith((SecretKey) getKey())
                    .build().parseSignedClaims(token);
            return  true;
        } catch (JwtException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Key getKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(KEY_SECRET));
    }
}
