package kr.aling.gateway.config;

import kr.aling.gateway.feignclient.FeignClientBase;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Eureka 와 관련된 설정이 모여있는 Java Config 입니다. 본래 PostApplication.class 상단에서 @EnableFeignClients 적용시 별다른 설정이 필요없지만 이와 같이 분리할
 * 경우 FeignClient 들의 위치를 명시해주어야 읽을 수 있습니다.
 *
 * @author : 이성준
 * @since : 1.0
 */
@Configuration
@EnableEurekaClient
@EnableFeignClients(basePackageClasses = FeignClientBase.class)
public class EurekaConfig {

}
