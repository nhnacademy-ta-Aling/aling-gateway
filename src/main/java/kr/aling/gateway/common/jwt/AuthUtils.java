package kr.aling.gateway.common.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import kr.aling.gateway.common.enums.CookieNames;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.common.properties.AccessProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * Some description here.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Component
@RequiredArgsConstructor
public class AuthUtils {
    public static final long ACCESS_TOKEN_EXPIRE = 3600;
    public static final int REFRESH_TOKEN_EXPIRE = 1209600;
    private final JwtUtils jwtUtils;
    private final AccessProperties accessProperties;
    private final ObjectMapper objectMapper;

    public static ResponseCookie makeTokenCookie(CookieNames cookieNames, String token, long maxAge) {
        return ResponseCookie
                .from(cookieNames.getName(), Objects.requireNonNull(
                        token))
                .httpOnly(true)
                .secure(true)
                .maxAge(maxAge)
                .path("/")
                .build();
    }

    public void addHeaderFromAccessToken(ServerHttpRequest request, String accessToken)
            throws JsonProcessingException {
        request.mutate()
                .header(HeaderNames.USER_NO.getName(),
                        jwtUtils.parseToken(accessProperties.getSecret(), accessToken).getSubject());
        List<String> roleList = (List<String>) jwtUtils.parseToken(
                accessProperties.getSecret(), accessToken).get("roles");
        request.mutate().header(HeaderNames.USER_ROLE.getName(),
                objectMapper.writeValueAsString(roleList));
    }
}
