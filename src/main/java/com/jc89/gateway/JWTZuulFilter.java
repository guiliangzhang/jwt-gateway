package com.jc89.gateway;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.MoreObjects;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

public class JWTZuulFilter extends ZuulFilter {

    Logger logger = LoggerFactory.getLogger(getClass());

    private final JWTVerifier jwtVerifier;
    private final String header;

    public JWTZuulFilter(JWTVerifier jwtVerifier, String header) {
        this.jwtVerifier = jwtVerifier;
        this.header = header;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try {
            String token = MoreObjects.firstNonNull(request.getHeader(header), "");
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            ctx.addZuulRequestHeader("jwt-payload", decodedJWT.getPayload());
            ctx.addZuulRequestHeader("jwt-token", decodedJWT.getToken());
            ctx.addZuulRequestHeader("jwt-header", decodedJWT.getHeader());
        } catch (Exception e) {
            logger.info("Failed request " + request.getRequestURI(), e);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}
