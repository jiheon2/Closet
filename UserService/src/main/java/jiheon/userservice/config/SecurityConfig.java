package jiheon.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.token.access.name}")
    private String accessTokenName;

    @Value("${jwt.token.refresh.name}")
    private String refreshTokenName;

    private final CorsConfigurationSource corsConfigurationSource; // CORS 매핑처리

    @Bean
    public PasswordEncoder passwordEncoder() {

        log.info(this.getClass().getName() + ".PasswordEncoder Start!");

        return new BCryptPasswordEncoder();

    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info(this.getClass().getName() + ".filterChain Start!");


        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(config -> config.configurationSource(corsConfigurationSource))
//                .authorizeHttpRequests(authz -> authz
//                .requestMatchers("/user/v1/**").hasAnyAuthority(role)
//                .requestMatchers("/style/v1/**").hasAnyAuthority(role)
//                .requestMatchers("/closet/v1/**").hasAnyAuthority(role)
//                .requestMatchers("/community/v1/**").hasAnyAuthority(role)
//                .requestMatchers("/security/v1/**").permitAll()
//                .requestMatchers("/resources/**").permitAll()
//                .anyRequest().permitAll()
                .formLogin(login -> login
                        .loginPage("/security/login.html") // 로그인 페이지 html
                        .loginProcessingUrl("/security/v1/loginProc") // 로그인 수행
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .successForwardUrl("/security/v1/loginSuccess") // 로그인 성공 URL
                        .failureForwardUrl("/security/v1/loginFail") // 로그인 실패 URL

                ).logout(logout -> logout
                        .logoutUrl("/security/v1/logout") // 로그아웃 요청 URL
                        .clearAuthentication(true)
                        .deleteCookies(accessTokenName, refreshTokenName)
                        .logoutSuccessUrl("http://front:12000/security/login.html") // 로그아웃 처리 URL

                ).sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
