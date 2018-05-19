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
    private static final String VALID_TOKEN= "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRHNvZSIsImFkbWluIjp0cnVlLCJpc3MiOiJqb2VsIn0.Eac42zNJZ5v_wxAk4-uxM_h6F6rO6rljBTbZpAVz02RwCHo8rAmSe8AqV_og1fVie_dL1MKwK2T4byX56x43RH3DbH5vjJH6HquzJMxIhZ_zP5bUM-yhK6ycMY3QHH4g_UukVYFGDrlPt2dNIv11zB_o44R3A8SQCEL1hlsOUKyHaPffPRV5m6zUZ9Zmu2iGN_HA-VUVDkhdZ99rxTNoOd8nof3k3v_TPouO765LAz8iYhl1SERwGOPp8bDrjPq32FO9AAm5LQN6Z4Uzxf3v7Cwz6_z2oslJx0pNv_r-_5woUVUFU6zaIWr557-wO63H7IozDNONBRm7S7pqgMV2_NNxuejum2kV9mzXuZd0U3owQ5E7qcxBIXLWaO2wh9783G5H5-geXO7HF_3f2QETqHDm1vSjlHYhh9JdWOwueSae-jkEZArmZ_P6B-G0XLYlsQcfB0FzLfaURXlpAJkgimfAiv8fAmvbtRabakEGv8GRvsda0P_8OhkACycsQE6GNWQ95j_eA0sT5FSmjwdjvjLxFifMwi-Y1a9P49v_ruXiv0oXg9t_TvtR1_HFWTcj7fpZwjbT7yuW0gK4_gu1Qqc4VQW49asrK8mOz2g8Bz9Ruv29_KbgSKShRUmV_2Tdx8nAw2WxSs2IQKG5Gk2omNPbqGjss9rEsYCDVpNjgCY";

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
                .withIssuer("joel")
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