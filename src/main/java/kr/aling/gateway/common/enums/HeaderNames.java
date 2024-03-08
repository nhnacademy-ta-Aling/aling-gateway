package kr.aling.gateway.common.enums;

import lombok.Getter;

/**
 * 헤더 이름을 모아놓은 Enum class.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Getter
public enum HeaderNames {
    USER_NO("X-User-No"),
    ACCESS_TOKEN("Authorization"),
    BAND_NO("X-BAND-NO"),
    BAND_USER_ROLE("X-BAND-USER-ROLE"),
    REFRESH_TOKEN("X-Refresh-Token"),
    USER_ROLE("X-User-Role");

    private final String name;

    HeaderNames(String name) {
        this.name = name;
    }
}
