package kr.aling.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import kr.aling.gateway.common.dto.request.LoginRequestDto;
import kr.aling.gateway.common.dto.response.LoginResponseDto;
import kr.aling.gateway.common.enums.CookieNames;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.common.exception.AuthenticationException;
import kr.aling.gateway.common.properties.AccessProperties;
import kr.aling.gateway.common.properties.RefreshProperties;
import kr.aling.gateway.common.utils.CookieUtils;
import kr.aling.gateway.feignclient.UserServerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 로그인 요청을 처리하여 jwt를 반환하는 필터입니다.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Slf4j
@Component
public class UserLoginGatewayFilterFactory extends AbstractGatewayFilterFactory<UserLoginGatewayFilterFactory.Config> {

    private static final String BEARER = "Bearer ";
    private static final String ID_HEADER = "X-Login-Id";
    private static final String PWD_HEADER = "X-Login-Pwd";

    private final UserServerClient userServerClient;
    private final ObjectMapper objectMapper;
    private final AccessProperties accessProperties;
    private final RefreshProperties refreshProperties;

    public UserLoginGatewayFilterFactory(@Lazy UserServerClient userServerClient, ObjectMapper objectMapper,
            AccessProperties accessProperties, RefreshProperties refreshProperties) {
        super(Config.class);
        this.userServerClient = userServerClient;
        this.objectMapper = objectMapper;
        this.accessProperties = accessProperties;
        this.refreshProperties = refreshProperties;
    }

    /**
     * 로그인 요청을 처리합니다.
     *
     * @param config config
     * @return gateway filter
     */
    @Override
    public GatewayFilter apply(UserLoginGatewayFilterFactory.Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            Optional<String> id = Optional.ofNullable(request.getHeaders().getFirst(ID_HEADER));
            Optional<String> pwd = Optional.ofNullable(request.getHeaders().getFirst(PWD_HEADER));

            if (id.isEmpty() || pwd.isEmpty()) {
                return chain.filter(exchange);
            }

            ModifyRequestBodyGatewayFilterFactory.Config modifyFilterConfig
                    = new ModifyRequestBodyGatewayFilterFactory.Config();
            modifyFilterConfig.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            modifyFilterConfig.setRewriteFunction(String.class, String.class, ((modifyExchange, originBody) -> {
                String modifiedBody;

                LoginResponseDto responseDto = null;

                try {
                    responseDto = getLoginResponse(id.get(), pwd.get());
                    modifiedBody = objectMapper.writeValueAsString(responseDto);
                } catch (Exception e) {
                    throw new AuthenticationException(HttpStatus.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다.");
                }

                return Mono.just(modifiedBody).subscribeOn(Schedulers.boundedElastic());
            }));

            return new ModifyRequestBodyGatewayFilterFactory().apply(modifyFilterConfig).filter(exchange, chain)
                    .subscribeOn(Schedulers.boundedElastic())
                    .then(Mono.fromRunnable(() -> {
                        HttpHeaders headers = exchange.getResponse().getHeaders();

                        ResponseCookie accessCookie = CookieUtils.makeTokenCookie(
                                CookieNames.ACCESS_TOKEN.getName(),
                                Objects.requireNonNull(headers.getFirst(HeaderNames.ACCESS_TOKEN.getName()))
                                        .substring(BEARER.length()),
                                accessProperties.getExpireTime().toMillis()
                        );

                        ResponseCookie refreshCookie = CookieUtils.makeTokenCookie(
                                CookieNames.REFRESH_TOKEN.getName(),
                                headers.getFirst(HeaderNames.REFRESH_TOKEN.getName()),
                                refreshProperties.getExpireTime().toMillis()
                        );

                        exchange.getResponse().addCookie(accessCookie);
                        exchange.getResponse().addCookie(refreshCookie);
                    }));
        });
    }

    /**
     * filter 에 사용될 Config.
     */
    public static class Config {

    }

    private LoginResponseDto getLoginResponse(String id, String pwd) throws ExecutionException, InterruptedException {
        CompletableFuture<LoginResponseDto> future =
                CompletableFuture.supplyAsync(() -> userServerClient.login(new LoginRequestDto(id, pwd)));

        return future.get();
    }
}
