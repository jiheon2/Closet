package jiheon.userservice.service.impl;

import jiheon.userservice.dto.KafkaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final RestTemplate restTemplate;

    @KafkaListener(topics = "user-delete-topic", groupId = "user-group")
    public void consume(String userId) {
        log.info("[Service] Kafka Start");

        log.info("userId : " + userId);

        String closetUrl = "http://localhost:14000/closet/v1/delete";
        HttpHeaders closetHeaders = new HttpHeaders();

        HttpEntity<String> closetEntity = new HttpEntity<>(userId, closetHeaders);
        try {
            restTemplate.exchange(closetUrl, HttpMethod.DELETE, closetEntity, Void.class);
        } catch (Exception e) {
            log.error("삭제 요청 중 오류 발생", e);
        }

        String communityUrl = "http://localhost:16000/community/v1/delete";
        HttpHeaders communityHeaders = new HttpHeaders();

        HttpEntity<String> communityEntity = new HttpEntity<>(userId, communityHeaders);
        try {
            restTemplate.exchange(communityUrl, HttpMethod.DELETE, communityEntity, Void.class);
        } catch (Exception e) {
            log.error("삭제 요청 중 오류 발생", e);
        }

        log.info("[Service] Kafka End");
    }
}
