package kr.aling.gateway.common.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT AccessToken, RefreshToken 생성 요청 파라미터를 담는 Dto.
 *
 * @author 이수정
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class IssueTokenRequestDto {

    private Long userNo;
    private List<String> roles;
}
