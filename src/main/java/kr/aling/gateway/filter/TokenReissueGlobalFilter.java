package kr.aling.gateway.filter;

import feign.Response;
import java.util.Objects;
import kr.aling.gateway.common.exception.AuthenticationException;
import kr.aling.gateway.common.exception.AuthorizationException;
import kr.aling.gateway.common.jwt.JwtUtils;
import kr.aling.gateway.common.properties.AccessProperties;
import kr.aling.gateway.feignclient.AuthServerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 토큰이 만료된 경우 재발급하여 쿠키에 등록하여주는 필터.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Component
@Slf4j
public class TokenReissueGlobalFilter implements GlobalFilter, Ordered {

    private final AuthServerClient authServerClient;
    private final AccessProperties accessProperties;
    private final JwtUtils jwtUtils;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "jtru";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "jteu";
    private static final String ACCESS_TOKEN_HEADER_NAME = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String USER_HEADER = "X-TEMP-USER-NO";

    public TokenReissueGlobalFilter(@Lazy AuthServerClient authServerClient, AccessProperties accessProperties,
                                    JwtUtils jwtUtils) {
        this.authServerClient = authServerClient;
        this.accessProperties = accessProperties;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 쿠키에서 refresh 토큰이 있는지 확인한 이후, 만료된 토큰에 한해 재발급합니다.
     *
     * @param exchange the current server exchange
     * @param chain provides a way to delegate to the next filter
     * @return Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        if (!cookies.containsKey(REFRESH_TOKEN_COOKIE_NAME)) {
            return chain.filter(exchange);
        }

        if (cookies.containsKey(ACCESS_TOKEN_COOKIE_NAME) &&
                !jwtUtils.isExpiredToken(accessProperties.getSecret(), Objects.requireNonNull(
                        cookies.getFirst(ACCESS_TOKEN_COOKIE_NAME)).getValue())) {
            return chain.filter(exchange);
        }

        try (Response reissueResponse = authServerClient.reissue(
                Objects.requireNonNull(cookies.getFirst(REFRESH_TOKEN_COOKIE_NAME)).getValue())) {
            String accessToken = reissueResponse.headers().get(ACCESS_TOKEN_HEADER_NAME)
                    .stream().findFirst().orElseThrow(AuthorizationException::new)
                    .substring(BEARER.length());

            exchange.getRequest().mutate()
                    .header(USER_HEADER, jwtUtils.parseToken(accessProperties.getSecret(), accessToken).getSubject());

            ResponseCookie accessCookie = ResponseCookie
                    .from(ACCESS_TOKEN_COOKIE_NAME, Objects.requireNonNull(
                            accessToken))
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(3600)
                    .path("/")
                    .build();

            exchange.getResponse().addCookie(accessCookie);

            return chain.filter(exchange);
        } catch (Exception e) {
            throw new AuthenticationException();
        }
    }

    /**
     * 필터의 우선도를 제공합니다.
     *
     * @return -2
     */
    @Override
    public int getOrder() {
        return -2;
    }
}
