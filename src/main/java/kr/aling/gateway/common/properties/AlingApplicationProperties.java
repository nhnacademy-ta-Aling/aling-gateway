package kr.aling.gateway.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Aling 관련 서버 어플리케이션 네임 properties.
 *
 * @author : 이성준
 * @since : 1.0
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = "aling.applications")
public class AlingApplicationProperties {
    private String auth;
    private String user;
    private String post;
}
