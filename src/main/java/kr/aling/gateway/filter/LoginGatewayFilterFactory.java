package kr.aling.gateway.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Custom GatewayFilterFactory. 로그인 후 JWT 토큰을 쿠키로 설정합니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Slf4j
@Component
public class LoginGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final AuthServerClient authServerClient;

    private final AccessProperties accessProperties;
    private final RefreshProperties refreshProperties;

    private final ObjectMapper objectMapper;

    public LoginGatewayFilterFactory(@Lazy AuthServerClient authServerClient,
            AccessProperties accessProperties, RefreshProperties refreshProperties, ObjectMapper objectMapper) {
        super(Config.class);
        this.authServerClient = authServerClient;
        this.accessProperties = accessProperties;
        this.refreshProperties = refreshProperties;
        this.objectMapper = objectMapper;
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
        return ((exchange, chain) -> {

            if (!exchange.getRequest().getPath().value().contains("login")) {
                return chain.filter(exchange);
            }

            return chain.filter(exchange).then(Mono.fromCallable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();

                Response response = authServerClient.issue(new IssueTokenRequestDto(
                        Long.parseLong(Objects.requireNonNull(headers.getFirst(HeaderNames.USER_NO.getName()))),
                        objectMapper.readValue(headers.getFirst(HeaderNames.USER_ROLE.getName()),
                                new TypeReference<>() {
                                })
                ));

                exchange.getResponse().addCookie(
                        CookieUtils.makeTokenCookie(
                                CookieNames.ACCESS_TOKEN.getName(),
                                (String) response.headers().get(HeaderNames.ACCESS_TOKEN.getName()).toArray()[0],
                                accessProperties.getExpireTime().toMillis()
                        )
                );

                exchange.getResponse().addCookie(
                        CookieUtils.makeTokenCookie(
                                CookieNames.REFRESH_TOKEN.getName(),
                                (String) response.headers().get(HeaderNames.REFRESH_TOKEN.getName()).toArray()[0],
                                refreshProperties.getExpireTime().toMillis()
                        )
                );

                return chain.filter(exchange);
            }).subscribeOn(Schedulers.boundedElastic())).onErrorReturn(Mono.empty()).then();
        });
    }

    public static class Config {

    }
}
