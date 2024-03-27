package kr.aling.gateway.feignclient;

import feign.Response;
import kr.aling.gateway.common.dto.request.IssueTokenRequestDto;
import kr.aling.gateway.config.OpenFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Auth 서버와 통신하기 위한 feign client.
 *
 * @author 여운석, 이수정
 * @since 1.0
 */
@FeignClient(name = "aling-auth", configuration = OpenFeignConfig.class)
public interface AuthServerClient {

    /**
     * 유저 번호와 권한을 받아 AccessToken, RefreshToken을 발급받습니다.
     *
     * @param requestDto 유저 번호와 권한
     * @return 발급받은 토큰 헤더를 담은 Response 객체
     */
    @PostMapping(value = "/api/v1/jwt/issue", consumes = "application/json", produces = "application/json")
    Response issue(@RequestBody IssueTokenRequestDto requestDto);


    /**
     * refresh token을 가지고 access token을 재발급 받습니다.
     *
     * @param header refresh-token
     * @return response 객체
     */
    @GetMapping("/api/v1/jwt/reissue")
    Response reissue(@RequestHeader("X-Refresh-Token") String header);
}
