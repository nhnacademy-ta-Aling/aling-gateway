package kr.aling.gateway.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 서버 통신을 위한 properties.
 *
 * @author : 정유진
 * @since  : 1.0
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = "aling.server")
public class AlingUrlProperties {
    private String userUrl;
}
