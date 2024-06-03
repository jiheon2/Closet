package jiheon.styleservice.service.impl;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jiheon.styleservice.dto.BlobDTO;
import jiheon.styleservice.dto.StyleDTO;
import jiheon.styleservice.service.IStyleService;
import jiheon.styleservice.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StyleService implements IStyleService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String fileKey;

    private final VectorStore vectorStore;

    // 생성자 주입 방법
    public StyleService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void saveVector(String folderPath) {

        log.info("[Service] saveVector Start");

        // document list 생성
        List<Document> documents = new ArrayList<>();

        // json파일을 폴더에서 읽어와 String 형식으로 변환 후 document list에 저장하기
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath), "*.json")) {

            for (Path path : stream) {
                String content = new String(Files.readAllBytes(path));
                documents.add(new Document(content));
            }

            // vector DB에 저장
            vectorStore.add(documents);
            log.info("저장된 document 수 : " + documents.size());

        } catch (IOException e) {
            log.info(e.toString());
        }

        log.info("[Service] SaveVector End");
    }

    @Override
    public List<Document> answer(String question) throws Exception {

        log.info("[Service] answer Start");

        log.info("question : " + question);

        // vector db에서 유사도 검색을 통해 3개의 답변 반환
        List<Document> answer = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(3));

        log.info("answer : " + answer);

        log.info("[Service] answer End");

        return answer;
    }

    @Override
    public List<Document> answer(StyleDTO pDTO) throws Exception {

        log.info("[Service] answer Start");

        String mainStyle = CmmUtil.nvl(pDTO.mainStyle());
        String subStyle = CmmUtil.nvl(pDTO.subStyle());
        String mainCategory = CmmUtil.nvl(pDTO.mainCategory());
        String subCategory = CmmUtil.nvl(pDTO.subCategory());
        String size = CmmUtil.nvl(pDTO.size());
        String gender = CmmUtil.nvl(pDTO.gender());
        String age = CmmUtil.nvl(pDTO.age());
        String color = CmmUtil.nvl(pDTO.color());

        String question = "The style is among " + mainStyle + ", the substyle is among " + subStyle + "," +
                " the category is among " + mainCategory + ", the subcategory is among " + subCategory + ", and the size is " +
                size + ", suitable for " + gender + " gender, " + age + " age group, and with the color " + color + ". Please recommend clothing.";


        log.info("question : " + question);

        List<Document> answer = vectorStore.similaritySearch(question);
        log.info("answer : " + answer);

        log.info("[Service] answer End");

        return answer;
    }

    @Override
    public List<String> styleList() throws Exception {

        log.info("[Service] styleList Start!");

        InputStream keyFile = ResourceUtils.getURL(fileKey).openStream();

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId)
                .setCredentials(GoogleCredentials.fromStream(keyFile)).build().getService();

        log.info("storage : " + storage);

        List<String> styleList = new ArrayList<>();

        Page<Blob> blobs =
                storage.list(
                        bucketName,
                        Storage.BlobListOption.currentDirectory());

        for (Blob blob : blobs.iterateAll())  {
            String prefix = blob.getName();

            Page<Blob> style =
                    storage.list(bucketName,
                            Storage.BlobListOption.prefix(prefix),
                            Storage.BlobListOption.currentDirectory());

            log.info("style : " + style);

            styleList.add(String.valueOf(style));
        }

        log.info("styleList : " + styleList);

        log.info("[Service] styleList End");

        return styleList;
    }
}
