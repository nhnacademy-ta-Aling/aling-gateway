package kr.aling.gateway.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Aling 관련 서버 url properties.
 *
 * @author : 정유진
 * @since : 1.0
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = "aling")
public class AlingUrlProperties {
    private String authUrl;
    private String userUrl;
    private String postUrl;
}
