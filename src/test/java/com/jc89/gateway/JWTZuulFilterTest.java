package com.jc89.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.io.ByteStreams;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JWTZuulFilterTest {

    private static final String HEADER = "header";
    private static final String VALID_TOKEN = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRHNvZSIsImFkbWluIjp0cnVlLCJpc3MiOiJsb2wifQ.C8LCKIkPneqvfSt6rqpxrWUHAyCEyoe-irhIwcPrWwGcFjg-imBNP1Z524j1faxu0W3SPm4Fsb5UfPAzDdyLnMMyjQ5nMsqcf1bFgAWMiKCu_qTBK_gwE-ninhTo99HTorxhBo9f98dheSrCg59IrPWv4P-AM24GE3v94RBWqW8pg8cH9SnxMBfmo3711wvRfYKecBm3AgpxTvlzrO35U1NpaAGV4jYl43MooBlRm7D9KQ3iMFhCmWDGF_FLymM_CjcsVr4c8aYxfEJwkpUoj7E7ei1XuKa6noWyQgOEbWJHtZmw4RA1zTy6clP7Pioxk6UoQidFXIZgGcdldZoCZNPd87xiegFMQeVn4UimQ8MuLQqaIofw6goOpyrTeZm3n85t_dffLYHGPcty84YoaeTcH4pzlOk6vZQDXMo1z7XMXD8GOhkjEmgX0Q4HUvzbDTvq_GrzPg4HmbZQ3tVWyEYvyjdNw5hVIz60ORdXcwb5cuqDANn8gEZMZCkVILIpzsBBmA82jd8ai4LMBBMpLgTLg3LbsUj5zOJ_Ydh5YeoAqWlcq0pnwtZybH9zLPD8ZpJSaZnGatF2zw6PWtqwfeIBs21IlAlILJPCAqrcTtOUjbrcJ8hK6vI6Jbq5Zn4Ctsr-rC-dbFPvPoWaiQtqB5ED0gR9L3ZCJL0NooZO9lU";

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    JWTVerifier jwtVerifier;
    RequestContext context;

    JWTZuulFilter filter;


    @Before
    public void setup() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = ByteStreams.toByteArray(getClass().getClassLoader().getResourceAsStream("public_key.der"));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey)kf.generatePublic(spec);

        jwtVerifier = JWT
                .require(Algorithm.RSA512(publicKey, null))
                .withIssuer("lol")
                .build();
        context = new RequestContext();
        context.setRequest(request);
        RequestContext.testSetCurrentContext(context);
        filter = new JWTZuulFilter(Lists.newArrayList(jwtVerifier), HEADER);
    }

    @Test
    public final void testShouldSetUnauthorizedOnInvalidToken() {
        when(request.getHeader(HEADER)).thenReturn("whatever");
        context.setResponse(response);
        context.set("sendZuulResponse", true);

        filter.run();

        assertThat(context.getBoolean("sendZuulResponse")).isFalse();
        assertThat(context.getResponseStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public final void testShouldNotDoAnythingOnValidToken() {
        when(request.getHeader(HEADER)).thenReturn(VALID_TOKEN);
        context.setResponse(response);

        filter.run();

        assertThat(context.getResponseStatusCode()).isNotEqualTo(HttpStatus.SC_UNAUTHORIZED);
    }


}
