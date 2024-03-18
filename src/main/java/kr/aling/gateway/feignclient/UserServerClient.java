package kr.aling.gateway.feignclient;

import javax.ws.rs.core.MediaType;
import kr.aling.gateway.common.dto.response.GetBandUserAuthResponseDto;
import kr.aling.gateway.common.dto.request.LoginRequestDto;
import kr.aling.gateway.common.dto.response.GetBandUserAuthResponseDto;
import kr.aling.gateway.common.dto.response.LoginResponseDto;
import kr.aling.gateway.config.OpenFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * 로그인합니다.
     *
     * @param loginRequestDto 로그인시 필요한 정보
     * @return 로그인한 유저의 정보
     */
    @PostMapping(path = "/api/v1/users/login", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto);

}
