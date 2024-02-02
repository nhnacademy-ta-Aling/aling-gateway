package kr.aling.gateway.config;

import kr.aling.gateway.common.properties.AlingUrlProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gateway 라우팅을 위한 RouteConfig.
 *
 * @author : 정유진
 * @since : 1.0
 **/
@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    private final AlingUrlProperties alingUrlProperties;

    /**
     * 라우팅을 위한 RouteLocator Bean.
     *
     * @param routeLocatorBuilder routeLocator 빌더
     * @return routeLocator
     */
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route("aling-auth", p -> p.path("/auth/**")
                        .filters(f -> f.rewritePath("/auth/(?<path>.*)", "/${path}"))
                        .uri(alingUrlProperties.getAuthUrl()))
                .route("aling-user", p -> p.path("/user/**")
                        .filters(f -> f.rewritePath("/user/(?<path>.*)", "/${path}"))
                        .uri(alingUrlProperties.getUserUrl()))
                .route("aling-post", p -> p.path("/post/**")
                        .filters(f -> f.rewritePath("/post/(?<path>.*)", "/${path}"))
                        .uri(alingUrlProperties.getPostUrl()))
                .build();
    }
}
