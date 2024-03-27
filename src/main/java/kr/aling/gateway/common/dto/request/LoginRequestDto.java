package kr.aling.gateway.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인시 파라미터를 담는 dto.
 *
 * @author 여운석
 * @since 1.0
 **/
@Getter
@AllArgsConstructor
public class LoginRequestDto {

    private String email;
    private String password;
}
