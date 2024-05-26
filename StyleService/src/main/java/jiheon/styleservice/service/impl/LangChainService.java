package jiheon.styleservice.service.impl;

import jiheon.styleservice.service.ILangChainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LangChainService implements ILangChainService {

    private final VectorStore vectorStore;

    public LangChainService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void saveVector(String folderPath) {

        List<Document> documents = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath), "*.json")) {

            for (Path path : stream) {
                String content = new String(Files.readAllBytes(path));
                documents.add(new Document(content));
            }

            vectorStore.add(documents);
            log.info("documents : " + documents.size());

        } catch (IOException e) {
            log.info(e.toString());
        }
    }

    @Override
    public List<Document> answer(String question) throws Exception {

        List<Document> answer = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(3));

        log.info("answer : " + answer);

        return answer;
    }
}
