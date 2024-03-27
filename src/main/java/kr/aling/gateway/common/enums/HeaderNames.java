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
    ACCESS_TOKEN("Authorization"),
    REFRESH_TOKEN("X-Refresh-Token"),
    USER_NO("X-User-No"),
    USER_ROLE("X-User-Role"),
    BAND_NO("X-BAND-NO"),
    BAND_USER_ROLE("X-BAND-USER-ROLE");

    private final String name;

    HeaderNames(String name) {
        this.name = name;
    }
}
