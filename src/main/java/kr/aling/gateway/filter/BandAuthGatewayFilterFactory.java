package kr.aling.gateway.filter;

import java.util.Objects;
import kr.aling.gateway.feignclient.UserServerClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * 그룹 회원 권한 필터.
 *
 * @author : 정유진
 * @since : 1.0
 **/
@Component
public class BandAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<BandAuthGatewayFilterFactory.Config> {
    private static final String X_BAND_NO = "X-BAND-NO";
    private static final String X_TEMP_USER_NO = "X-TEMP-USER-NO";
    private static final String X_BAND_USER_ROLE = "X-BAND-USER-ROLE";

    private final UserServerClient userServerClient;

    public BandAuthGatewayFilterFactory(@Lazy UserServerClient userServerClient) {
        super(Config.class);
        this.userServerClient = userServerClient;
    }

    @Override
    public GatewayFilter apply(BandAuthGatewayFilterFactory.Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(X_BAND_NO) || !request.getHeaders().containsKey(X_TEMP_USER_NO)) {
                return chain.filter(exchange);
            }

            Long userNo = Long.parseLong(Objects.requireNonNull(request.getHeaders().get(X_TEMP_USER_NO)).get(0));
            Long bandNo = Long.parseLong(Objects.requireNonNull(request.getHeaders().get(X_BAND_NO)).get(0));

            exchange.getRequest().mutate()
                    .header(X_BAND_USER_ROLE, userServerClient.getUserById(bandNo, userNo).getBandUserRoleName())
                    .build();

            return chain.filter(exchange);
        });
    }

    /**
     * filter 에 사용될 Config.
     */
    public static class Config {
    }
}
