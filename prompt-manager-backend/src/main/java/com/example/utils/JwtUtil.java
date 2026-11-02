package com.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@org.springframework.stereotype.Component
public class JwtUtil {

    private final SecretKey key;
    public JwtUtil(@org.springframework.beans.factory.annotation.Value("${security.jwt-secret:}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalArgumentException("请通过 JWT_SECRET 配置至少 32 字节的随机 JWT 签名密钥");
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static final long EXPIRE_TIME = 1000L * 60 * 60 * 24;

    public String createToken(Long userId, String userName) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userName", userName)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
