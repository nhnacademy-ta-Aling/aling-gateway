package kr.aling.gateway.filter;

import kr.aling.gateway.common.enums.CookieNames;
import kr.aling.gateway.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 로그아웃시 쿠키를 지우기 위한 filter
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Slf4j
@Component
public class LogoutGatewayFilterFactory extends AbstractGatewayFilterFactory<LogoutGatewayFilterFactory.Config> {

    private static final String LOGOUT_URL = "/auth/api/v1/jwt/logout";
    private static final long EXPIRE_ZERO = 0L;

    public LogoutGatewayFilterFactory() {
        super(LogoutGatewayFilterFactory.Config.class);
    }

    @Override
    public GatewayFilter apply(LogoutGatewayFilterFactory.Config config) {
        return ((exchange, chain) -> {
            if (!exchange.getRequest().getURI().getPath().equals(LOGOUT_URL)) {
                return chain.filter(exchange);
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ResponseCookie accessCookie = CookieUtils.makeTokenCookie(
                        CookieNames.ACCESS_TOKEN.getName(),
                        "",
                        EXPIRE_ZERO
                );

                ResponseCookie refreshCookie = CookieUtils.makeTokenCookie(
                        CookieNames.REFRESH_TOKEN.getName(),
                        "",
                        EXPIRE_ZERO
                );

                exchange.getResponse().addCookie(accessCookie);
                exchange.getResponse().addCookie(refreshCookie);
            }));
        });
    }

    public static class Config {

    }
}
