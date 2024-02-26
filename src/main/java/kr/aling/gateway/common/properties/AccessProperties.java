package kr.aling.gateway.common.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Access Token 관련 설정 Properties.
 *
 * @author 이수정
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "aling.security.access")
public class AccessProperties {

    private String secret;
    private Duration expireTime;
}
