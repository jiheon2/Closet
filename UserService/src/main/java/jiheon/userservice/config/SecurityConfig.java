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


    final String role = "ROLE_USER";
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

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(config -> config.configurationSource(corsConfigurationSource));

        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/user/v1/**").hasAnyAuthority(role)
                .requestMatchers("/style/v1/**").hasAnyAuthority(role)
                .requestMatchers("/closet/v1/**").hasAnyAuthority(role)
                .requestMatchers("/community/v1/**").hasAnyAuthority(role)
                .requestMatchers("/security/v1/**").permitAll()
                .requestMatchers("/resources/**").permitAll()
                .anyRequest().permitAll()
        ).formLogin(login -> login
                .loginPage("/security/v1/login")
                .loginProcessingUrl("/security/v1/loginProc")
                .usernameParameter("userId")
                .passwordParameter("password")
                .successForwardUrl("/security/v1/loginSuccess")
                .failureForwardUrl("/security/v1/loginFail")
        ).logout(logout -> logout
                .logoutUrl("/security/v1/logout")
                .deleteCookies(accessTokenName, refreshTokenName)
                .logoutSuccessUrl("/security/v1/logoutSuccess")

        ).sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
