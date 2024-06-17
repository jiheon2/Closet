package jiheon.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RoutConfig {
    /**
     * Gateway로 접근되는 모든 요청에 대해 URL 요청 분리하기
     */
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes().
                // 연결될 서버 주소
                route(r -> r.path("/community/**").uri("lb://CLOSET-COMMUNITY:16000")).
                route(r -> r.path("/user/**").uri("lb://CLOSET-USER:13000")).
                route(r -> r.path("/security/**").uri("lb://CLOSET-USER:13000")).
                route(r -> r.path("/style/**").uri("lb://CLOSET-STYLE:18000")).
                route(r -> r.path("/closet/**").uri("lb://CLOSET-CLOSET:14000")).
                build();
    }
}
