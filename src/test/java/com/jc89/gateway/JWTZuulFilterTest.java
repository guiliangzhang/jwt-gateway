package com.jc89.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JWTZuulFilterTest {

    private static final String HEADER = "header";
    private static final String SECRET = "18732kybhkbfkhbdfsdfs";
    private static final String VALID_TOKEN= "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRHNvZSIsImFkbWluIjp0cnVlLCJpc3MiOiJqb2VsIn0.r7eJ-6Cebw0Ep9XY-cHCJ-jufUaVDVfjG3gsjDZpiEk";

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    JWTVerifier jwtVerifier;
    RequestContext context;

    JWTZuulFilter filter;


    @Before
    public void setup() throws UnsupportedEncodingException {
        jwtVerifier = JWT
                .require(Algorithm.HMAC256(SECRET))
                .withIssuer("joel")
                .build();
        context = new RequestContext();
        context.setRequest(request);
        RequestContext.testSetCurrentContext(context);
        filter = new JWTZuulFilter(jwtVerifier, HEADER);
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