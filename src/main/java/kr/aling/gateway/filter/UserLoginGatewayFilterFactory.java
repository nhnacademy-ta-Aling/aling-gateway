package kr.aling.gateway.filter;

import static kr.aling.gateway.common.jwt.AuthUtils.makeTokenCookie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import kr.aling.gateway.common.dto.request.LoginRequestDto;
import kr.aling.gateway.common.dto.response.LoginResponseDto;
import kr.aling.gateway.common.enums.CookieNames;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.common.exception.AuthenticationException;
import kr.aling.gateway.common.jwt.AuthUtils;
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
    private final UserServerClient userServerClient;
    private final ObjectMapper objectMapper;
    private static final String ID_HEADER = "X-Login-Id";
    private static final String PWD_HEADER = "X-Login-Pwd";
    private static final String BEARER = "Bearer ";


    public UserLoginGatewayFilterFactory(@Lazy UserServerClient userServerClient, ObjectMapper objectMapper) {
        super(Config.class);
        this.userServerClient = userServerClient;
        this.objectMapper = objectMapper;
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
            String id = Objects.requireNonNull(request.getHeaders().get(ID_HEADER)).get(0);
            String pwd = Objects.requireNonNull(request.getHeaders().get(PWD_HEADER)).get(0);

            LoginResponseDto responseDto = userServerClient.login(new LoginRequestDto(id, pwd));

            ModifyRequestBodyGatewayFilterFactory.Config modifyFilterConfig
                    = new ModifyRequestBodyGatewayFilterFactory.Config();
            modifyFilterConfig.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            modifyFilterConfig.setRewriteFunction(String.class, String.class, ((modifyExchange, originBody) -> {
                String modifiedBody;

                try {
                    modifiedBody = objectMapper.writeValueAsString(responseDto);
                } catch (JsonProcessingException e) {
                    throw new AuthenticationException(HttpStatus.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다.");
                }

                return Mono.just(modifiedBody);
            }));

            return new ModifyRequestBodyGatewayFilterFactory().apply(modifyFilterConfig).filter(exchange, chain)
                    .then(Mono.fromRunnable(() -> {
                        HttpHeaders headers = exchange.getResponse().getHeaders();

                        ResponseCookie refreshCookie = makeTokenCookie(
                                CookieNames.REFRESH_TOKEN,
                                headers.getFirst(HeaderNames.REFRESH_TOKEN.getName()),
                                AuthUtils.REFRESH_TOKEN_EXPIRE);

                        ResponseCookie accessCookie = makeTokenCookie(
                                CookieNames.ACCESS_TOKEN,
                                Objects.requireNonNull(headers.getFirst(HeaderNames.ACCESS_TOKEN.getName()))
                                        .substring(BEARER.length()),
                                AuthUtils.ACCESS_TOKEN_EXPIRE
                        );

                        exchange.getResponse().addCookie(refreshCookie);
                        exchange.getResponse().addCookie(accessCookie);
                    }));
        });
    }

    /**
     * filter 에 사용될 Config.
     */
    public static class Config {

    }
}
