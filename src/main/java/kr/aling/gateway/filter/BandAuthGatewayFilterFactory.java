package kr.aling.gateway.filter;

import java.util.Objects;
import kr.aling.gateway.common.dto.GetBandUserAuthResponseDto;
import kr.aling.gateway.common.properties.AlingUrlProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

    private final AlingUrlProperties alingUrlProperties;

    public BandAuthGatewayFilterFactory(AlingUrlProperties alingUrlProperties) {
        super(Config.class);
        this.alingUrlProperties = alingUrlProperties;
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

            GetBandUserAuthResponseDto dto = WebClient.create(
                            alingUrlProperties.getUserUrl() + "/api/v1/bands/" + bandNo + "/users/" + userNo + "/role")
                    .get().retrieve().bodyToMono(GetBandUserAuthResponseDto.class).block();

            exchange.getRequest().mutate().header(X_BAND_USER_ROLE, dto.getBandUserRoleName()).build();

            return chain.filter(exchange);
        });
    }

    /**
     * filter 에 사용될 Config.
     */
    public static class Config {
    }
}
