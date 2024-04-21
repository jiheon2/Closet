package jiheon.closetservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
@Configuration
public class SwaggerConfig {
    private Info apiInfo() {
        return new Info()
                .title("ClosetService")
                .description("ClosetService Description")
                .contact(new Contact().name("Prof. Jiheon2")
                        .email("000224kjh@gmail.com"))
                .license(new License()
                        .name("API 명세서"))
                .version("1.0.0");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().components(new Components()).info(apiInfo());
    }
}
