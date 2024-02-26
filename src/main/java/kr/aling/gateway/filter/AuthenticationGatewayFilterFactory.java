package kr.aling.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import kr.aling.gateway.common.jwt.JwtUtils;
import kr.aling.gateway.common.properties.AccessProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom GatewayFilterFactory. JWT Access Token을 바탕으로 인증된 요청인지 확인하는 필터입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Component
public class AuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private static final String AUTHORIZATION = "Authorization";

    private final AccessProperties accessProperties;
    private final JwtUtils jwtUtils;

    public AuthenticationGatewayFilterFactory(AccessProperties accessProperties, JwtUtils jwtUtils) {
        super(Config.class);
        this.accessProperties = accessProperties;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 토큰의 유효 여부를 확인해 토큰이 존재하지 않거나, 유효하지 않을 시 401 응답을 보냅니다. Access Token이 만료되었다면 토큰의 만료를 알립니다.
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

            String subPath = request.getPath().subPath(6).value();
            if (config.getExcludes() != null && config.getExcludes().stream().anyMatch(subPath::matches)) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(AUTHORIZATION)) {
                return unauthorizedWriteWith(exchange, "Token not exists.");
            }

            String accessToken = Objects.requireNonNull(
                    request.getHeaders().get(AUTHORIZATION)).get(0).substring(7);

            try {
                if (jwtUtils.isExpiredToken(accessProperties.getSecret(), accessToken)) {
                    return unauthorizedWriteWith(exchange, "Token expired.");
                }
                return chain.filter(exchange);
            } catch (Exception e) {
                return unauthorizedWriteWith(exchange, "Token invalid : " + e.getMessage());
            }
        });
    }

    /**
     * 401 응답을 설정하고, 응답 메세지를 담아 보냅니다.
     *
     * @param exchange ServerHttpResponse를 얻기 위한 ServerWebExchange 객체
     * @param message  응답 메세지
     * @return 401 메세지 설정된 응답 데이터
     * @author 이수정
     * @since 1.0
     */
    private static Mono<Void> unauthorizedWriteWith(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }


    @Getter
    @Setter
    public static class Config {

        private List<String> excludes;
    }
}
