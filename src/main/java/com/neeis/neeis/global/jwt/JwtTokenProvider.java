package com.neeis.neeis.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final CustomUserDetailsService customUserDetailsService;

    @Value("${spring.jwt.secret}")
    private String key;

    private SecretKey secretKey;

    @Value("${spring.jwt.access-token-valid-time}")
    private Long accessTokenValidTime;

    @PostConstruct
    protected void init() {this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));}

    public String createAccessToken(String username, String role){
        Claims claims = (Claims) Jwts.claims().setSubject(username);
        claims.put("role", role);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+ accessTokenValidTime))
                .signWith(secretKey)
                .compact();
    }

    //SecuriyContextHolder 저장 객체(Authentication) 생성
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(this.getUsername(token));
        log.info("JwtTokenProvider.getAuthentication - {} ", userDetails.getUsername());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // username - subject에 들어감
    public String getUsername(String token) {
        String username = parseClaims(token).getSubject();
        log.info("JwtTokenProvider.getUsername - {}", username);
        return username;
    }
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Claims parseClaims(String token){
        try{
            Claims claims = Jwts.parserBuilder().setSigningKey(this.secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.info("JwtTokenProvider.parseClaims - {}", claims);
            return claims;
        }catch (ExpiredJwtException e){
            log.error("JwtTokenProvider.parseClaims - 토큰 만료");
        }catch (JwtException e){
            log.error("JwtTokenProvider.parseClaims - {}", e.getMessage());
        }
        return null;
    }
}
