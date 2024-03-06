package kr.aling.gateway.common.dto.response;

import lombok.Getter;

/**
 * 그룹 회원 권한 이름을 가져오기 위한 dto.
 *
 * @author : 정유진
 * @since : 1.0
 **/
@Getter
public class GetBandUserAuthResponseDto {
    private String bandUserRoleName;
}
