package kr.aling.gateway.feignclient;

import feign.Response;
import kr.aling.gateway.config.OpenFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Auth 서버와 통신하기 위한 feign client.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@FeignClient(name = "aling-auth", configuration = OpenFeignConfig.class)
public interface AuthServerClient {

    /**
     * refresh token을 가지고 access token을 재발급 받습니다.
     *
     * @param header refresh-token
     * @return response 객체
     */
    @GetMapping("/api/v1/jwt/reissue")
    Response reissue(@RequestHeader("X-Refresh-Token") String header);
}
