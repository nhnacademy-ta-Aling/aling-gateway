package kr.aling.gateway.filter;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import kr.aling.gateway.common.jwt.JwtUtils;
import kr.aling.gateway.common.properties.AccessProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom GatewayFilterFactory. JWT Access Token을 바탕으로 인가된 요청인지 확인하는 필터입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Component
public class AuthorizationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthorizationGatewayFilterFactory.Config> {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "jteu";

    private final AccessProperties accessProperties;
    private final JwtUtils jwtUtils;

    public AuthorizationGatewayFilterFactory(AccessProperties accessProperties, JwtUtils jwtUtils) {
        super(Config.class);
        this.accessProperties = accessProperties;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 토큰을 통해 요청의 권한을 확인합니다.
     *
     * @param config 설정 정보
     * @return 다음 필터로 요청 전달
     * @author 이수정
     * @since 1.0
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            MultiValueMap<String, HttpCookie> cookies = request.getCookies();

            String subPath = request.getPath().subPath(6).value();
            if (config.getExcludes() != null && config.getExcludes().stream().anyMatch(subPath::matches)) {
                return chain.filter(exchange);
            }

            String accessToken = Objects.requireNonNull(
                    cookies.getFirst(ACCESS_TOKEN_COOKIE_NAME)).getValue();

            Claims claims = jwtUtils.parseToken(accessProperties.getSecret(), accessToken);
            List<String> roles = (List<String>) claims.get("roles");
            if (roles.stream().noneMatch(role -> config.roles.contains(role))) {
                return forbiddenWriteWith(exchange, "요청 권한 : " + roles + ", 필요 권한 : " + config.getRoles());
            }

            return chain.filter(exchange);
        });
    }

    /**
     * 403 응답을 설정하고, 응답 메세지를 담아 보냅니다.
     *
     * @param exchange ServerHttpResponse를 얻기 위한 ServerWebExchange 객체
     * @param message  응답 메세지
     * @return 403 메세지 설정된 응답 데이터
     * @author 이수정
     * @since 1.0
     */
    private static Mono<Void> forbiddenWriteWith(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @Getter
    @Setter
    public static class Config {

        private List<String> roles;
        private List<String> excludes;
    }
}
