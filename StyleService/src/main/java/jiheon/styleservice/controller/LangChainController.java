package jiheon.styleservice.controller;

import jiheon.styleservice.service.impl.LangChainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LangChainController {

    private final LangChainService langChainService;

    @GetMapping(value = "/saveVector")
    public void saveVector() throws Exception {

        String folderPath = "C:\\Closet\\StyleService\\src\\main\\resources\\json\\힙합";
        String question = "힙합 스타일의 상의를 추천해줘";

        langChainService.saveVector(folderPath);

        langChainService.answer(question);
    }
}
