package jiheon.apigateway.config;

import jiheon.apigateway.filter.JwtAuthenticationFilter;
import jiheon.apigateway.handler.AccessDeniedHandler;
import jiheon.apigateway.handler.LoginServerAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
public class SecurityConfig {

    final String role = "ROLE_USER";

    private final AccessDeniedHandler accessDeniedHandler;

    private final LoginServerAuthenticationEntryPoint loginServerAuthenticationEntryPoint;

    // JWT 검증을 위한 필터
    // 초기 Spring Filter를 Spring에 제어가 불가능했지만, 현재 제어 가능함
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {

        log.info(this.getClass().getName() + ".filterChain 실행");

        http.csrf(ServerHttpSecurity.CsrfSpec::disable); // Post 방식 전송을 위해 csrf 막기
        http.cors(ServerHttpSecurity.CorsSpec::disable); // CORS 사용X
        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable); // 로그인 기능 사용X

        // 인증 에러 처리
        http.exceptionHandling(exceptionHandlingSpec ->
                exceptionHandlingSpec.accessDeniedHandler(accessDeniedHandler));

        // 인가 에러 처리
        http.exceptionHandling(exceptionHandlingSpec ->
                exceptionHandlingSpec.authenticationEntryPoint(loginServerAuthenticationEntryPoint));

        // stateless방식의 애플리케이션이 되도록 설정
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        http.authorizeExchange(authz -> authz
                .pathMatchers("/user/v1/**").hasAnyAuthority(role)
                .pathMatchers("/style/v1/**").hasAnyAuthority(role)
                .pathMatchers("/closet/v1/**").hasAnyAuthority(role)
                .pathMatchers("/community/v1/**").hasAnyAuthority(role)
                .pathMatchers("/resources/**").permitAll()
                .pathMatchers("/security/v1/**").permitAll()
                .anyExchange().permitAll()
        );

        // Spring Security 필터들이 실행되기 전에 JWT 필터 실행
        http.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC);

        return http.build();
    }
}
