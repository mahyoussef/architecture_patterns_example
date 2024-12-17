package com.charity_hub.infrastructure.services;

import com.charity_hub.domain.contracts.IJWTGenerator;
import com.charity_hub.domain.contracts.ILogger;
import com.charity_hub.domain.models.account.Account;
import com.charity_hub.domain.models.device.Device;
import com.charity_hub.infrastructure.mappers.TokenMapper;
import com.charity_hub.api.authconfig.JWTPayload;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class JwtGenerator implements IJWTGenerator {

    @Value("${auth.secretKey}")
    private String secretKey;
    private final ILogger logger;

    public JwtGenerator(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public String generateAccessToken(Account account, Device device) {
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.add(Calendar.MINUTE, 5);
        JWTPayload jwtPayload = TokenMapper.toAccessToken(
            account, 
            device, 
            new Date(expiryDate.getTimeInMillis())
        );
        logger.info("jwt id: ".formatted(jwtPayload.getJwtId()));
        return createToken(jwtPayload);
    }

    @Override
    public String generateRefreshToken(Account account, Device device) {
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.add(Calendar.YEAR, 1);
        JWTPayload jwtPayload = TokenMapper.toRefreshToken(
            account, 
            device, 
            new Date(expiryDate.getTimeInMillis())
        );
        return createToken(jwtPayload);
    }

    private String createToken(JWTPayload jwtPayload) {
        var builder = Jwts.builder()
            .subject(jwtPayload.getSubject())
            .issuedAt(jwtPayload.getIssuedAt())
            .issuer(jwtPayload.getIssuer())
            .audience().add(jwtPayload.getAudience()).and()
            .id(jwtPayload.getJwtId())
            .expiration(jwtPayload.getExpireAt())
            .id(jwtPayload.getJwtId())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()));

        for (Map.Entry<String, Object> entry : jwtPayload.toMap().entrySet()) {
            builder.claims().add(entry.getKey(), entry.getValue());
        }

        return builder.compact();
    }
}