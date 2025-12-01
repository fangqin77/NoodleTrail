package com.example.noodletrail.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    // HS256 要求密钥长度至少 256 位，这里使用足够长的字符串并通过 Keys.hmacShaKeyFor 生成 SecretKey
    // 生产环境建议将 SECRET 放到配置或环境变量中
    private static final String SECRET = "NoodleTrail-HS256-SecretKey-2024-ChangeMe-1234567890";
    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private static final long EXP = 1000L * 60 * 60 * 24 * 7; // 7 天

    public static String generate(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(System.currentTimeMillis() + EXP))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Long parseUserId(String token) {
        var body = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(body.getSubject());
    }
}