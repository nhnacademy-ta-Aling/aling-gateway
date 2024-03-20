package kr.aling.gateway.common.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringTokenizer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 쿠키를 생성하는 Util class.
 *
 * @author 이수정
 * @since 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

    /**
     * 토큰에 대한 응답 쿠키를 생성합니다.
     *
     * @param cookieName 쿠키 이름
     * @param token      쿠키에 담을 토큰
     * @param maxAge     쿠키 만료 시간
     * @return 생성된 응답 쿠키
     */
    public static ResponseCookie makeTokenCookie(String cookieName, String token, long maxAge) {
        return ResponseCookie.from(cookieName, Objects.requireNonNull(URLEncoder.encode(token, StandardCharsets.UTF_8)))
                .httpOnly(true).secure(true).maxAge(maxAge).path("/").build();
    }

    /**
     * cookie header로 받은 쿠키 문자열을 파싱하여 반환합니다.
     *
     * @param header 파싱할 쿠키 헤더 문자열
     * @return 파싱된 쿠키 맵
     */
    public static MultiValueMap<String, HttpCookie> parseHeaderToCookies(String header) {
        MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();

        StringTokenizer st = new StringTokenizer(header, ";");
        while (st.hasMoreTokens()) {
            String[] splited = URLDecoder.decode(st.nextToken().trim(), StandardCharsets.UTF_8).split("=");
            cookies.add(splited[0], new HttpCookie(splited[0], splited[1]));
        }

        return cookies;
    }
}
