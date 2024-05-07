package jiheon.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KakaoConfig {

    // RestTemplate에 대한 의존성 관리를 스프링 컨테이너가 관리하도록 등록
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
