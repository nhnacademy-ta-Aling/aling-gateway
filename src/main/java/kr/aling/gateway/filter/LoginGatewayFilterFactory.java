package kr.aling.gateway.filter;

import kr.aling.gateway.filter.LoginGatewayFilterFactory.Config;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

/**
 * Custom GatewayFilterFactory. 로그인 후 JWT 토큰을 쿠키로 설정합니다.
 *
 * @author 이수정
 * @since 1.0
 */
public class LoginGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // Pre Filter Pass

            // Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();


            }));
        });
    }

    public static class Config {

    }
}
