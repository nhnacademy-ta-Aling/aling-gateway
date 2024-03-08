package kr.aling.gateway.filter;

import java.util.Objects;
import java.util.Optional;
import kr.aling.gateway.common.enums.CookieNames;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.common.exception.AuthorizationException;
import kr.aling.gateway.common.jwt.AuthUtils;
import kr.aling.gateway.common.properties.AuthGlobalFilterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 토큰이 존재하는지 확인 후 존재하면 요청 헤더에 회원번호를 추가합니다.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class AuthorizationGlobalFilter implements GlobalFilter, Ordered {

    private final AuthGlobalFilterProperties authGlobalFilterProperties;
    private final AuthUtils authUtils;

    /**
     * 쿠키에 Access 토큰이 있는지 확인 후, api로 요청을 보내는 request에 유저 번호 헤더, 권한 헤더를 추가합니다.
     *
     * @param exchange the current server exchange
     * @param chain provides a way to delegate to the next filter
     * @return Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        if (isExcludes(request.getURI().getPath())) {
            return chain.filter(exchange);
        }

        if (Optional.ofNullable(request.getHeaders()
                .getFirst(HeaderNames.USER_NO.getName())).isPresent()) {
            return chain.filter(exchange);
        }

        if (!cookies.containsKey(CookieNames.ACCESS_TOKEN.getName())) {
            throw new AuthorizationException(HttpStatus.UNAUTHORIZED, "토큰이 없습니다.");
        }

        String accessToken = Objects.requireNonNull(cookies.getFirst(CookieNames.ACCESS_TOKEN.getName())).getValue();

        try {
            authUtils.addHeaderFromAccessToken(request, accessToken);
        } catch (Exception e) {
            throw new AuthorizationException(HttpStatus.UNAUTHORIZED, "올바른 토큰이 아닙니다.");
        }

        return chain.filter(exchange);
    }

    /**
     * 필터의 우선도를 제공합니다.
     *
     * @return -1
     */
    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 인가가 필요하지 않은 경로들에 대해 검사합니다.
     *
     * @param path 유저가 접근하려는 경로
     * @return 포함 여부
     */
    private boolean isExcludes(String path) {
        return authGlobalFilterProperties.getGlobalExcludes().contains(path);
    }

}
