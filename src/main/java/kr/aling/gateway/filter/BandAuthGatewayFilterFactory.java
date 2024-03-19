package kr.aling.gateway.filter;

import java.util.Objects;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.feignclient.UserServerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 그룹 회원 권한 필터.
 *
 * @author : 정유진
 * @since : 1.0
 **/
@Component
@Slf4j
public class BandAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<BandAuthGatewayFilterFactory.Config> {

    private final UserServerClient userServerClient;

    public BandAuthGatewayFilterFactory(@Lazy UserServerClient userServerClient) {
        super(Config.class);
        this.userServerClient = userServerClient;
    }

    @Override
    public GatewayFilter apply(BandAuthGatewayFilterFactory.Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HeaderNames.BAND_NO.getName())
                    || !request.getHeaders().containsKey(HeaderNames.USER_NO.getName())) {
                return chain.filter(exchange);
            }

            Long userNo = Long.parseLong(Objects.requireNonNull(request.getHeaders().get(HeaderNames.USER_NO.getName()))
                    .get(0));
            Long bandNo = Long.parseLong(Objects.requireNonNull(request.getHeaders().get(HeaderNames.BAND_NO.getName()))
                    .get(0));

            return Mono.fromCallable(() -> userServerClient.getUserById(bandNo, userNo))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(getBandUserAuthResponseDto -> {
                        String bandUserRoleName = getBandUserAuthResponseDto.getBandUserRoleName();
                        exchange.getRequest().mutate()
                                .header(HeaderNames.BAND_USER_ROLE.getName(), bandUserRoleName)
                                .build();
                        return chain.filter(exchange);
                    })
                    .doOnError(error ->
                            log.error(error.getMessage()));
        });
    }

    /**
     * filter 에 사용될 Config.
     */
    public static class Config {
    }
}
