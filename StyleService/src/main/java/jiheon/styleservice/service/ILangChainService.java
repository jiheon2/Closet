package jiheon.styleservice.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface ILangChainService {

    void saveVector(String folderPath);

    List<Document> answer(String question) throws Exception;

}
