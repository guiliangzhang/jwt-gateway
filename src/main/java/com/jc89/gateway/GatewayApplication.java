package com.jc89.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.netflix.zuul.ZuulFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import java.io.UnsupportedEncodingException;

@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public ZuulFilter simpleFilter(@Value("${jwt.secret}") String secret,
                                   @Value("${jwt.issuer}") String issuer,
                                   @Value("${filter.header}") String header)
            throws UnsupportedEncodingException {
        JWTVerifier jwtVerifier = JWT
                .require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .build();

        return new JWTZuulFilter(jwtVerifier, header);
    }

}