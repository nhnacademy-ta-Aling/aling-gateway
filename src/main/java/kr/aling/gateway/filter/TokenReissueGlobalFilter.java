package kr.aling.gateway.filter;

import static kr.aling.gateway.common.jwt.AuthUtils.ACCESS_TOKEN_EXPIRE;
import static kr.aling.gateway.common.jwt.AuthUtils.makeTokenCookie;

import feign.Response;
import java.util.Objects;
import kr.aling.gateway.common.enums.CookieNames;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.common.exception.AuthorizationException;
import kr.aling.gateway.common.jwt.AuthUtils;
import kr.aling.gateway.common.jwt.JwtUtils;
import kr.aling.gateway.common.properties.AccessProperties;
import kr.aling.gateway.feignclient.AuthServerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
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
    private final AuthUtils authUtils;
    private static final String BEARER = "Bearer ";

    public TokenReissueGlobalFilter(@Lazy AuthServerClient authServerClient, AccessProperties accessProperties,
                                    JwtUtils jwtUtils, AuthUtils authUtils) {
        this.authServerClient = authServerClient;
        this.accessProperties = accessProperties;
        this.jwtUtils = jwtUtils;
        this.authUtils = authUtils;
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
        if (!cookies.containsKey(CookieNames.REFRESH_TOKEN.getName())) {
            return chain.filter(exchange);
        }

        if (cookies.containsKey(CookieNames.ACCESS_TOKEN.getName()) &&
                !jwtUtils.isExpiredToken(accessProperties.getSecret(), Objects.requireNonNull(
                        cookies.getFirst(CookieNames.ACCESS_TOKEN.getName())).getValue())) {
            return chain.filter(exchange);
        }

        try (Response reissueResponse = authServerClient.reissue(
                Objects.requireNonNull(cookies.getFirst(CookieNames.REFRESH_TOKEN.getName())).getValue())) {

            String accessToken = reissueResponse.headers().get(HeaderNames.ACCESS_TOKEN.getName())
                    .stream().findFirst().orElseThrow(() -> new AuthorizationException(HttpStatus.UNAUTHORIZED, "토큰이 없습니다."))
                    .substring(BEARER.length());

            authUtils.addHeaderFromAccessToken(exchange.getRequest(), accessToken);

            ResponseCookie accessCookie =
                    makeTokenCookie(CookieNames.ACCESS_TOKEN, accessToken,
                            ACCESS_TOKEN_EXPIRE);

            exchange.getResponse().addCookie(accessCookie);

            return chain.filter(exchange);
        } catch (Exception e) {
            throw new AuthorizationException(HttpStatus.UNAUTHORIZED, "발급에 실패하였습니다.");
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
