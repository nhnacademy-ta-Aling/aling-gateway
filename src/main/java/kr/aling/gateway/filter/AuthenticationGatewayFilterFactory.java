package kr.aling.gateway.filter;

import java.util.Objects;
import kr.aling.gateway.common.properties.SecurityProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final SecurityProperties securityProperties;

    public AuthenticationGatewayFilterFactory(SecurityProperties securityProperties) {
        super(Config.class);
        this.securityProperties = securityProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if (!request.getHeaders().containsKey(securityProperties.getAtkHeaderName())) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            String accessToken = Objects.requireNonNull(
                    request.getHeaders().get(securityProperties.getAtkHeaderName())).getFirst();
            
        }));
    }

    public static class Config {

    }
}
