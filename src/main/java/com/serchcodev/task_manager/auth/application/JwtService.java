package com.serchcodev.task_manager.auth.application;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.serchcodev.task_manager.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppProperties properties;

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(properties.jwt().expirationMinutes() * 60)))
                .build();
        try {
            SignedJWT token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            token.sign(new MACSigner(properties.jwt().secret().getBytes()));
            return token.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create JWT", exception);
        }
    }

    public String extractUsername(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(properties.jwt().secret().getBytes()))) {
                return null;
            }
            if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                return null;
            }
            return jwt.getJWTClaimsSet().getSubject();
        } catch (Exception exception) {
            return null;
        }
    }
}