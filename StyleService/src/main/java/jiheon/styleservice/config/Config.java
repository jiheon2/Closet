package jiheon.styleservice.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;



@Configuration
public class Config {
    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(new OpenAiApi("sk-proj-OZcdjXdSJ45jKfkViXOzT3BlbkFJq6XWUTIp7UB97E7XjnWa"));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



}
