package kr.aling.gateway.feignclient;

import kr.aling.gateway.common.dto.GetBandUserAuthResponseDto;
import kr.aling.gateway.config.OpenFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * User 서버와 통신 하기 위한 feign client.
 *
 * @author 정유진
 * @since 1.0
 **/
@FeignClient(name = "aling-user", configuration = OpenFeignConfig.class)
public interface UserServerClient {
    @GetMapping("/api/v1/bands/{bandNo}/users/{userNo}/role")
    GetBandUserAuthResponseDto getUserById(@PathVariable("bandNo") Long bandNo, @PathVariable("userNo") Long userNo);
}
