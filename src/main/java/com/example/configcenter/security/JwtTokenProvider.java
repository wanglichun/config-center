package com.example.configcenter.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT Token提供者
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    /**
     * 生成JWT Token
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
        
        return JWT.create()
                .withSubject(Long.toString(userPrincipal.getId()))
                .withClaim("username", userPrincipal.getUsername())
                .withClaim("role", userPrincipal.getRole())
                .withIssuedAt(new Date())
                .withExpiresAt(expiryDate)
                .withIssuer("config-center")
                .sign(algorithm);
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("config-center")
                    .build();
            
            DecodedJWT jwt = verifier.verify(token);
            return Long.parseLong(jwt.getSubject());
        } catch (JWTVerificationException e) {
            log.error("获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("config-center")
                    .build();
            
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("username").asString();
        } catch (JWTVerificationException e) {
            log.error("获取用户名失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("config-center")
                    .build();
            
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("role").asString();
        } catch (JWTVerificationException e) {
            log.error("获取角色失败", e);
            return null;
        }
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String authToken) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("config-center")
                    .build();
            
            verifier.verify(authToken);
            return true;
        } catch (JWTVerificationException e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("config-center")
                    .build();
            
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            log.error("检查Token过期失败", e);
            return true;
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("config-center")
                    .build();
            
            DecodedJWT jwt = verifier.verify(token);
            
            Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
            
            return JWT.create()
                    .withSubject(jwt.getSubject())
                    .withClaim("username", jwt.getClaim("username").asString())
                    .withClaim("role", jwt.getClaim("role").asString())
                    .withIssuedAt(new Date())
                    .withExpiresAt(expiryDate)
                    .withIssuer("config-center")
                    .sign(algorithm);
        } catch (JWTVerificationException e) {
            log.error("刷新Token失败", e);
            return null;
        }
    }
} 