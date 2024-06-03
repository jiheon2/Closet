package jiheon.styleservice.service;

import jiheon.styleservice.dto.BlobDTO;
import jiheon.styleservice.dto.StyleDTO;
import org.springframework.ai.document.Document;

import java.util.List;

public interface IStyleService {

    void saveVector(String folderPath);

    List<Document> answer(String question) throws Exception;

    List<Document> answer(StyleDTO pDTO) throws Exception;

    List<String> styleList() throws Exception;

}
