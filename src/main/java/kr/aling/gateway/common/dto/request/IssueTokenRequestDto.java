package kr.aling.gateway.common.dto.request;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import kr.aling.gateway.common.dto.response.LoginResponseDto;

/**
 * 토큰을 받기위한 request dto.
 *
 * @author : 여운석
 * @since : 1.0
 **/
public class IssueTokenRequestDto {

    @NotNull
    @Positive
    private Long userNo;

    @NotEmpty
    private List<String> roles;

    public IssueTokenRequestDto(LoginResponseDto responseDto) {
        this.userNo = responseDto.getUserNo();
        this.roles = responseDto.getRoles();
    }
}
