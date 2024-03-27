package kr.aling.gateway.common.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.ToString;

/**
 * 로그인 시 되돌려줄 정보가 담긴 dto.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Getter
@ToString
public class LoginResponseDto {

    private Long userNo;
    private List<String> roles;
}
