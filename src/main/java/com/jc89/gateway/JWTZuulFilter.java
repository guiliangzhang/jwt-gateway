package com.jc89.gateway;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class JWTZuulFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<JWTVerifier> jwtVerifiers;
    private final String header;

    public JWTZuulFilter(List<JWTVerifier> jwtVerifiers, String header) {
        Preconditions.checkArgument(!jwtVerifiers.isEmpty());
        
        this.jwtVerifiers = jwtVerifiers;
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
        String token = MoreObjects.firstNonNull(request.getHeader(header), "");
        for(JWTVerifier jwtVerifier: jwtVerifiers) {
            try {
                DecodedJWT decodedJWT = jwtVerifier.verify(token);
                ctx.addZuulRequestHeader("jwt-payload", decodedJWT.getPayload());
                ctx.addZuulRequestHeader("jwt-token", decodedJWT.getToken());
                ctx.addZuulRequestHeader("jwt-header", decodedJWT.getHeader());
                return null;
            } catch (Exception e) {
                logger.debug("Failed request " + request.getRequestURI(), e);
            }
        }
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        return null;
    }
}
