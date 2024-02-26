package kr.aling.gateway.config;

import kr.aling.gateway.common.filter.BandAuthFilter;
import kr.aling.gateway.common.properties.AlingUrlProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
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
    private final BandAuthFilter bandAuthFilter;

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
                        .filters(f -> f.rewritePath("/user/(?<path>.*)",
                                        "/${path}")                       // 임시 유저 번호 헤더 추가
//                                .addRequestHeader("X-TEMP-USER-NO", "1")                                                  // 관리자 유저
                                .addRequestHeader("X-TEMP-USER-NO",
                                        "2")                            // 일반 유저 / 그룹 CREATOR 유저
                                .filter(bandAuthGatewayFilter(bandAuthFilter)))
                        .uri(alingUrlProperties.getUserUrl()))
                .route("aling-post", p -> p.path("/post/**")
                        .filters(f -> f.rewritePath("/post/(?<path>.*)", "/${path}"))
                        .uri(alingUrlProperties.getPostUrl()))
                .build();
    }

    private GatewayFilter bandAuthGatewayFilter(BandAuthFilter bandAuthFilter) {
        return bandAuthFilter.apply(new BandAuthFilter.Config(alingUrlProperties));
    }
}
