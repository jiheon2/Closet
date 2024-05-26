package jiheon.styleservice.config;

import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChromaConfig {
    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(new OpenAiApi("sk-proj-eraW7VPjwFB5FHiuX94BT3BlbkFJUsN32BUAn5KWyJq8mo4W"));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ChromaApi chromaApi(RestTemplate restTemplate) {
        String chromaUrl = "http://localhost:8000";
        ChromaApi chromaApi = new ChromaApi(chromaUrl, restTemplate);
        return chromaApi;
    }

    @Bean
    public VectorStore chromaVectorSore(EmbeddingModel embeddingModel, ChromaApi chromaApi) {
        return new ChromaVectorStore(embeddingModel, chromaApi, "TestCollection");
    }
}
