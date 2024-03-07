package kr.aling.gateway.common.enums;

import lombok.Getter;

/**
 * 쿠키 이름을 모아놓은 Enum class.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Getter
public enum CookieNames {
    ACCESS_TOKEN("jteu"),
    REFRESH_TOKEN("jtru");

    private final String name;

    CookieNames(String name) {
        this.name = name;
    }
}
