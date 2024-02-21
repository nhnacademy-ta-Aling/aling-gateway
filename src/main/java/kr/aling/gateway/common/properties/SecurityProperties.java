package kr.aling.gateway.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security 관련 설정 Properties.
 *
 * @author 이수정
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "aling.security")
public class SecurityProperties {

    private String atkHeaderName;
    private String rtkHeaderName;
}
