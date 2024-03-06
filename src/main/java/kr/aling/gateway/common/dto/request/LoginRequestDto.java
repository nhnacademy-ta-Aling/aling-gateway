package kr.aling.gateway.common.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인시 파라미터를 담는 dto.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequestDto {
    @NotBlank
    @Size(min = 3, max = 100)
    @Email
    private String email;
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
}
