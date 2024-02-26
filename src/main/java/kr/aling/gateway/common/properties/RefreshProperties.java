package kr.aling.gateway.common.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Refresh Token 관련 설정 Properties.
 *
 * @author 이수정
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "aling.security.refresh")
public class RefreshProperties {

    private String secret;
    private String headerName;
    private Duration expireTime;
}
