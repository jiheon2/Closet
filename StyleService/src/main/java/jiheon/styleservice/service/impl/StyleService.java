package jiheon.styleservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jiheon.styleservice.dto.ShopDTO;
import jiheon.styleservice.dto.StyleDTO;
import jiheon.styleservice.service.IStyleService;
import jiheon.styleservice.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    private final OpenAiChatModel chatModel;
    private final MongoTemplate mongoTemplate;


    // 생성자 주입 방법
    public StyleService(VectorStore vectorStore, OpenAiChatModel chatModel, MongoTemplate mongoTemplate) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.mongoTemplate = mongoTemplate;
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
    public String answer(String question) throws Exception {

        log.info("[Service] answer Start");

        log.info("question : " + question);

        // vector db에서 유사도 검색을 통해 3개의 답변 반환
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(3));

        List<String> styleList = similarDocuments.stream().map(Document::getContent).toList();

        String template =
                "{documents}를 참고한 답변을 알려줘. 사용자가 질문한 스타일과 비슷한 스타일을 추천해줘. 스타일을 추천할 땐 스타일의 이름만 추천해줘" +
                        "맨 마지막줄에 [추천스타일] 1.OO 2.OO 3.OO 이러한 형태로 스타일을 3개 나열해줘. 모든 답변은 한국어로 하고 간략하게 스타일에 대한 설명을 더해줘";

        PromptTemplate promptTemplate = new PromptTemplate(template);

        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", question);
        promptParameters.put("documents", String.join("\n", styleList));
        Prompt prompt = promptTemplate.create(promptParameters);

        log.info("[Service] answer End");

        return chatModel.call(prompt).getResult().getOutput().getContent();
    }

    @Override
    public String answer(StyleDTO pDTO) throws Exception {

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

        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(3));

        List<String> styleList = similarDocuments.stream().map(Document::getContent).toList();

        String template =
                "{documents}를 참고한 답변을 알려줘. 사용자가 질문한 스타일과 비슷한 스타일을 추천해줘. 스타일을 추천할 땐 스타일의 이름만 추천해줘" +
                        "맨 마지막줄에 [추천스타일] 1.OO 2.OO 3.OO 이러한 형태로 스타일을 3개 나열해줘. 모든 답변은 한국어로 하고 간략하게 스타일에 대한 설명을 더해줘";

        PromptTemplate promptTemplate = new PromptTemplate(template);

        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", question);
        promptParameters.put("documents", String.join("\n", styleList));
        Prompt prompt = promptTemplate.create(promptParameters);

        log.info("[Service] answer End");

        return chatModel.call(prompt).getResult().getOutput().getContent();
    }

    @Override
    public List<String> styleDictionary(String style) throws Exception {

        log.info("[Service] styleDictionary Start!");

        InputStream keyFile = ResourceUtils.getURL(fileKey).openStream();

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId)
                .setCredentials(GoogleCredentials.fromStream(keyFile)).build().getService();

        log.info("storage : " + storage);

        List<String> styleDictionary = new ArrayList<>();
        ;

        // style 파라미터를 사용하여 특정 폴더 안의 이미지들만 가져오기
        String folderPrefix = style + "/";
        log.info("folderPrefix : " + folderPrefix);

        Page<Blob> styleList =
                storage.list(bucketName,
                        Storage.BlobListOption.prefix(folderPrefix),
                        Storage.BlobListOption.currentDirectory());

        log.info("styleList : " + styleList);

        for (Blob image : styleList.iterateAll()) {
            // 공개 URL 생성
            String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, image.getName());
            log.info("Public URL : " + publicUrl);

            // 공개 URL을 리스트에 추가
            styleDictionary.add(publicUrl);
        }

        log.info("styleDictionary : " + styleDictionary);

        log.info("[Service] styleDictionary End");

        return styleDictionary;
    }

    @Override
    public Map styleInfo(int imageNum) throws Exception {

        log.info("[Service] styleInfo Start");
        log.info("imageNum : " + imageNum);
        return Objects.requireNonNull(mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("imageInfo.number").is(imageNum))
                ),
                "style",
                Map.class
        ).getUniqueMappedResult());
    }


    @Override
    public List<ShopDTO.Item> shop(String item) throws Exception {

        log.info("[Service] shop Start");

        String clientId = "JujXD4i42hadx4UA0gOz";
        String clientSecret = "InMFT81vfH";

        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/shop.json")
                .queryParam("query", item+"스타일 옷")
                .queryParam("display", 5)
                .queryParam("start", 1)
                .queryParam("sort", "sim")
                .encode()
                .build()
                .toUri();

        RequestEntity<Void> req = RequestEntity
                .get(uri)
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(req, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        ShopDTO rDTO = objectMapper.readValue(response.getBody(), ShopDTO.class);

        List<ShopDTO.Item> items = rDTO.items();

        log.info("items : " + items);

        log.info("[Service] shop End!");

        return items;
    }
}
