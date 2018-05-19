package com.jc89.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.netflix.zuul.ZuulFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public ZuulFilter jwtZuulFilter(
            @Value("${jwt.public_keys_paths}") String paths,
            @Value("${jwt.issuer}") String issuer,
            @Value("${filter.header}") String header
    ) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        List<JWTVerifier> verifierList = Lists.newArrayList();
        for(String path: Splitter.on(",").trimResults().split(paths)) {
            byte[] keyBytes = Files.readAllBytes(Paths.get(path));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey)kf.generatePublic(spec);
            verifierList.add(
                    JWT
                    .require(Algorithm.RSA512(publicKey, null))
                    .withIssuer(issuer)
                    .build()
            );
        }

        return new JWTZuulFilter(verifierList, header);
    }

}