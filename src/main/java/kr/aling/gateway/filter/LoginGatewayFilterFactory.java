package kr.aling.gateway.filter;

import feign.Response;
import java.util.Objects;
import kr.aling.gateway.common.dto.request.IssueTokenRequestDto;
import kr.aling.gateway.common.enums.CookieNames;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.common.properties.AccessProperties;
import kr.aling.gateway.common.properties.RefreshProperties;
import kr.aling.gateway.common.utils.CookieUtils;
import kr.aling.gateway.feignclient.AuthServerClient;
import kr.aling.gateway.filter.LoginGatewayFilterFactory.Config;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Custom GatewayFilterFactory. 로그인 후 JWT 토큰을 쿠키로 설정합니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Component
public class LoginGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final AuthServerClient authServerClient;

    private final AccessProperties accessProperties;
    private final RefreshProperties refreshProperties;

    public LoginGatewayFilterFactory(AuthServerClient authServerClient,
            AccessProperties accessProperties, RefreshProperties refreshProperties) {
        super(Config.class);
        this.authServerClient = authServerClient;
        this.accessProperties = accessProperties;
        this.refreshProperties = refreshProperties;
    }

    /**
     * 로그인 요청을 처리합니다.
     *
     * @param config 설정 정보
     * @return 다음 필터로 요청 전달
     * @author 이수정
     * @since 1.0
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) ->
                chain.filter(exchange).then(Mono.fromCallable(() -> {
                    HttpHeaders headers = exchange.getResponse().getHeaders();

                    Response response = authServerClient.issue(new IssueTokenRequestDto(
                            Long.parseLong(Objects.requireNonNull(headers.get(HeaderNames.USER_NO.getName())).get(0)),
                            headers.get(HeaderNames.USER_ROLE.getName())
                    ));

                    exchange.getResponse().addCookie(
                            CookieUtils.makeTokenCookie(
                                    CookieNames.ACCESS_TOKEN.getName(),
                                    response.headers().get(HeaderNames.ACCESS_TOKEN.getName()).toString(),
                                    accessProperties.getExpireTime().toMillis()
                            )
                    );

                    exchange.getResponse().addCookie(
                            CookieUtils.makeTokenCookie(
                                    CookieNames.REFRESH_TOKEN.getName(),
                                    response.headers().get(HeaderNames.REFRESH_TOKEN.getName()).toString(),
                                    refreshProperties.getExpireTime().toMillis()
                            )
                    );

                    return chain.filter(exchange);
                })).then()
        );
    }

    public static class Config {

    }
}
