package jiheon.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    final String role = "ROLE_USER";

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info(this.getClass().getName() + ".PasswordEncoder Start!");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info(this.getClass().getName() + ".filterChain Start!");

        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/user/**").hasAnyAuthority(role)
                .requestMatchers("/style/**").hasAnyAuthority(role)
                .requestMatchers("/closet/**").hasAnyAuthority(role)
                .requestMatchers("/community/**").hasAnyAuthority(role)
                .requestMatchers("/security/**").permitAll()
                .anyRequest().permitAll()
        ).formLogin(login -> login
                .loginPage("/security/login")
                .loginProcessingUrl("/security/loginProc")
                .usernameParameter("userId")
                .passwordParameter("password")
                .successForwardUrl("/security/loginSuccess")
                .failureForwardUrl("/security/loginFail")
        );

        return http.build();
    }
}
