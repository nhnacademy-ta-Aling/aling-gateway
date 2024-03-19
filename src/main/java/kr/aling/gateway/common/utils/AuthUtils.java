package kr.aling.gateway.common.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.aling.gateway.common.enums.HeaderNames;
import kr.aling.gateway.common.properties.AccessProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * 인증 / 인가에 사용하는 util class.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@RequiredArgsConstructor
@Component
public class AuthUtils {

    private final ObjectMapper objectMapper;
    private final AccessProperties accessProperties;

    /**
     * 헤더에 유저 번호, 권한을 추가합니다.
     *
     * @param request     request 객체
     * @param accessToken token
     * @throws JsonProcessingException objectMapper 사용시 발생하는 예외
     */
    public void addHeaderFromAccessToken(ServerHttpRequest request, String accessToken) throws JsonProcessingException {
        request.mutate().header(HeaderNames.USER_NO.getName(),
                JwtUtils.parseToken(accessProperties.getSecret(), accessToken).getSubject());
        List<String> roleList =
                (List<String>) JwtUtils.parseToken(accessProperties.getSecret(), accessToken).get("roles");
        request.mutate().header(HeaderNames.USER_ROLE.getName(), objectMapper.writeValueAsString(roleList));
    }
}
